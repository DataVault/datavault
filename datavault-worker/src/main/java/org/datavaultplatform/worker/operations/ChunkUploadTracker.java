package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.worker.queue.EventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class ChunkUploadTracker implements Callable {

    private File chunk;
    private int chunkCount;
    private HashMap<String, ArchiveStore> archiveStores = new HashMap<>();
    private String depositId;
    private String jobID;
    private EventSender eventStream;
    private File tarFile;
    private String userID;
    private static final Logger logger = LoggerFactory.getLogger(ChunkUploadTracker.class);

    @Override
    public HashMap<String, String> call() throws Exception {
        logger.debug("Copying chunk: "+chunk.getName());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<HashMap<String, String>>> futures = new ArrayList();
        HashMap<String, String> archiveIds = new HashMap<>();
        for (String archiveStoreId : archiveStores.keySet() ) {
            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
            DeviceTracker dt = new DeviceTracker();
            dt.setArchiveStore(archiveStore);
            dt.setArchiveStoreId(archiveStoreId);
            dt.setChunkCount(chunkCount);
            dt.setDepositId(depositId);
            dt.setJobID(jobID);
            dt.setEventStream(eventStream);
            dt.setTarFile(chunk);
            dt.setUserID(userID);
            logger.debug("Creating device thread:" + archiveStore.getClass());
            Future<HashMap<String, String>> dtFuture = executor.submit(dt);
            futures.add(dtFuture);
        }
        executor.shutdown();

        for (Future<HashMap<String, String>> future : futures) {
            try {
                HashMap<String, String> result = future.get();
                archiveIds.putAll(result);
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                if (cause instanceof Exception) {
                    logger.info("Device upload failed. " + cause.getMessage());
                    throw (Exception) cause;
                }
            }
        }
        logger.debug("Chunk upload thread completed: " + this.chunkCount);
        return archiveIds;
    }

    public File getChunk() {
        return this.chunk;
    }

    public void setChunk(File chunk) {
        this.chunk = chunk;
    }

    public int getChunkCount() {
        return this.chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    public HashMap<String, ArchiveStore> getArchiveStores() {
        return this.archiveStores;
    }

    public void setArchiveStores(HashMap<String, ArchiveStore> archiveStores) {
        this.archiveStores = archiveStores;
    }

    public String getDepositId() {
        return this.depositId;
    }

    public void setDepositId(String depositId) {
        this.depositId = depositId;
    }

    public String getJobID() {
        return this.jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public EventSender getEventStream() {
        return this.eventStream;
    }

    public void setEventStream(EventSender eventStream) {
        this.eventStream = eventStream;
    }

    public File getTarFile() {
        return this.tarFile;
    }

    public void setTarFile(File tarFile) {
        this.tarFile = tarFile;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
