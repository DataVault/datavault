package org.datavaultplatform.worker.tasks;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import lombok.AllArgsConstructor;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.delete.DeleteComplete;
import org.datavaultplatform.common.event.delete.DeleteStart;
import org.datavaultplatform.common.event.delete.DeletedChunk;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.worker.operations.FileSplitter;
import org.datavaultplatform.worker.operations.ProgressTracker;
import org.datavaultplatform.worker.tasks.delete.DeleteState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import static java.util.Comparator.*;

public class Delete extends Task {

    public static final String NO_LOCATION = "no-location";
    private static final Logger logger = LoggerFactory.getLogger(Delete.class);
    // Maps the model ArchiveStore ID to the storage equivalent
    private final HashMap<String, ArchiveStore> archiveStores = new HashMap<>();
    private String archiveId = null;
    private String userID = null;
    private int numOfChunks = 0;
    private String depositId = null;
    private long archiveSize = 0;
    private EventSender eventSender = null;
    private boolean sendDeletedChunkEvents = false;

    @Override
    public void performAction(Context context) {
        
        this.eventSender = context.getEventSender();
        logger.info("Delete job - performAction()");
        Map<String, String> properties = getProperties();
        this.depositId = properties.get(PropNames.DEPOSIT_ID);
        String bagID = properties.get(PropNames.BAG_ID);
        this.userID = properties.get(PropNames.USER_ID);
        this.numOfChunks = Integer.parseInt(properties.get(PropNames.NUM_OF_CHUNKS));
        this.archiveSize = Long.parseLong(properties.get(PropNames.ARCHIVE_SIZE));
        this.sendDeletedChunkEvents = Boolean.parseBoolean(properties.get(PropNames.WORKERS_SEND_DELETED_CHUNK_EVENTS));

        if (this.isRedeliver()) {
            eventSender.send(new Error(this.jobID, this.depositId, "Delete stopped: the message had been redelivered, please investigate")
                .withUserId(this.userID));
            return;
        }
        
        this.initStates();

        final TaskExecutor<DeletedChunk> taskExecutor;

        logger.info("bagID: {}", bagID);
        
        this.setupArchiveFileStores(context.getStorageClassNameResolver());
        
        try {
            taskExecutor = getTaskExecutor(context);
            
        	String tarFileName = bagID + ".tar";
        	Path tarPath = context.getTempDir().resolve(tarFileName);
            File tarFile = tarPath.toFile();
            eventSender.send(new DeleteStart(this.jobID, this.depositId)
                    .withNextState(DeleteState.DeleteState00DeleteStart.getStateNumber())
                    .withUserId(this.userID));
            
            eventSender.send(new UpdateProgress(this.jobID, this.depositId, 0, this.archiveSize, "Deposit delete started ...")
                    .withUserId(this.userID));
            for (Map.Entry<String, ArchiveStore> entry : archiveStores.entrySet()) {
                ArchiveContext archiveContext = new ArchiveContext(entry);
                this.archiveId = properties.get(archiveContext.archiveStoreId);
                logger.info("archiveId: {}", this.archiveId);

                if (archiveContext.hasMultipleCopies()) {
                    deleteMultipleCopiesFromArchiveStorage(taskExecutor, context, archiveContext, tarFileName, tarFile);
                } else {
                    deleteFromArchiveStorage(taskExecutor, context, archiveContext, tarFileName, tarFile);
                }
            }

            List<DeletedChunk> deletedChunks = new ArrayList<>();
            taskExecutor.execute(deletedChunks::add); //this will throw an exception if there was an error with a single delete task

            sortAndLogDeletedChunks(deletedChunks);

            logger.info("Sending delete complete event");
            eventSender.send(new DeleteComplete(this.jobID, this.depositId)
                    .withNextState(DeleteState.DeleteState01DeleteComplete.getStateNumber())
                    .withUserId(this.userID));
            logger.info("Sent delete complete event");

        } catch (Exception ex) {
            String msg = "Deposit delete failed: " + ex.getMessage();
            logger.error(msg, ex);
            Event errEvent = new Error(jobID, depositId, msg)
                    .withUserId(userID);
            errEvent.setArchiveId(archiveId);
            if (ex instanceof DeleteFileException dfEx) {
                errEvent.setArchiveStoreId(dfEx.getArchiveStoreId());
                errEvent.setLocation(dfEx.getLocation());
                errEvent.setChunkNumber(dfEx.getChunkNumber());
            }
            eventSender.send(errEvent);
            throw new RuntimeException(ex);
        }
    }

