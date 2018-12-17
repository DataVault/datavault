package org.datavaultplatform.worker.tasks;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Error;
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
import org.datavaultplatform.worker.operations.FileSplitter;
import org.datavaultplatform.worker.operations.Packager;
import org.datavaultplatform.worker.operations.ProgressTracker;
import org.datavaultplatform.worker.operations.Tar;
import org.datavaultplatform.worker.queue.EventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.*;

/**
 * A class that extends Task which is used to handle Retrievals from the vault
 */
public class Retrieve extends Task {
    
    private static final Logger logger = LoggerFactory.getLogger(Retrieve.class);
    
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
        
        EventSender eventStream = (EventSender)context.getEventStream();
        
        logger.info("Retrieve job - performAction()");
        
        Map<String, String> properties = getProperties();
        String depositId = properties.get("depositId");
        String retrieveId = properties.get("retrieveId");
        String bagID = properties.get("bagId");
        String retrievePath = properties.get("retrievePath");
        String archiveId = properties.get("archiveId");
        String userID = properties.get("userId");
        String archiveDigest = properties.get("archiveDigest");
        String archiveDigestAlgorithm = properties.get("archiveDigestAlgorithm");
        
        
        int numOfChunks = Integer.parseInt(properties.get("numOfChunks"));
        
        long archiveSize = Long.parseLong(properties.get("archiveSize"));
        
        Map<Integer, String> chunksDigest = getChunkFilesDigest();
        Map<Integer, String> encChunksDigest = getEncChunksDigest();
        String encTarDigest = getEncTarDigest();

        if (this.isRedeliver()) {
            eventStream.send(new Error(jobID, depositId, "Retrieve stopped: the message had been redelivered, please investigate")
                .withUserId(userID));
            return;
        }
        
        ArrayList<String> states = new ArrayList<>();
        states.add("Computing free space");    // 0
        states.add("Retrieving from archive"); // 1
        states.add("Validating data");         // 2
        states.add("Transferring files");      // 3
        states.add("Data retrieve complete");  // 4
        eventStream.send(new InitStates(jobID, depositId, states)
            .withUserId(userID));
        
        eventStream.send(new RetrieveStart(jobID, depositId, retrieveId)
            .withUserId(userID)
            .withNextState(0));
        
        logger.info("bagID: " + bagID);
        logger.info("retrievePath: " + retrievePath);
        
        Device userFs = null;
        UserStore userStore = null;
        Device archiveFs = null;
        
        for (String storageID : userFileStoreClasses.keySet()) {
            
            String storageClass = userFileStoreClasses.get(storageID);
            Map<String, String> storageProperties = userFileStoreProperties.get(storageID);
            
            // Connect to the first user storage device (we only expect one for a retrieval)
            try {
                Class<?> clazz = Class.forName(storageClass);
                Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
                Object instance = constructor.newInstance(storageClass, storageProperties);                
                userFs = (Device)instance;
                userStore = (UserStore)userFs;
                logger.info("Connected to user store: " + storageID + ", class: " + storageClass);
                break;
            } catch (Exception e) {
                String msg = "Deposit failed: could not access user filesystem";
                logger.error(msg, e);
                eventStream.send(new Error(jobID, depositId, msg)
                    .withUserId(userID));

                System.exit(1);
            }
        }

        // We get passed a list because it is a parameter common to deposits and retrieves, but for retrieve there should only be one.
        ArchiveStore archiveFileStore = archiveFileStores.get(0);

        // Connect to the archive storage
        try {
            Class<?> clazz = Class.forName(archiveFileStore.getStorageClass());
            Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
            Object instance = constructor.newInstance(archiveFileStore.getStorageClass(), archiveFileStore.getProperties());
            archiveFs = (Device)instance;
        } catch (Exception e) {
            String msg = "Retrieve failed: could not access archive filesystem";
            logger.error(msg, e);
            eventStream.send(new Error(jobID, depositId, msg)
                .withUserId(userID));

            System.exit(1);
        }
        
