package org.datavaultplatform.worker.tasks;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.event.retrieve.RetrieveStart;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.worker.operations.FileSplitter;
import org.datavaultplatform.worker.operations.ProgressTracker;
import org.datavaultplatform.worker.operations.Tar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * A class that extends Task which is used to handle Retrievals from the vault
 */
public class Retrieve extends Task {
    
    private static final Logger logger = LoggerFactory.getLogger(Retrieve.class);
    private String archiveId = null;
    private String archiveDigestAlgorithm = null;
    private String archiveDigest = null;
    private String userID = null;
    private int numOfChunks = 0;
    private Map<Integer, String> chunksDigest = null;
    private Map<Integer, String> encChunksDigest = null;
    private String encTarDigest = null;
    private String retrievePath = null;
    private String retrieveId = null;
    private String depositId = null;
    private String bagID = null;
    private long archiveSize = 0;
    private EventSender eventSender = null;
    
    /* (non-Javadoc)
     * @see org.datavaultplatform.common.task.Task#performAction(org.datavaultplatform.common.task.Context)
     * 
     * Connect to the user's file store, check if they have enough free space, retrieve the archive and transfer to the users file store.
     * During the operation we untar / validate the archive before cleaning up.
     * Events are created for each stage of the operation.
     * 
     * The user's file store can be a local disk, Drop box, Amazon Glacier, SFTP ...
     *  
     * @param context
     */
    @Override
    public void performAction(Context context) {
        
        this.eventSender = context.getEventSender();
        logger.info("Retrieve job - performAction()");
        Map<String, String> properties = getProperties();
        this.depositId = properties.get("depositId");
        this.retrieveId = properties.get("retrieveId");
        this.bagID = properties.get("bagId");
        this.retrievePath = properties.get("retrievePath");
        this.archiveId = properties.get("archiveId");
        this.userID = properties.get("userId");
        this.archiveDigest = properties.get("archiveDigest");
        this.archiveDigestAlgorithm = properties.get("archiveDigestAlgorithm");
        this.numOfChunks = Integer.parseInt(properties.get("numOfChunks"));
        this.archiveSize = Long.parseLong(properties.get("archiveSize"));
        this.chunksDigest = this.getChunkFilesDigest();
        this.encChunksDigest = this.getEncChunksDigest();
        this.encTarDigest = this.getEncTarDigest();

        if (this.isRedeliver()) {
            eventSender.send(new Error(this.jobID, this.depositId, "Retrieve stopped: the message had been redelivered, please investigate")
                .withUserId(this.userID));
            return;
        }
        
        this.initStates();
        
        eventSender.send(new RetrieveStart(this.jobID, this.depositId, this.retrieveId)
            .withUserId(this.userID)
            .withNextState(0));
        
        logger.info("bagID: " + this.bagID);
        logger.info("retrievePath: " + this.retrievePath);
        
        Device userFs = this.setupUserFileStores();
        Device archiveFs = this.setupArchiveFileStores();
        
        try {
        	this.checkUserStoreFreeSpace(userFs);

            // Retrieve the archived data
            String tarFileName = this.bagID + ".tar";
            
            // Copy the tar file from the archive to the temporary area
            Path tarPath = context.getTempDir().resolve(tarFileName);
            File tarFile = tarPath.toFile();
            
            eventSender.send(new UpdateProgress(this.jobID, this.depositId, 0, this.archiveSize, "Starting transfer ...")
                .withUserId(this.userID)
                .withNextState(1));
            
            if (archiveFs.hasMultipleCopies()) {
            	this.multipleCopies(context, tarFileName, tarFile, archiveFs, userFs);
            } else {
            	this.singleCopy(context, tarFileName, tarFile, archiveFs, userFs);
            }
            
        } catch (Exception e) {
            String msg = "Data retrieve failed: " + e.getMessage();
            logger.error(msg, e);
            eventSender.send(new Error(jobID, depositId, msg)
                .withUserId(userID));
            throw new RuntimeException(e);
        }
    }
    
