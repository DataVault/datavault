package org.datavaultplatform.worker.tasks;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.delete.DeleteComplete;
import org.datavaultplatform.common.event.delete.DeleteStart;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.worker.operations.FileSplitter;
import org.datavaultplatform.worker.operations.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Delete extends Task {

	private static final Logger logger = LoggerFactory.getLogger(Delete.class);
    private String archiveId = null;
    
    private String userID = null;
    private int numOfChunks = 0;
    
    private String depositId = null;
    private long archiveSize = 0;
    private EventSender eventSender = null;
    // Maps the model ArchiveStore ID to the storage equivalent
    private final HashMap<String, ArchiveStore> archiveStores = new HashMap<>();

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

        if (this.isRedeliver()) {
            eventSender.send(new Error(this.jobID, this.depositId, "Delete stopped: the message had been redelivered, please investigate")
                .withUserId(this.userID));
            return;
        }
        
        this.initStates();

        logger.info("bagID: {}", bagID);
        
        //userStores = this.setupUserFileStores();
        this.setupArchiveFileStores(context.getStorageClassNameResolver());
        
        try {
        	String tarFileName = bagID + ".tar";
        	Path tarPath = context.getTempDir().resolve(tarFileName);
            File tarFile = tarPath.toFile();
            eventSender.send(new DeleteStart(this.jobID, this.depositId).withNextState(0)
                    .withUserId(this.userID));
            
            eventSender.send(new UpdateProgress(this.jobID, this.depositId, 0, this.archiveSize, "Deposit delete started ...")
                    .withUserId(this.userID));
            for (String archiveStoreId : archiveStores.keySet() ) {
                ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
                this.archiveId = properties.get(archiveStoreId);
                logger.info("archiveId: {}", this.archiveId);
                Device archiveFs = ((Device)archiveStore);
                if(archiveFs.hasMultipleCopies()) {
                	deleteMultipleCopiesFromArchiveStorage(context, archiveFs, tarFileName, tarFile);
                } else {
                	deleteFromArchiveStorage(context, archiveFs, tarFileName, tarFile);
                }
            }
            
            eventSender.send(new DeleteComplete(this.jobID, this.depositId).withNextState(1)
                    .withUserId(this.userID));
            
        } catch (Exception e) {
            String msg = "Deposit delete failed: " + e.getMessage();
            logger.error(msg, e);
            eventSender.send(new Error(jobID, depositId, msg)
                .withUserId(userID));
            throw new RuntimeException(e);
        }
    }
    
    private void initStates() {
    	ArrayList<String> states = new ArrayList<>();
        states.add("Deleting from archive"); // 0
        states.add("Delete complete");  // 1
        eventSender.send(new InitStates(this.jobID, this.depositId, states)
            .withUserId(userID));		
	}
    

    private void setupArchiveFileStores(StorageClassNameResolver resolver) {
    	// Connect to the archive storage(s). Look out! There are two classes called archiveStore.
    	for (org.datavaultplatform.common.model.ArchiveStore archiveFileStore : archiveFileStores ) {
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
    
    private void deleteMultipleCopiesFromArchiveStorage(Context context, Device archiveFs, String tarFileName, File tarFile) throws Exception {

        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, this.jobID, this.depositId, this.archiveSize, this.eventSender);
        tracker.track(() -> {
            logger.info("deleteMultipleCopiesFromArchiveStorage for deposit : {}", this.depositId);
            List<String> locations = archiveFs.getLocations();
            for (String location : locations) {
                logger.info("Delete from location : {}", location);
                if (context.isChunkingEnabled()) {
                    for (int chunkNum = 1; chunkNum <= this.numOfChunks; chunkNum++) {
                        Path chunkPath = context.getTempDir().resolve(tarFileName + FileSplitter.CHUNK_SEPARATOR + chunkNum);
                        File chunkFile = chunkPath.toFile();
                        String chunkArchiveId = this.archiveId + FileSplitter.CHUNK_SEPARATOR + chunkNum;
                        archiveFs.delete(chunkArchiveId, chunkFile, progress, location);
                        logger.info("---------deleteMultipleCopiesFromArchiveStorage ------chunkArchiveId Deleted---- {} ", chunkArchiveId);
                    }
                } else {
                    archiveFs.delete(this.archiveId, tarFile, progress, location);
                    logger.info("---------deleteMultipleCopiesFromArchiveStorage ------archiveId Deleted---- {} ", this.archiveId);
                }
            }
        });
    }
    
    private void deleteFromArchiveStorage(Context context, Device archiveFs, String tarFileName, File tarFile) throws Exception {
        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, this.jobID, this.depositId, this.archiveSize, this.eventSender);
        tracker.track(() -> {

            logger.info("deleteFromArchiveStorage for deposit : {}", this.depositId);
            if (context.isChunkingEnabled()) {
                for (int chunkNum = 1; chunkNum <= this.numOfChunks; chunkNum++) {
                    Path chunkPath = context.getTempDir().resolve(tarFileName + FileSplitter.CHUNK_SEPARATOR + chunkNum);
                    File chunkFile = chunkPath.toFile();
                    String chunkArchiveId = this.archiveId + FileSplitter.CHUNK_SEPARATOR + chunkNum;
                    archiveFs.delete(chunkArchiveId, chunkFile, progress);
                    logger.info("---------deleteFromArchiveStorage ------chunkArchiveId Deleted---- {} ", chunkArchiveId);
                }
            } else {
                archiveFs.delete(this.archiveId, tarFile, progress);
                logger.info("---------deleteFromArchiveStorage ------archiveId Deleted---- {} ", this.archiveId);
            }
        });
    }
}