        try {
            if (!userStore.exists(retrievePath) || !userStore.isDirectory(retrievePath)) {
                // Target path must exist and be a directory
                logger.info("Target directory not found!");
            }
            
            // Check that there's enough free space ...
            try {
                long freespace = userFs.getUsableSpace();
                logger.info("Free space: " + freespace + " bytes (" +  FileUtils.byteCountToDisplaySize(freespace) + ")");
                if (freespace < archiveSize) {
                    eventStream.send(new Error(jobID, depositId, "Not enough free space to retrieve data!")
                        .withUserId(userID));
                    return;
                }
            } catch (Exception e) {
                logger.info("Unable to determine free space");
                eventStream.send(new Error(jobID, depositId, "Unable to determine free space")
                    .withUserId(userID));

                System.exit(1);
            }

            // Retrieve the archived data
            String tarFileName = bagID + ".tar";
            
            // Copy the tar file from the archive to the temporary area
            Path tarPath = context.getTempDir().resolve(tarFileName);
            File tarFile = tarPath.toFile();
            
            eventStream.send(new UpdateProgress(jobID, depositId, 0, archiveSize, "Starting transfer ...")
                .withUserId(userID)
                .withNextState(1));
            
            if (archiveFs.hasMultipleCopies()) {
                logger.info("Device has multiple copies");
                Progress progress = new Progress();
                ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, archiveSize, eventStream);
                Thread trackerThread = new Thread(tracker);
                trackerThread.start();

                List<String> locations = archiveFs.getLocations();
                Iterator<String> locationsIt = locations.iterator();
                LOCATION: while (locationsIt.hasNext()) {
                    String location = locationsIt.next();
                    try {
                        try {
                            // NEED TO UPDATE THIS TO INCLUDE CHUNKING STUFF IS TURNED ON
                            if (context.isChunkingEnabled()) {
                                // TODO can bypass this if there is only one chunk.
                                recomposeMulti(numOfChunks, tarFileName, context, archiveId, archiveFs, progress,
                                        archiveDigestAlgorithm, tarFile, chunksDigest, encChunksDigest, location,
                                        depositId);
                            } else {
                                // Ask the driver to copy files to the temp directory
                                archiveFs.retrieve(archiveId, tarFile, progress, location);
                            }
                        } finally {
                            // Stop the tracking thread
                            tracker.stop();
                            trackerThread.join();
                        }

                        logger.info("Attempting retrieve on archive from " + location);
                        this.doRetrieve(depositId, userID, retrievePath, retrieveId, context, userFs, tarFile,
                                eventStream, archiveDigestAlgorithm, archiveDigest, progress);
                        logger.info("Completed retrieve on archive from " + location);
                        break LOCATION;
                    } catch (Exception e) {
                        // if last location has an error throw the error else go
                        // round again
                        // continue LOCATION;
                        if (!locationsIt.hasNext()) {
                            logger.info("All locations had problems throwing exception " + e.getMessage());
                            throw e;
                        }
                        System.exit(1);
                    }
                }

            } else {
                logger.info("Single copy device");
                // Progress tracking (threaded)
                Progress progress = new Progress();
                ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, archiveSize, eventStream);
                Thread trackerThread = new Thread(tracker);
                trackerThread.start();
                

                // Verify integrity with deposit checksum
                String systemAlgorithm = Verify.getAlgorithm();
                if (!systemAlgorithm.equals(archiveDigestAlgorithm)) {
                    throw new Exception("Unsupported checksum algorithm: " + archiveDigestAlgorithm);
                }

                try {
                    // Ask the driver to copy files to the temp directory
                    if (context.isChunkingEnabled()) {
                        // TODO can bypass this if there is only one chunk.
                        recomposeSingle(numOfChunks, tarFileName, context, archiveId, archiveFs, progress,
                                archiveDigestAlgorithm, tarFile, chunksDigest, encChunksDigest, depositId);
                    } else {
                        archiveFs.retrieve(archiveId, tarFile, progress);

                        if (this.getTarIV() != null) {
                            // Decrypt tar file
                            String encTarFileHash = Verify.getDigest(tarFile);
                            
                            logger.info("Encrypted tar Checksum algorithm: " + archiveDigestAlgorithm);
                            logger.info("Encrypted tar Checksum: " + encTarFileHash);
                            
                            if (!encTarFileHash.equals(encTarDigest)) {
                                throw new Exception("checksum failed: " + encTarFileHash + " != " + encTarDigest);
                            }
                            
                            Encryption.decryptFile(context, tarFile, this.getTarIV());
                        }
                    }
                } finally {
                    // Stop the tracking thread
                    tracker.stop();
                    trackerThread.join();
                }