    private void recomposeSingle(String tarFileName, Context context, Device archiveFs, Progress progress, File tarFile) throws Exception {
        recompose(tarFileName, context, archiveFs, progress, tarFile, false, null);
    }
    
    private void recomposeMulti(String tarFileName, Context context, Device archiveFs, Progress progress, File tarFile, String location) throws Exception {
        recompose(tarFileName, context, archiveFs, progress, tarFile, true, location);
    }
    
    private void recompose(String tarFileName, Context context, Device archiveFs, Progress progress, 
            File tarFile, boolean singleCopy, String location) throws Exception {

        File[] chunks = new File[this.numOfChunks];
        logger.info("Retrieving " + this.numOfChunks + " chunk(s)");
        for( int chunkNum = 1; chunkNum <= this.numOfChunks; chunkNum++) {
            Path chunkPath = context.getTempDir().resolve(tarFileName+FileSplitter.CHUNK_SEPARATOR+chunkNum);
            File chunkFile = chunkPath.toFile();
            String chunkArchiveId = this.archiveId+FileSplitter.CHUNK_SEPARATOR+chunkNum;
            if (! singleCopy) {
                archiveFs.retrieve(chunkArchiveId, chunkFile, progress);
            } else {
                archiveFs.retrieve(chunkArchiveId, chunkFile, progress, location);
            }
            chunks[chunkNum-1] = chunkFile;
            
            if( this.getChunksIVs().get(chunkNum) != null ) {
                String archivedEncChunkFileHash = this.encChunksDigest.get(chunkNum);
                
                // Check encrypted file checksum
                String encChunkFileHash = Verify.getDigest(chunkFile);
                
                logger.info("Encrypted chunk Checksum algorithm: " + this.archiveDigestAlgorithm);
                logger.info("Encrypted Checksum: " + encChunkFileHash);
                
                if (!encChunkFileHash.equals(archivedEncChunkFileHash)) {
                    throw new Exception("checksum failed: " + encChunkFileHash + " != " + archivedEncChunkFileHash);
                }
                
                Encryption.decryptFile(context, chunkFile, this.getChunksIVs().get(chunkNum));
            }
            
            // Check file
            String archivedChunkFileHash = this.chunksDigest.get(chunkNum);
            
            // TODO: Should we check algorithm each time or assume main tar file algorithm is the same
            // We might also want to move algorithm check before this loop
            String chunkFileHash = Verify.getDigest(chunkFile);
            
            logger.info("Chunk Checksum algorithm: " + this.archiveDigestAlgorithm);
            logger.info("Checksum: " + chunkFileHash);
            
            if (!chunkFileHash.equals(archivedChunkFileHash)) {
                throw new Exception("checksum failed: " + chunkFileHash + " != " + archivedChunkFileHash);
            }
        }

        logger.info("Recomposing tar file from chunk(s)");
        FileSplitter.recomposeFile(chunks, tarFile);

        // On the assumption that we have the tarfile now, delete the chunks
        logger.info("Deleting the chunks now we have the recomposed tarfile");
        for (File chunk : chunks) {
            chunk.delete();
        }
    }
    