    private void sortAndLogDeletedChunks(List<DeletedChunk> deletedChunks) {
        deletedChunks.sort(
                comparing(DeletedChunk::getArchiveStoreId, nullsLast(naturalOrder()))
                        .thenComparing(DeletedChunk::getLocation, nullsLast(naturalOrder()))
                        .thenComparing(DeletedChunk::getChunkNumber, nullsLast(naturalOrder()))
        );

        // if we get here - we can assume all deletes worked
        var size = deletedChunks.size();
        logger.info("Deleted [{}] Chunks", size);
        for (int i = 0; i < size; i++) {
            logger.info("Deleted Chunk[{}/{}][{}]", i + 1, size, deletedChunks.get(i));
        }
    }

    private void initStates() {
        eventSender.send(new InitStates(this.jobID, this.depositId, DeleteState.getDeleteStates())
                .withUserId(userID));
    }

    private void setupArchiveFileStores(StorageClassNameResolver resolver) {
        // Connect to the archive storage(s). Look out! There are two classes called archiveStore.
        for (org.datavaultplatform.common.model.ArchiveStore archiveFileStore : archiveFileStores) {
            try {
                ArchiveStore archiveStore = StorageClassUtils.createStorage(
                        archiveFileStore.getStorageClass(),
                        archiveFileStore.getProperties(),
                        ArchiveStore.class, resolver);
                archiveStores.put(archiveFileStore.getID(), archiveStore);
            } catch (Exception e) {
                String msg = "Deposit failed: could not access archive filesystem : " + archiveFileStore.getStorageClass();
                logger.error(msg, e);
                eventSender.send(new Error(this.jobID, this.depositId, msg).withUserId(this.userID));
                throw new RuntimeException(e);
            }
        }
    }