                this.doRetrieve(depositId, userID, retrievePath, retrieveId, context, userFs, tarFile, eventStream, archiveDigestAlgorithm, archiveDigest, progress);
            }
            
        } catch (Exception e) {
            String msg = "Data retrieve failed: " + e.getMessage();
            logger.error(msg, e);
            eventStream.send(new Error(jobID, depositId, msg)
                .withUserId(userID));

            System.exit(1);
        }
    }
    
    private void recomposeSingle(int numOfChunks, String tarFileName, Context context, String archiveId, Device archiveFs, Progress progress, 
            String archiveDigestAlgorithm, File tarFile, Map<Integer, String> chunksDigest, Map<Integer, String> encChunksDigest, 
            String depositId)  
                    throws Exception {
        recompose(numOfChunks, tarFileName, context, archiveId, archiveFs, progress, archiveDigestAlgorithm, 
                tarFile, chunksDigest, encChunksDigest, false, null, depositId);
    }
    
    private void recomposeMulti(int numOfChunks, String tarFileName, Context context, String archiveId, Device archiveFs, Progress progress, 
            String archiveDigestAlgorithm, File tarFile, Map<Integer, String> chunksDigest, Map<Integer, String> encChunksDigest, 
            String location, String depositId)  
                    throws Exception {
        recompose(numOfChunks, tarFileName, context, archiveId, archiveFs, progress, archiveDigestAlgorithm, 
                tarFile, chunksDigest, encChunksDigest, true, location, depositId);
    }
    
    private void recompose(int numOfChunks, String tarFileName, Context context, String archiveId, Device archiveFs, Progress progress, 
            String archiveDigestAlgorithm, File tarFile, Map<Integer, String> chunksDigest, Map<Integer, String> encChunksDigest, 
            boolean singleCopy, String location, String depositId) throws Exception {

        File[] chunks = new File[numOfChunks];
        logger.info("Retrieving " + numOfChunks + " chunk(s)");
        for( int chunkNum = 1; chunkNum <= numOfChunks; chunkNum++) {
            Path chunkPath = context.getTempDir().resolve(tarFileName+FileSplitter.CHUNK_SEPARATOR+chunkNum);
            File chunkFile = chunkPath.toFile();
            String chunkArchiveId = archiveId+FileSplitter.CHUNK_SEPARATOR+chunkNum;
            if (! singleCopy) {
                archiveFs.retrieve(chunkArchiveId, chunkFile, progress);
            } else {
                archiveFs.retrieve(chunkArchiveId, chunkFile, progress, location);
            }
            chunks[chunkNum-1] = chunkFile;
            
            if( this.getChunksIVs().get(chunkNum) != null ) {
                String archivedEncChunkFileHash = encChunksDigest.get(chunkNum);
                
                // Check encrypted file checksum
                String encChunkFileHash = Verify.getDigest(chunkFile);
                
                logger.info("Encrypted chunk Checksum algorithm: " + archiveDigestAlgorithm);
                logger.info("Encrypted Checksum: " + encChunkFileHash);
                
                if (!encChunkFileHash.equals(archivedEncChunkFileHash)) {
                    throw new Exception("checksum failed: " + encChunkFileHash + " != " + archivedEncChunkFileHash);
                }
                
                Encryption.decryptFile(context, chunkFile, this.getChunksIVs().get(chunkNum));
            }
            
            // Check file
            String archivedChunkFileHash = chunksDigest.get(chunkNum);
            
            // TODO: Should we check algorithm each time or assume main tar file algorithm is the same
            // We might also want to move algorythm check before this loop
            String chunkFileHash = Verify.getDigest(chunkFile);
            
            logger.info("Chunk Checksum algorithm: " + archiveDigestAlgorithm);
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
    
    private void doRetrieve(String depositId, String userID, String retrievePath, String retrieveId, Context context, Device userFs, File tarFile, EventSender eventStream, 
            String archiveDigestAlgorithm, String archiveDigest, Progress progress) throws Exception{
        logger.info("Copied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
        
        logger.info("Validating data ...");
        eventStream.send(new UpdateProgress(jobID, depositId).withNextState(2)
            .withUserId(userID));
        
        String tarHash = Verify.getDigest(tarFile);
        logger.info("Checksum algorithm: " + archiveDigestAlgorithm);
        logger.info("Checksum: " + tarHash);
        
        if (!tarHash.equals(archiveDigest)) {
        		logger.info("Checksum failed: " + tarHash + " != " + archiveDigest);
            throw new Exception("checksum failed: " + tarHash + " != " + archiveDigest);
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
        long payloadSize = FileUtils.sizeOfDirectory(payloadDir);

        // Copy the extracted files to the target retrieve area
        logger.info("Copying to user directory ...");
        eventStream.send(new UpdateProgress(jobID, depositId, 0, bagDirSize, "Starting transfer ...")
            .withUserId(userID)
            .withNextState(3));
        
        // Progress tracking (threaded)
        progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, bagDirSize, eventStream);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();

        try {
            ArrayList<File> contents = new ArrayList<File>(Arrays.asList(payloadDir.listFiles()));
            for(File content: contents){
                userFs.store(retrievePath, content, progress);
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



        logger.info("Data retrieve complete: " + retrievePath);
        eventStream.send(new RetrieveComplete(jobID, depositId, retrieveId).withNextState(4)
            .withUserId(userID));
    }
}