    private void doRetrieve(Context context, Device userFs, File tarFile, Progress progress) throws Exception{
        logger.info("Copied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
        
        logger.info("Validating data ...");
        eventSender.send(new UpdateProgress(this.jobID, this.depositId).withNextState(2)
            .withUserId(this.userID));
        
        String tarHash = Verify.getDigest(tarFile);
        logger.info("Checksum algorithm: " + this.archiveDigestAlgorithm);
        logger.info("Checksum: " + tarHash);
        
        if (!tarHash.equals(this.archiveDigest)) {
        		logger.info("Checksum failed: " + tarHash + " != " + this.archiveDigest);
            throw new Exception("checksum failed: " + tarHash + " != " + this.archiveDigest);
        }
        
        // Decompress to the temporary directory
        File bagDir = Tar.unTar(tarFile, context.getTempDir());
        long bagDirSize = FileUtils.sizeOfDirectory(bagDir);
        
        // Validate the bagit directory
        //if (!Packager.validateBag(bagDir)) {
        //    throw new Exception("Bag is invalid");
        //}

        // Get the payload data directory
        File payloadDir = bagDir.toPath().resolve("data").toFile();
        //File payloadDir = bagDir;
        //long payloadSize = FileUtils.sizeOfDirectory(payloadDir);

        // Copy the extracted files to the target retrieve area
        logger.info("Copying to user directory ...");
        eventSender.send(new UpdateProgress(this.jobID, this.depositId, 0, bagDirSize, "Starting transfer ...")
            .withUserId(this.userID)
            .withNextState(3));
        
        // Progress tracking (threaded)
        progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, this.jobID, this.depositId, bagDirSize, this.eventSender);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();

        try {
            ArrayList<File> contents = new ArrayList<>(Arrays.asList(payloadDir.listFiles()));
            for(File content: contents){
                userFs.store(this.retrievePath, content, progress);
            }
        } finally {
            // Stop the tracking thread
            tracker.stop();
            trackerThread.join();
        }
        
        logger.info("Copied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
        
        // Cleanup
        logger.info("Cleaning up ...");
        FileUtils.deleteDirectory(bagDir);
        tarFile.delete();



        logger.info("Data retrieve complete: " + this.retrievePath);
        eventSender.send(new RetrieveComplete(this.jobID, this.depositId, this.retrieveId).withNextState(4)
            .withUserId(this.userID));
    }
    
    private void initStates() {
    	ArrayList<String> states = new ArrayList<>();
        states.add("Computing free space");    // 0
        states.add("Retrieving from archive"); // 1
        states.add("Validating data");         // 2
        states.add("Transferring files");      // 3
        states.add("Data retrieve complete");  // 4
        eventSender.send(new InitStates(this.jobID, this.depositId, states)
            .withUserId(userID));		
	}
    
    private Device setupUserFileStores() {
        Device userFs = null;
        
        for (String storageID : userFileStoreClasses.keySet()) {
            
            String storageClass = userFileStoreClasses.get(storageID);
            Map<String, String> storageProperties = userFileStoreProperties.get(storageID);
            
            // Connect to the first user storage device (we only expect one for a retrieval)
            try {
                userFs = StorageClassUtils.createStorage(storageClass, storageProperties, Device.class);
                logger.info("Connected to user store: " + storageID + ", class: " + storageClass);
                break;
            } catch (Exception e) {
                String msg = "Deposit failed: could not access user filesystem";
                logger.error(msg, e);
                eventSender.send(new Error(this.jobID, this.depositId, msg)
                    .withUserId(this.userID));
                throw new RuntimeException(e);
            }
        }
        
        return userFs;
    }
    
    private Device setupArchiveFileStores() {
    	// We get passed a list because it is a parameter common to deposits and retrieves, but for retrieve there should only be one.
        ArchiveStore archiveFileStore = archiveFileStores.get(0);

        // Connect to the archive storage
        try {
            Device archiveFs = StorageClassUtils.createStorage(
                archiveFileStore.getStorageClass(),
                archiveFileStore.getProperties(),
                Device.class);
            return archiveFs;
        } catch (Exception e) {
            String msg = "Retrieve failed: could not access archive filesystem";
            logger.error(msg, e);
            eventSender.send(new Error(this.jobID, this.depositId, msg)
                .withUserId(userID));

            throw new RuntimeException(e);
        }
    }
    
    private void checkUserStoreFreeSpace(Device userFs) throws Exception {
    	UserStore userStore = ((UserStore)userFs);
        if (!userStore.exists(retrievePath) || !userStore.isDirectory(retrievePath)) {
            // Target path must exist and be a directory
            logger.info("Target directory not found or is not a directory ! [{}]", retrievePath);
        }
        
        // Check that there's enough free space ...
        try {
            long freespace = userFs.getUsableSpace();
            logger.info("Free space: " + freespace + " bytes (" +  FileUtils.byteCountToDisplaySize(freespace) + ")");
            if (freespace < archiveSize) {
                eventSender.send(new Error(this.jobID, this.depositId, "Not enough free space to retrieve data!")
                    .withUserId(this.userID));
            }
        } catch (Exception e) {
            logger.info("Unable to determine free space");
            eventSender.send(new Error(jobID, depositId, "Unable to determine free space")
                .withUserId(this.userID));

            throw new RuntimeException(e);
        }
    }
    
    private void multipleCopies(Context context, String tarFileName, File tarFile, Device archiveFs, Device userFs) throws Exception {
    	logger.info("Device has multiple copies");
        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, this.jobID, this.depositId, this.archiveSize, this.eventSender);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();

        List<String> locations = archiveFs.getLocations();
        Iterator<String> locationsIt = locations.iterator();
        LOCATION: 
        while (locationsIt.hasNext()) {
            String location = locationsIt.next();
            logger.info("Current " + location);
            try {
                try {
                    // NEED TO UPDATE THIS TO INCLUDE CHUNKING STUFF IS TURNED ON
                    if (context.isChunkingEnabled()) {
                        // TODO can bypass this if there is only one chunk.
                        recomposeMulti(tarFileName, context, archiveFs, progress, tarFile, location);
                    } else {
                        // Ask the driver to copy files to the temp directory
                        archiveFs.retrieve(this.archiveId, tarFile, progress, location);
                    }
                } finally {
                    // Stop the tracking thread
                    tracker.stop();
                    trackerThread.join();
                }

                logger.info("Attempting retrieve on archive from " + location);
                this.doRetrieve(context, userFs, tarFile, progress);
                logger.info("Completed retrieve on archive from " + location);
                break LOCATION;
            } catch (Exception e) {
                // if last location has an error throw the error else go
                // round again
                if (!locationsIt.hasNext()) {
                    logger.info("All locations had problems throwing exception " + e.getMessage());
                    throw e;
                } else {
                	logger.info("Current " + location + " has a problem trying next location");
                	continue LOCATION;
                }
            }
        }
    }
    
