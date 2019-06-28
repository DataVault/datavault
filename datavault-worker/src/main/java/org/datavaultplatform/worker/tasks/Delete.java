package org.datavaultplatform.worker.tasks;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.delete.DeleteComplete;
import org.datavaultplatform.common.event.delete.DeleteStart;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.worker.operations.FileSplitter;
import org.datavaultplatform.worker.operations.ProgressTracker;
import org.datavaultplatform.worker.queue.EventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Delete extends Task{

	private static final Logger logger = LoggerFactory.getLogger(Delete.class);
    private String archiveId = null;
    
    private String userID = null;
    private int numOfChunks = 0;
    
    private String depositId = null;
    private String bagID = null;
    private long archiveSize = 0;
    private EventSender eventStream = null;
    // Maps the model ArchiveStore Id to the storage equivelant
    HashMap<String, ArchiveStore> archiveStores = new HashMap<>();
    
    @Override
    public void performAction(Context context) {
        
        this.eventStream = (EventSender)context.getEventStream();
        logger.info("Delete job - performAction()");
        Map<String, String> properties = getProperties();
        this.depositId = properties.get("depositId");
        this.bagID = properties.get("bagId");
        this.userID = properties.get("userId");
        this.numOfChunks = Integer.parseInt(properties.get("numOfChunks"));
        this.archiveSize = Long.parseLong(properties.get("archiveSize"));

        if (this.isRedeliver()) {
            eventStream.send(new Error(this.jobID, this.depositId, "Delete stopped: the message had been redelivered, please investigate")
                .withUserId(this.userID));
            return;
        }
        
        this.initStates();
        
        logger.info("bagID: " + this.bagID);
        
        //userStores = this.setupUserFileStores();
        this.setupArchiveFileStores();
        
        try {
        	String tarFileName = this.bagID + ".tar";
        	Path tarPath = context.getTempDir().resolve(tarFileName);
            File tarFile = tarPath.toFile();
            
            eventStream.send(new DeleteStart(this.jobID, this.depositId).withNextState(0)
                    .withUserId(this.userID));
            
            eventStream.send(new UpdateProgress(this.jobID, this.depositId, 0, this.archiveSize, "Deposit delete started ...")
                    .withUserId(this.userID)
                    .withNextState(1));
            
            for (String archiveStoreId : archiveStores.keySet() ) {
                ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
                this.archiveId = properties.get(archiveStoreId);
                logger.info("archiveId: " + this.archiveId);
                Device archiveFs = ((Device)archiveStore);
                if(archiveFs.hasMultipleCopies()) {
                	deleteMultipleCopiesFromArchiveStorage(context, archiveFs, tarFileName, tarFile);
                } else {
                	deleteFromArchiveStorage(context, archiveFs, tarFileName, tarFile);
                }
            }
            
            eventStream.send(new DeleteComplete(this.jobID, this.depositId).withNextState(2)
                    .withUserId(this.userID));
            
        } catch (Exception e) {
            String msg = "Deposit delete failed: " + e.getMessage();
            logger.error(msg, e);
            eventStream.send(new Error(jobID, depositId, msg)
                .withUserId(userID));
            throw new RuntimeException(e);
        }
    }
    
    private void initStates() {
    	ArrayList<String> states = new ArrayList<>();
        states.add("Deleting from archive"); // 0
        states.add("Delete complete");  // 1
        eventStream.send(new InitStates(this.jobID, this.depositId, states)
            .withUserId(userID));		
	}
    

    private void setupArchiveFileStores() {
    	// Connect to the archive storage(s). Look out! There are two classes called archiveStore.
    	for (org.datavaultplatform.common.model.ArchiveStore archiveFileStore : archiveFileStores ) {
    		try {
    			Class<?> clazz = Class.forName(archiveFileStore.getStorageClass());
	            Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
	            Object instance = constructor.newInstance(archiveFileStore.getStorageClass(), archiveFileStore.getProperties());
	
	            archiveStores.put(archiveFileStore.getID(), (ArchiveStore)instance);
	
    		} catch (Exception e) {
    			String msg = "Deposit failed: could not access archive filesystem : " + archiveFileStore.getStorageClass();
	            logger.error(msg, e);
	            eventStream.send(new Error(this.jobID, this.depositId, msg).withUserId(this.userID));
	            throw new RuntimeException(e);
	        }
    	}
    }
    
    private void deleteMultipleCopiesFromArchiveStorage(Context context, Device archiveFs, String tarFileName, File tarFile) throws Exception {

        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, this.jobID, this.depositId, this.archiveSize, this.eventStream);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();
        logger.info("deleteMultipleCopiesFromArchiveStorage for deposit : {}",this.depositId);
    	 List<String> locations = archiveFs.getLocations();
    	 for(String location : locations) {
    		 logger.info("Delte from location : {}",location);
    		 try {
    			 if (context.isChunkingEnabled()) {
    				 for( int chunkNum = 1; chunkNum <= this.numOfChunks; chunkNum++) {
    					Path chunkPath = context.getTempDir().resolve(tarFileName+FileSplitter.CHUNK_SEPARATOR+chunkNum);
			            File chunkFile = chunkPath.toFile();
			            String chunkArchiveId = this.archiveId+FileSplitter.CHUNK_SEPARATOR+chunkNum;
			            archiveFs.delete(chunkArchiveId,chunkFile, progress,location);
			            logger.info("---------deleteMultipleCopiesFromArchiveStorage ------After Delete---- {} ",chunkArchiveId);
    				 }
    			 } else {
    				 archiveFs.delete(this.archiveId,tarFile, progress,location);
    				 logger.info("---------deleteMultipleCopiesFromArchiveStorage ------After Delete---- {} ",this.archiveId);
    			 }
    			 
    		 } finally {
                 // Stop the tracking thread
                 tracker.stop();
                 trackerThread.join();
             }
    	 }
            
    }
    
    private void deleteFromArchiveStorage(Context context, Device archiveFs, String tarFileName, File tarFile) throws Exception {
            Progress progress = new Progress();
            ProgressTracker tracker = new ProgressTracker(progress, this.jobID, this.depositId, this.archiveSize, this.eventStream);
            Thread trackerThread = new Thread(tracker);
            trackerThread.start();

            logger.info("deleteFromArchiveStorage for deposit : {}",this.depositId);
            try {
            	if (context.isChunkingEnabled()) {
            		for( int chunkNum = 1; chunkNum <= this.numOfChunks; chunkNum++) {
            			Path chunkPath = context.getTempDir().resolve(tarFileName+FileSplitter.CHUNK_SEPARATOR+chunkNum);
			            File chunkFile = chunkPath.toFile();
			            String chunkArchiveId = this.archiveId+FileSplitter.CHUNK_SEPARATOR+chunkNum;
			            archiveFs.delete(chunkArchiveId,chunkFile, progress);
			            logger.info("---------deleteFromArchiveStorage ------After Delete---- {} ",chunkArchiveId);
            		}
            	} else {
            		archiveFs.delete(this.archiveId,tarFile, progress);
		            logger.info("---------deleteFromArchiveStorage ------After Delete---- {} ",this.archiveId);
            	}
            		
            } finally {
                // Stop the tracking thread
                tracker.stop();
                trackerThread.join();
            }
            
        } 
}