    private void deleteMultipleCopiesFromArchiveStorage(TaskExecutor<DeletedChunk> taskExecutor, Context context, ArchiveContext archiveContext, String tarFileName, File tarFile) throws Exception {

        final Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, this.jobID, this.depositId, this.archiveSize, this.eventSender);
        tracker.track(() -> {

            DeleteContext deleteContext = new DeleteContext(archiveContext, progress, taskExecutor);
            
            logger.info("deleteMultipleCopiesFromArchiveStorage for deposit : {}", this.depositId);
            for (String location : archiveContext.getLocations()) {
                logger.info("Delete from location : {}", location);
                if (context.isChunkingEnabled()) {
                    for (int chunkNum = 1; chunkNum <= this.numOfChunks; chunkNum++) {
                        Path chunkPath = context.getTempDir().resolve(tarFileName + FileSplitter.CHUNK_SEPARATOR + chunkNum);
                        File chunkFile = chunkPath.toFile();
                        String chunkArchiveId = this.archiveId + FileSplitter.CHUNK_SEPARATOR + chunkNum;
                        deleteContext.deleteChunkWithLocation(chunkNum, location, chunkArchiveId, chunkFile);
                    }
                } else {
                    deleteContext.deleteChunkWithLocation(0, location, this.archiveId, tarFile);
                }
            }
        });
    }

    private void deleteFromArchiveStorage(TaskExecutor<DeletedChunk> taskExecutor, Context context,
                                          ArchiveContext archiveContext, String tarFileName, File tarFile) throws Exception {
        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, this.jobID, this.depositId, this.archiveSize, this.eventSender);
        tracker.track(() -> {

            DeleteContext deleteContext = new DeleteContext(archiveContext, progress, taskExecutor);

            logger.info("deleteFromArchiveStorage for deposit : {}", this.depositId);
            if (context.isChunkingEnabled()) {
                for (int chunkNum = 1; chunkNum <= this.numOfChunks; chunkNum++) {
                    Path chunkPath = context.getTempDir().resolve(tarFileName + FileSplitter.CHUNK_SEPARATOR + chunkNum);
                    File chunkFile = chunkPath.toFile();
                    String chunkArchiveId = this.archiveId + FileSplitter.CHUNK_SEPARATOR + chunkNum;
                    deleteContext.deleteChunkNoLocation(chunkNum, chunkArchiveId, chunkFile);
                }
            } else {
                deleteContext.deleteChunkNoLocation(0, this.archiveId, tarFile);
            }
        });
    }

    private TaskExecutor<DeletedChunk> getTaskExecutor(Context context) {
        int noOfThreads = context.getNoChunkThreads();
        logger.debug("Number of threads: [{}]", noOfThreads);
        return new TaskExecutor<>(noOfThreads, "Delete depositId[%s]jobId[%s]numberOfChunks[%s] failed.".formatted(depositId, jobID, numOfChunks));
    }
    
    public record ArchiveContext(String archiveStoreId, ArchiveStore archiveStore) {

        public ArchiveContext {
            Assert.isTrue(archiveStoreId != null, "archiveStoreId cannot be null");
            Assert.isTrue(archiveStore != null, "archiveStore cannot be null");
        }

        public ArchiveContext(Map.Entry<String, ArchiveStore> entry) {
            this(entry.getKey(), entry.getValue());
        }

        public Device getDevice() {
            if (!(archiveStore instanceof Device device)) {
                throw new IllegalStateException("ArchiveContext [%s] is not a Device".formatted(archiveStore.getClass().getSimpleName()));
            }
            return device;
        }

        public boolean hasMultipleCopies() {
            return Boolean.TRUE.equals(getDevice().hasMultipleCopies());
        }

        public List<String> getLocations() {
            return getDevice().getLocations();
        }
    }

    @AllArgsConstructor
    class DeleteContext {
        private final ArchiveContext archiveContext;
        private final Progress progress;
        private final TaskExecutor<DeletedChunk> taskExecutor;

        private DeletedChunk sendDeletedChunkEvent(int chunkNumber, String location) {
                DeletedChunk event = new DeletedChunk(jobID, depositId, chunkNumber, numOfChunks, archiveContext.archiveStore().getClass(), archiveContext.archiveStoreId, location);
                event.setUserId(userID);
                event.setArchiveId(archiveId);
            if (sendDeletedChunkEvents) {
                logger.info("SENDING {}", event);
                eventSender.send(event);
            } else {
                logger.info("NOT SENDING {}", event);
            }
            return event;
        }

        // this ensures any Exception will always be recorded with the chunkNumber
        private void addDeleteFileTask(int chunkNum, String location, Callable<DeletedChunk> task) {
            taskExecutor.add(() -> {
                try {
                    return task.call();
                } catch (Exception ex) {
                    throw new DeleteFileException(archiveContext, location, chunkNum, ex);
                }
            });
        }

        void deleteChunkNoLocation(int chunkNum, String chunkArchiveId, File chunkFile) {
            addDeleteFileTask(chunkNum, NO_LOCATION, () -> {
                    Device archiveFs = archiveContext.getDevice();
                    archiveFs.delete(chunkArchiveId, chunkFile, progress);
                    return sendDeletedChunkEvent(chunkNum, NO_LOCATION);
            });
        }

        void deleteChunkWithLocation(int chunkNum, String location, String chunkArchiveId, File chunkFile) {
            addDeleteFileTask(chunkNum, location, () -> {
                Device archiveFs = archiveContext.getDevice();
                archiveFs.delete(chunkArchiveId, chunkFile, progress, location);
                return sendDeletedChunkEvent(chunkNum, location);
            });
        }
    }
}