    private void singleCopy(Context context, String tarFileName, File tarFile, Device archiveFs, Device userFs) throws Exception {
    	logger.info("Single copy device");
        // Progress tracking (threaded)
        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, this.jobID, this.depositId, this.archiveSize, this.eventSender);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();
        

        // Verify integrity with deposit checksum
        String systemAlgorithm = Verify.getAlgorithm();
        if (!systemAlgorithm.equals(this.archiveDigestAlgorithm)) {
            throw new Exception("Unsupported checksum algorithm: " + this.archiveDigestAlgorithm);
        }

        try {
            // Ask the driver to copy files to the temp directory
            if (context.isChunkingEnabled()) {
                // TODO can bypass this if there is only one chunk.
                recomposeSingle(tarFileName, context, archiveFs, progress, tarFile);
            } else {
                archiveFs.retrieve(this.archiveId, tarFile, progress);

                if (this.getTarIV() != null) {
                    // Decrypt tar file
                    String encTarFileHash = Verify.getDigest(tarFile);
                    
                    logger.info("Encrypted tar Checksum algorithm: " + this.archiveDigestAlgorithm);
                    logger.info("Encrypted tar Checksum: " + encTarFileHash);
                    
                    if (!encTarFileHash.equals(this.encTarDigest)) {
                        throw new Exception("checksum failed: " + encTarFileHash + " != " + this.encTarDigest);
                    }
                    
                    Encryption.decryptFile(context, tarFile, this.getTarIV());
                }
            }
        } finally {
            // Stop the tracking thread
            tracker.stop();
            trackerThread.join();
        }

        this.doRetrieve(context, userFs, tarFile, progress);
    }
}