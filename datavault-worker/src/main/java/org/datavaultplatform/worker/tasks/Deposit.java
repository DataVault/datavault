package org.datavaultplatform.worker.tasks;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.worker.operations.*;
import org.datavaultplatform.worker.queue.EventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * A class that extends Task which is used to handle Deposits to the vault
 */
public class Deposit extends Task {

    private static final Logger logger = LoggerFactory.getLogger(Deposit.class);
    private static final Set<String> RESTART_FROM_BEGINNNING = new HashSet<>();
    private static final Set<String> RESTART_FROM_TRANSFER = new HashSet<>();
    private static final Set<String> RESTART_FROM_PACKAGING = new HashSet<>();
    private static final Set<String> RESTART_FROM_TAR_CHECKSUM = new HashSet<>();
    private static final Set<String> RESTART_FROM_ENC_CHECKSUM = new HashSet<>();
    private static final Set<String> RESTART_FROM_CHUNKING = new HashSet<>();
    private static final Set<String> RESTART_FROM_UPLOAD = new HashSet<>();
    private static final Set<String> RESTART_FROM_VALIDATION = new HashSet<>();
    private static final Set<String> RESTART_FROM_COMPLETE = new HashSet<>();
    {
        this.setupRestartHashes();
    }
    EventSender eventStream;
    HashMap<String, UserStore> userStores;

    // todo: I'm sure these two maps could be combined in some way.

    // Maps the model ArchiveStore Id to the storage equivelant
    HashMap<String, ArchiveStore> archiveStores = new HashMap<>();

    // Maps the model ArchiveStore Id to the generated Archive Id
    HashMap<String, String> archiveIds = new HashMap<>();

    String depositId;
    String bagID;
    String userID;

    PackagerV2 packagerV2 = new PackagerV2();
    
    // Chunking attributes
    File[] chunkFiles;
    String[] chunksHash;
    String[] encChunksHash;

    private void setupRestartHashes() {
        Deposit.RESTART_FROM_BEGINNNING.add("org.datavaultplatform.common.event.deposit.Start");

        Deposit.RESTART_FROM_TRANSFER.addAll(RESTART_FROM_BEGINNNING);
        Deposit.RESTART_FROM_TRANSFER.add("org.datavaultplatform.common.event.deposit.ComputedSize");

        Deposit.RESTART_FROM_PACKAGING.addAll(RESTART_FROM_TRANSFER);
        Deposit.RESTART_FROM_PACKAGING.add("org.datavaultplatform.common.event.deposit.TransferComplete");

        Deposit.RESTART_FROM_TAR_CHECKSUM.addAll(RESTART_FROM_PACKAGING);
        Deposit.RESTART_FROM_TAR_CHECKSUM.add("org.datavaultplatform.common.event.deposit.PackageComplete");

        Deposit.RESTART_FROM_CHUNKING.addAll(RESTART_FROM_TAR_CHECKSUM);
        Deposit.RESTART_FROM_CHUNKING.add("org.datavaultplatform.common.event.deposit.ComputedDigest");

        Deposit.RESTART_FROM_ENC_CHECKSUM.addAll(RESTART_FROM_CHUNKING);
        Deposit.RESTART_FROM_ENC_CHECKSUM.add("org.datavaultplatform.common.event.deposit.ComputedChunks");


        Deposit.RESTART_FROM_UPLOAD.addAll(RESTART_FROM_ENC_CHECKSUM);
        Deposit.RESTART_FROM_UPLOAD.add("org.datavaultplatform.common.event.deposit.ComputedEncryption");
        Deposit.RESTART_FROM_UPLOAD.add("org.datavaultplatform.common.event.deposit.StartCopyUpload");
        Deposit.RESTART_FROM_UPLOAD.add("org.datavaultplatform.common.event.deposit.CompleteCopyUpload");

        Deposit.RESTART_FROM_VALIDATION.addAll(RESTART_FROM_UPLOAD);
        Deposit.RESTART_FROM_VALIDATION.add("org.datavaultplatform.common.event.deposit.UploadComplete");
        Deposit.RESTART_FROM_VALIDATION.add("org.datavaultplatform.common.event.deposit.StartChunkValidation");
        Deposit.RESTART_FROM_VALIDATION.add("org.datavaultplatform.common.event.deposit.CompleteChunkValidation");
        Deposit.RESTART_FROM_VALIDATION.add("org.datavaultplatform.common.event.deposit.StartTarValidation");
        Deposit.RESTART_FROM_VALIDATION.add("org.datavaultplatform.common.event.deposit.CompleteTarValidation");

        Deposit.RESTART_FROM_COMPLETE.addAll(RESTART_FROM_VALIDATION);
        Deposit.RESTART_FROM_COMPLETE.add("org.datavaultplatform.common.event.deposit.ValidationComplete");
    }
    /* (non-Javadoc)
     * @see org.datavaultplatform.common.task.Task#performAction(org.datavaultplatform.common.task.Context)
     */
    @Override
    public void performAction(Context context) {
    	this.InitialLogging(context);
        eventStream = (EventSender)context.getEventStream();
        Map<String, String> properties = getProperties();
        depositId = properties.get("depositId");
        bagID = properties.get("bagId");
        userID = properties.get("userId");

        String lastEventClass = (this.getLastEvent() != null) ? this.getLastEvent().getEventClass() : null;
        if (lastEventClass != null) {
            logger.debug("The last event was: " + lastEventClass);
        }
        if (this.isRedeliver()) {
            eventStream.send(new Error(jobID, depositId, "Deposit stopped: the message had been redelivered, please investigate")
                .withUserId(userID));
            return;
        }
        
        this.initStates();
        
        eventStream.send(new Start(jobID, depositId)
            .withUserId(userID)
            .withNextState(0));

        userStores = this.setupUserFileStores();
        this.setupArchiveFileStores();
        DepositTransferHelper uploadHelper = this.initialTransferStep(context,lastEventClass);
        Path bagDataPath = uploadHelper.getBagDataPath();
        File bagDir = uploadHelper.getBagDir();

        try {
            if (lastEventClass == null || RESTART_FROM_TRANSFER.contains(lastEventClass)) {
                this.copyAdditionalUserData(context, bagDataPath);
            } else {
                logger.debug("Last event is: " + lastEventClass + " skipping secondary File copy");
            }

            PackageHelper packageHelper = this.packageStep(context, properties, lastEventClass, bagDir);
            File tarFile = packageHelper.getTarFile();
            String tarHash = packageHelper.getTarHash();
            byte iv[] = packageHelper.getIv();
            String encTarHash = packageHelper.getEncTarHash();
            Map<Integer, byte[]> chunksIVs = packageHelper.getChunksIVs();
            Long archiveSize = packageHelper.getArchiveSize();

            // Copy the resulting tar file to the archive area
            logger.info("Copying tar file(s) to archive ...");

            if (lastEventClass == null || RESTART_FROM_UPLOAD.contains(lastEventClass)) {
                this.uploadToStorage(context, tarFile);
            } else {
                logger.debug("Last event is: " + lastEventClass + " skipping upload");
                if (this.getRestartArchiveIds() != null) {
                    logger.debug("We have restart Archive Ids");
                    archiveIds.putAll(this.getRestartArchiveIds());
                } else {
                    logger.debug("We don't have restart archive Ids");
                }

            }

            logger.info("Verifying archive package ...");
            if (lastEventClass == null || RESTART_FROM_VALIDATION.contains(lastEventClass)) {
                this.verifyArchive(context, tarFile, tarHash, iv, chunksIVs, encTarHash);
            } else {
                logger.debug("Last event is: " + lastEventClass + " skipping validation");
            }

            logger.info("Deposit complete");

            logger.debug("The jobID: " + jobID);
            logger.debug("The depositId: " + depositId);
            logger.debug("The archiveIds:" + archiveIds);
            eventStream.send(new Complete(jobID, depositId, archiveIds, archiveSize)
                .withUserId(userID)
                .withNextState(5));
        } catch (Exception e) {
            String msg = "Deposit failed: " + e.getMessage();
            logger.error(msg, e);
            eventStream.send(new Error(jobID, depositId, msg)
                .withUserId(userID));

            throw new RuntimeException(e);
        }
        
        // TODO: Disconnect from user and archive storage system?
    }
    
    /**
     * @param userStore
     * @param filePath
     * @param bagPath
     * @throws Exception
     */
    private void copyFromUserStorage(UserStore userStore, String filePath, Path bagPath) throws Exception {

        String fileName = userStore.getName(filePath);
        File outputFile = bagPath.resolve(fileName).toFile();
        
        // Compute bytes to copy
        long expectedBytes = userStore.getSize(filePath);
        
        // Display progress bar
        eventStream.send(new UpdateProgress(jobID, depositId, 0, expectedBytes, "Starting transfer ...")
            .withUserId(userID));
        
        logger.info("Size: " + expectedBytes + " bytes (" +  FileUtils.byteCountToDisplaySize(expectedBytes) + ")");
        
        // Progress tracking (threaded)
        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, expectedBytes, eventStream);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();
        
        try {
            // Ask the driver to copy files to our working directory
            logger.debug("CopyFromUserStorage filePath:" + filePath);
            logger.debug("CopyFromUserStorage filePath:" + outputFile.toPath().toString());
            ((Device)userStore).retrieve(filePath, outputFile, progress);
        } finally {
            // Stop the tracking thread
            tracker.stop();
            trackerThread.join();
        }
    }
    
    /**
     * @param tempPath
     * @param userID
     * @param uploadPath
     * @return
     * @throws Exception
     */
    private long getUserUploadsSize(Path tempPath, String userID, String uploadPath) throws Exception {
        // TODO: this is a bit of a hack to escape the per-worker temp directory
        File uploadDir = tempPath.getParent().resolve("uploads").resolve(userID).resolve(uploadPath).toFile();
        if (uploadDir.exists()) {
            logger.info("Calculating size of user uploads directory");
            return FileUtils.sizeOfDirectory(uploadDir);
        }
        return 0;
    }
    
    /**
     * @param tempPath
     * @param bagPath
     * @param userID
     * @param uploadPath
     * @throws Exception
     */
    private void moveFromUserUploads(Path tempPath, Path bagPath, String userID, String uploadPath) throws Exception {
        
        File outputFile = bagPath.resolve("uploads").toFile();
        
        // TODO: this is a bit of a hack to escape the per-worker temp directory
        File uploadDir = tempPath.getParent().resolve("uploads").resolve(userID).resolve(uploadPath).toFile();
        if (uploadDir.exists()) {
            logger.info("Moving user uploads to bag directory");
            FileUtils.moveDirectory(uploadDir, outputFile);
        }
    }
    
    /**
     * @param tarFile
     * @throws Exception
     */
    private void copyToArchiveStorage(File tarFile) throws Exception {
        copyToArchiveStorage(tarFile, 0);
    }
    
    /**
     * @param tarFile
     * @throws Exception
     */
    private void copyToArchiveStorage(File tarFile, int chunkCount) throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<HashMap<String, String>>> futures = new ArrayList();
        for (String archiveStoreId : archiveStores.keySet() ) {
            logger.debug("ArchiveStoreId: " + archiveStoreId);
            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
            // add thread to executor
            // add future to futures list
            DeviceTracker dt = new DeviceTracker();
            dt.setArchiveStore(archiveStore);
            dt.setArchiveStoreId(archiveStoreId);
            dt.setChunkCount(chunkCount);
            dt.setDepositId(depositId);
            dt.setJobID(jobID);
            dt.setEventStream(eventStream);
            dt.setTarFile(tarFile);
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

        // foreach future
        // check for exception
        // append archiveIds from future to global archiveIds
    }
    
    /**
     * @param context
     * @param chunkFiles
     * @param chunksHash
     * @param tarFile
     * @param tarHash
     * @throws Exception
     */
    private void verifyArchive(Context context, File[] chunkFiles, String[] chunksHash, File tarFile, String tarHash, 
            Map<Integer, byte[]> ivs, String[] encChunksHash) throws Exception {

        for (String archiveStoreId : archiveStores.keySet() ) {

            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
            String archiveId = archiveIds.get(archiveStoreId);
            
            if (archiveStore.getVerifyMethod() != Verify.Method.COPY_BACK && archiveStore.getVerifyMethod() != Verify.Method.CLOUD ) {
                throw new Exception("Wrong Verify Method: [" + archiveStore.getVerifyMethod() + "] has to be " + Verify.Method.COPY_BACK);
            }
            
            if (((Device)archiveStore).hasMultipleCopies()) {
                boolean firstArchiveStore = true;
                for (String loc : ((Device)archiveStore).getLocations()) {
                    if (firstArchiveStore || context.isMultipleValidationEnabled() == true) {
                        this.doArchive(context, chunkFiles, chunksHash, tarFile, tarHash, archiveStore, archiveId, loc, true, ivs, null, encChunksHash, firstArchiveStore);
                    }
                    firstArchiveStore = false;
                }
            } else {   
            	if (archiveStore.getVerifyMethod() != Verify.Method.CLOUD) {
            		this.doArchive(context, chunkFiles, chunksHash, tarFile, tarHash, archiveStore, archiveId, ivs, null, encChunksHash, true);
            	} else {
            		this.doArchive();
            	}
            }
        }
    }

    private void doArchive() {
    	logger.info("Skipping verification as a cloud plugin (for now)");
    }

    private void doArchive(Context context, File[] chunkFiles, String[] chunksHash, File tarFile, String tarHash, ArchiveStore archiveStore,
            String archiveId, Map<Integer, byte[]> ivs, String encTarHash, String[] encChunksHash, boolean doVerification) throws Exception {
        this.doArchive(context, chunkFiles, chunksHash, tarFile, tarHash, archiveStore, archiveId, null,  false, ivs, encTarHash, encChunksHash, doVerification);
    }

    /**
     * @param context
     * @param tarFile
     * @param tarHash
     * @throws Exception
     */
    private void verifyArchive(Context context, File tarFile, String tarHash, byte[] iv, String encTarHash) throws Exception {

        boolean alreadyVerified = false;

        for (String archiveStoreId : archiveStores.keySet() ) {
            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
            String archiveId = archiveIds.get(archiveStoreId);

            Verify.Method vm = archiveStore.getVerifyMethod();
            logger.info("Verification method: " + vm);

            logger.debug("verifyArchive - archiveId: "+archiveId);

            // Get the tar file

            if ((vm == Verify.Method.LOCAL_ONLY) && (!alreadyVerified)){

                // Decryption
                if(iv != null) {
                    Encryption.decryptFile(context, tarFile, iv);
                }

                // Verify the contents of the temporary file
                verifyTarFile(context.getTempDir(), tarFile, null);

            } else if (vm == Verify.Method.COPY_BACK) {

                alreadyVerified = true;

                // Delete the existing temporary file
                tarFile.delete();
                // Copy file back from the archive storage
                if (((Device)archiveStore).hasMultipleCopies()) {
                    for (String loc : ((Device)archiveStore).getLocations()) {
                        CopyBackFromArchive.copyBackFromArchive(archiveStore, archiveId, tarFile, loc);

                        // check encrypted tar
                        verifyTarFile(context.getTempDir(), tarFile, encTarHash);

                        // Decryption
                        if(iv != null) {
                            Encryption.decryptFile(context, tarFile, iv);
                        }

                        // Verify the contents
                        verifyTarFile(context.getTempDir(), tarFile, tarHash);
                    }
                } else {
                    CopyBackFromArchive.copyBackFromArchive(archiveStore, archiveId, tarFile);

                    // check encrypted tar
                    String encTarFileHash = Verify.getDigest(tarFile);
                    logger.info("Checksum: " + encTarFileHash);
                    if (!encTarFileHash.equals(encTarFileHash)) {
                        throw new Exception("checksum failed: " + encTarFileHash + " != " + encTarFileHash);
                    }

                    // Decryption
                    if(iv != null) {
                        Encryption.decryptFile(context, tarFile, iv);
                    }

                    // Verify the contents
                    verifyTarFile(context.getTempDir(), tarFile, tarHash);
                }
            } else if (vm == Verify.Method.CLOUD) {
                // do nothing for now but we hope to extend this so that we can compare checksums supplied by the cloud api
                logger.info("Skipping verification as a cloud plugin (for now)");
            }
        }
    }

    private void doArchive(Context context, File[] chunkFiles, String[] chunksHash, File tarFile, String tarHash, ArchiveStore archiveStore,
            String archiveId, HashMap<Integer, byte[]> ivs, String encTarHash, String[] encChunksHash, boolean doVerification) throws Exception {
        this.doArchive(context, chunkFiles, chunksHash, tarFile, tarHash, archiveStore, archiveId, null,  false, ivs, encTarHash, encChunksHash, doVerification);
    }
    
    private void doArchive(Context context, File[] chunkFiles, String[] chunksHash, File tarFile, String tarHash, ArchiveStore archiveStore, 
            String archiveId, String location, boolean multipleCopies, Map<Integer, byte[]> ivs,
            String encTarHash, String[] encChunksHash, boolean doVerification) throws Exception {

        int noOfThreads = context.getNoChunkThreads();
        if (noOfThreads != 0 && noOfThreads < 0 ) {
            noOfThreads = 25;
        }
        logger.debug("Number of threads: " + noOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
        List<Future<HashMap<String, String>>> futures = new ArrayList();
        for (int i = 0; i < chunkFiles.length; i++) {
            // if less that max threads started start new one
            File chunkFile = chunkFiles[i];
            //String chunkHash = chunksHash[i];

            ChunkDownloadTracker cdt = new ChunkDownloadTracker();
            cdt.setArchiveId(archiveId);
            cdt.setArchiveStore(archiveStore);
            cdt.setChunkFile(chunkFile);
            //cdt.setChunkHash(chunkHash);
            cdt.setContext(context);
            cdt.setCount(i);
            cdt.setDoVerification(doVerification);
            cdt.setEncChunksHash(encChunksHash);
            cdt.setIvs(ivs);
            cdt.setLocation(location);
            cdt.setMultipleCopies(multipleCopies);
            logger.debug("Creating chunk download thread:" + i);
            Future<HashMap<String, String>> dtFuture = executor.submit(cdt);
            futures.add(dtFuture);
        }
        executor.shutdown();

        for (Future<HashMap<String, String>> future : futures) {
            try {
                future.get();
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                if (cause instanceof Exception) {
                    logger.info("Chunk download failed. " + cause.getMessage());
                    throw (Exception) cause;
                }
            }
        }


        if(doVerification) {
            FileSplitter.recomposeFile(chunkFiles, tarFile);

            // Verify the contents
            verifyTarFile(context.getTempDir(), tarFile, tarHash);
        }
    }
    
    /**
     * Compare the SHA hash of the passed in tar file and the passed in original hash.
     * 
     * First compare the tar file then the bag then clean up
     * 
     * If the verification fails at either check throw an exception (maybe we could throw separate exceptions here)
     * @param tempPath Path to the temp storage location
     * @param tarFile File 
     * @param origTarHash String representing the orig hash
     * @throws Exception
     */
    private void verifyTarFile(Path tempPath, File tarFile, String origTarHash) throws Exception {

        if (origTarHash != null) {
            logger.info("Get Digest from: " + tarFile.getAbsolutePath());
            // Compare the SHA hash
            String tarHash = Verify.getDigest(tarFile);
            logger.info("Checksum: " + tarHash);
            if (!tarHash.equals(origTarHash)) {
                throw new Exception("checksum failed: " + tarHash + " != " + origTarHash);
            }
        }
        
        // Decompress to the temporary directory
        //File bagDir = Tar.unTar(tarFile, tempPath);
        
        // Validate the bagit directory
        //if (!Packager.validateBag(bagDir)) {
        //    throw new Exception("Bag is invalid");
        //} else {
        //    logger.info("Bag is valid");
        //}
        
        // Cleanup
        logger.info("Cleaning up ...");
        //FileUtils.deleteDirectory(bagDir);
        tarFile.delete();
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
	            eventStream.send(new Error(jobID, depositId, msg).withUserId(userID));
	            throw new RuntimeException(e);
	        }
    	}
    }
    
	private void calculateTotalDepositSize(Context context) {
		// Calculate the total deposit size of selected files
		long depositTotalSize = 0;
		logger.info("Calculating Total Deposit Size");
		for (String filePath : fileStorePaths) {
			String storageID = filePath.substring(0, filePath.indexOf('/'));
			String storagePath = filePath.substring(filePath.indexOf('/') + 1);

			try {
				UserStore userStore = userStores.get(storageID);
				depositTotalSize += userStore.getSize(storagePath);
				logger.info("Total Deposit size currently: " + depositTotalSize);
			} catch (Exception e) {
				String msg = "Deposit failed: could not access user filesystem";
				logger.error(msg, e);
				eventStream.send(new Error(jobID, depositId, msg).withUserId(userID));
				throw new RuntimeException(e);
			}

		}
		
//		depositTotalSize += this.calculateUserUploads(depositTotalSize, context);

		// Store the calculated deposit size
        logger.info("Final Total Deposit size: " + depositTotalSize);
        eventStream.send(new ComputedSize(jobID, depositId, depositTotalSize)
            .withUserId(userID));
	}

	// TO REMOVE
	// wpetit: I'm commenting this out as I don't think we want to keep it.
    // It seems to be necessary when we still had the "Browse" button on the Deposit page
    // But we removed the button and now is cause the Deposit size to be doubled
//	private long calculateUserUploads(long depositSize, Context context) {
//		// Add size of any user uploads
//        try {
//            for (String path : fileUploadPaths) {
//                depositSize += getUserUploadsSize(context.getTempDir(), userID, path);
//            }
//        } catch (Exception e) {
//            String msg = "Deposit failed: could not access user uploads";
//            logger.error(msg, e);
//            eventStream.send(new Error(jobID, depositId, msg).withUserId(userID));
//            throw new RuntimeException(e);
//        }
//
//        return depositSize;
//	}
	
	private HashMap<String, UserStore> setupUserFileStores() {
		userStores = new HashMap<String, UserStore>();
        
        for (String storageID : userFileStoreClasses.keySet()) {
            
            String storageClass = userFileStoreClasses.get(storageID);
            Map<String, String> storageProperties = userFileStoreProperties.get(storageID);
            
            // Connect to the user storage devices
            try {
                Class<?> clazz = Class.forName(storageClass);
                Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
                Object instance = constructor.newInstance(storageClass, storageProperties);                
                Device userFs = (Device)instance;
                UserStore userStore = (UserStore)userFs;
                userStores.put(storageID, userStore);
                logger.info("Connected to user store: " + storageID + ", class: " + storageClass);
            } catch (Exception e) {
                String msg = "Deposit failed: could not access user filesystem";
                logger.error(msg, e);
                e.printStackTrace();
                eventStream.send(new Error(jobID, depositId, msg)
                    .withUserId(userID));

                //System.exit(1);
                throw new RuntimeException(e);
            }
        }
        
        return userStores;
	}
	
	private File createDir(Path path) {
      File dir = path.toFile();
      dir.mkdir();
      
      return dir;
	}
	
	private void copySelectedUserDataToBagDir(Path bagDataPath) {
		 Long depositIndex = 0L;
	        
	        for (String filePath: fileStorePaths) {
	        
	            String storageID = filePath.substring(0, filePath.indexOf('/'));
	            String storagePath = filePath.substring(filePath.indexOf('/')+1);
	            
	            logger.info("Deposit file: " + filePath);
	            logger.info("Deposit storageID: " + storageID);
	            logger.info("Deposit storagePath: " + storagePath);
	            
	            UserStore userStore = userStores.get(storageID);

	            // Retrieve the data straight into the Bag data directory.
	            Path depositPath = bagDataPath;
	            // If there are multiple deposits then create a sub-directory for each one
	            if (fileStorePaths.size() > 1) {
	                depositIndex += 1;
	                depositPath = bagDataPath.resolve(depositIndex.toString());
	                depositPath.toFile().mkdir();
	            }
	            
	            try {
	                if (userStore.exists(storagePath)) {

	                    // Copy the target file to the bag directory
	                    eventStream.send(new UpdateProgress(jobID, depositId)
	                        .withUserId(userID)
	                        .withNextState(1));

	                    logger.info("Copying target to bag directory ...");
	                    copyFromUserStorage(userStore, storagePath, depositPath);
	                    
	                } else {
	                    logger.error("File does not exist.");
	                    eventStream.send(new Error(jobID, depositId, "Deposit failed: file not found")
	                        .withUserId(userID));
	                }
	            } catch (Exception e) {
	                String msg = "Deposit failed: " + e.getMessage();
	                logger.error(msg, e);
	                eventStream.send(new Error(jobID, depositId, msg)
	                    .withUserId(userID));
	                throw new RuntimeException(e);
	            }
	        }
	}
	
	private void createBag(File bagDir, String depositMetadata, String vaultMetadata, String externalMetadata) throws Exception {
		logger.debug("Creating bag in bag dir: " + bagDir.getCanonicalPath());
        packagerV2.createBag(bagDir);
        
        // Add vault/deposit/type metadata to the bag
        packagerV2.addMetadata(bagDir, depositMetadata, vaultMetadata, null, externalMetadata);		
	}
    
	private File createTar(Context context, File bagDir) throws Exception {
		String tarFileName = bagID + ".tar";
		logger.debug("BagDir length: " + bagDir.length());
        Path tarPath = context.getTempDir().resolve(tarFileName);
        File tarFile = tarPath.toFile();
        Tar.createTar(bagDir, tarFile);
        return tarFile;
	}
	
	private HashMap<Integer, String> createChunks(File tarFile, Context context, HashMap<Integer, String> chunksDigest, String tarHashAlgorithm) throws Exception {
		logger.info("Chunking tar file ...");
        chunkFiles = FileSplitter.spliteFile(tarFile, context.getChunkingByteSize());
        chunksHash = new String[chunkFiles.length];
        chunksDigest = new HashMap<Integer, String>();

        int noOfThreads = context.getNoChunkThreads();
        if (noOfThreads != 0 && noOfThreads < 0 ) {
            noOfThreads = 25;
        }

        logger.debug("Number of threads:" + noOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
        List<Future<ChecksumHelper>> futures = new ArrayList();
        for (int i = 0; i < chunkFiles.length; i++){
            File chunk = chunkFiles[i];
            ChecksumTracker ct = new ChecksumTracker();
            ct.setChunk(chunk);
            ct.setChunkCount(i);

            logger.debug("Creating chunk checksum thread:" + i);
            Future<ChecksumHelper> ctFuture = executor.submit(ct);
            futures.add(ctFuture);

//            chunksHash[i] = Verify.getDigest(chunk);
//
//            long chunkSize = chunk.length();
//            logger.info("Chunk file " + i + ": " + chunkSize + " bytes");
//            logger.info("Chunk file location: " + chunk.getAbsolutePath());
//            logger.info("Checksum algorithm: " + tarHashAlgorithm);
//            logger.info("Checksum: " + chunksHash[i]);
//
//            //if (i > (chunkFiles.length / 2)) {
//            //    throw new Exception("Failed during chunking");
//            //}
//            chunksDigest.put(i+1, chunksHash[i]);
        }
        executor.shutdown();

        for (Future<ChecksumHelper> future : futures) {
            try {
                ChecksumHelper result = future.get();
                int i = result.getChunkCount();
                chunksHash[i] = result.getChunkHash();
                chunksDigest.put(i + 1, chunksHash[i]);
                File chunk = result.getChunk();
                long chunkSize = chunk.length();
                logger.info("Chunk file " + i + ": " + chunkSize + " bytes");
                logger.info("Chunk file location: " + chunk.getAbsolutePath());
                logger.info("Checksum algorithm: " + tarHashAlgorithm);
                logger.info("Checksum: " + chunksHash[i]);
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                if (cause instanceof Exception) {
                    logger.info("Chunk encryption failed. " + cause.getMessage());
                    throw (Exception) cause;
                }
            }
        }
        
        if(!context.isEncryptionEnabled()) {
            eventStream.send(new ComputedChunks(jobID, depositId, chunksDigest, tarHashAlgorithm)
                    .withUserId(userID));
        }
        
        logger.info(chunkFiles.length + " chunk files created.");
        return chunksDigest;
        
	}
	
	private HashMap<Integer, byte[]> encryptChunks(Context context, HashMap<Integer, String> chunksDigest, String tarHashAlgorithm) throws Exception {
		HashMap<Integer, String> encChunksDigests = new HashMap<Integer, String>();
		HashMap<Integer, byte[]> chunksIVs = new HashMap<Integer, byte[]>();
        encChunksHash = new String[chunkFiles.length];

        int noOfThreads = context.getNoChunkThreads();
        if (noOfThreads != 0 && noOfThreads < 0 ) {
            noOfThreads = 25;
        }

        logger.debug("Number of threads:" + noOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
        List<Future<EncryptionHelper>> futures = new ArrayList();
        for (int i = 0; i < chunkFiles.length; i++){
            EncryptionTracker et = new EncryptionTracker();
            et.setChunkCount(i);
            et.setChunk(chunkFiles[i]);
            et.setContext(context);
            logger.debug("Creating chunk encryption thread:" + i);
            Future<EncryptionHelper> etFuture = executor.submit(et);
            futures.add(etFuture);
        }
        executor.shutdown();

        for (Future<EncryptionHelper> future : futures) {
            try {
                EncryptionHelper result = future.get();
                int i = result.getChunkCount();
                encChunksHash[i] = result.getEncTarHash();
                encChunksDigests.put(i+1, encChunksHash[i]);
                chunksIVs.put(i+1, result.getIv());

            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                if (cause instanceof Exception) {
                    logger.info("Chunk encryption failed. " + cause.getMessage());
                    throw (Exception) cause;
                }
            }
        }

        
        logger.info(chunkFiles.length + " chunk files encrypted.");
        eventStream.send(new ComputedEncryption(jobID, depositId, chunksIVs, null, 
                context.getEncryptionMode().toString(), null, encChunksDigests,
                chunksDigest, tarHashAlgorithm)
                    .withUserId(userID));
		
        return chunksIVs;		
	}
	
//	private void encrypt(Context context, HashMap<Integer, byte[]> chunksIVs, byte[] iv, String encTarHash, File tarFile, 
//			HashMap<Integer, String> chunksDigest, String tarHashAlgorithm ) throws Exception {
//		if (context.isChunkingEnabled()) {
//        	chunksIVs = this.encryptChunks(context, chunksDigest, tarHashAlgorithm);
//        } else {
//            iv = Encryption.encryptFile(context, tarFile);
//            encTarHash = Verify.getDigest(tarFile);
//            logger.info("Encrypted Tar file: " + tarFile.length() + " bytes");
//            logger.info("Encrypted tar checksum: " + encTarHash);
//            
//            eventStream.send(new ComputedEncryption(jobID, depositId,null,
//                    iv, context.getEncryptionMode().toString(),
//                    encTarHash, null, null, null)
//                        .withUserId(userID));
//        }
//	}
	
	private EncryptionHelper encryptFullTar(Context context, File tarFile) throws Exception {
		EncryptionHelper retVal = new EncryptionHelper();
		byte[] iv = Encryption.encryptFile(context, tarFile);
        String encTarHash = Verify.getDigest(tarFile);
        
        logger.info("Encrypted Tar file: " + tarFile.length() + " bytes");
        logger.info("Encrypted tar checksum: " + encTarHash);
        
        eventStream.send(new ComputedEncryption(jobID, depositId,null,
                iv, context.getEncryptionMode().toString(),
                encTarHash, null, null, null)
                    .withUserId(userID));	
        retVal.setIv(iv);
        retVal.setEncTarHash(encTarHash);
        return retVal;
	}
	
	private void uploadToStorage(Context context, File tarFile) throws Exception {
        logger.debug("Uploading to storage.");

		if ( context.isChunkingEnabled() ) {
            int noOfThreads = context.getNoChunkThreads();
            if (noOfThreads != 0 && noOfThreads < 0 ) {
                noOfThreads = 25;
            }
            logger.debug("Number of threads:" + noOfThreads);
            ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
            List<Future<HashMap<String, String>>> futures = new ArrayList();
    		int chunkCount = 0;
    		// kick of 10 (maybe more) threads at a time?  each thread would kick off 3 threads of their own
    		for (File chunk : chunkFiles){
                // kick of 10 (maybe more) threads at a time?  each thread would kick off 3 threads of their own
                chunkCount++;
                ChunkUploadTracker cut = new ChunkUploadTracker();
                cut.setChunkCount(chunkCount);
                cut.setChunk(chunk);
                cut.setArchiveStores(this.archiveStores);
                cut.setDepositId(this.depositId);
                cut.setEventStream(this.eventStream);
                cut.setJobID(this.jobID);
                cut.setTarFile(tarFile);
                cut.setUserID(this.userID);
                logger.debug("Creating chunk upload thread:" + chunkCount);
                Future<HashMap<String, String>> dtFuture = executor.submit(cut);
                futures.add(dtFuture);
            }
            executor.shutdown();

            for (Future<HashMap<String, String>> future : futures) {
                try {
                    HashMap<String, String> result = future.get();
                    logger.debug("returned archiveIds: " + result);
                    archiveIds.putAll(result);
                    logger.debug("archiveIds: "+archiveIds);
                } catch (ExecutionException ee) {
                    Throwable cause = ee.getCause();
                    if (cause instanceof Exception) {
                        logger.info("Chunk upload failed. " + cause.getMessage());
                        throw (Exception) cause;
                    }
                }
            }

            logger.debug("final archiveIds: "+archiveIds);
		} else {
			copyToArchiveStorage(tarFile);
		}
        eventStream.send(new UploadComplete(jobID, depositId, archiveIds).withUserId(userID));
		eventStream.send(new UpdateProgress(jobID, depositId)
        .withUserId(userID)
        .withNextState(4));
	}
	
	private void verifyArchive(Context context, File tarFile, String tarHash, byte[] iv, Map<Integer, byte[]> chunksIVs, String encTarHash) throws Exception {
		if( context.isChunkingEnabled() ) {
            verifyArchive(context, chunkFiles, chunksHash, tarFile, tarHash, chunksIVs, encChunksHash);
        } else {
            verifyArchive(context, tarFile, tarHash, iv, encTarHash);
        }
        eventStream.send(new ValidationComplete(jobID, depositId).withUserId(userID));

	}
	
	private void InitialLogging(Context context) {
		logger.info("Deposit job - performAction()");
        logger.info("chunking: "+context.isChunkingEnabled());
        logger.info("chunks byte size: "+context.getChunkingByteSize());
        logger.info("encryption: "+context.isEncryptionEnabled());
        logger.info("encryption mode: "+context.getEncryptionMode());
        logger.info("validate multiple: " + context.isMultipleValidationEnabled());
	}
	
	private void initStates() {
	      ArrayList<String> states = new ArrayList<>();
	      states.add("Calculating size");     // 0
	      states.add("Transferring");         // 1
	      states.add("Packaging");            // 2
	      states.add("Storing in archive");   // 3
	      states.add("Verifying");            // 4
	      states.add("Complete");             // 5
	      eventStream.send(new InitStates(jobID, depositId, states)
	          .withUserId(userID));		
	}
	
	private void copyAdditionalUserData(Context context, Path bagDataPath) throws Exception {
		// Add any directly uploaded files (direct move from temp dir)            
        for (String path : fileUploadPaths) {
            // Retrieve the data straight into the Bag data directory.
            Path depositPath = bagDataPath;
            moveFromUserUploads(context.getTempDir(), depositPath, userID, path);
        }
        
        // Bag the directory in-place
        eventStream.send(new TransferComplete(jobID, depositId)
            .withUserId(userID)
            .withNextState(2));
	}

	private DepositTransferHelper initialTransferStep(Context context, String lastEventClass) {
        DepositTransferHelper retVal = new DepositTransferHelper();
        if (lastEventClass == null || RESTART_FROM_TRANSFER.contains(lastEventClass)) {
            logger.info("bagID: " + bagID);
            this.calculateTotalDepositSize(context);

            // Create a new directory based on the broker-generated UUID
            Path bagPath = context.getTempDir().resolve(bagID);
            logger.debug("The is the bagPath " + bagPath.toString());
            retVal.setBagDataPath(bagPath.resolve("data"));
            logger.debug("The is the bagDataPath " + retVal.getBagDataPath().toString());
            retVal.setBagDir(this.createDir(bagPath));
            this.createDir(retVal.getBagDataPath());

            this.copySelectedUserDataToBagDir(retVal.getBagDataPath());
        } else {
            logger.debug("Last event is: " + lastEventClass + " skipping initial File copy");
            Path bagPath = context.getTempDir().resolve(bagID);
            logger.debug("Setting bag path to: " + bagPath.toString());
            logger.debug("Setting bag data path to: " + bagPath.resolve("data").toString());
            retVal.setBagDataPath(bagPath.resolve("data"));
            retVal.setBagDir(bagPath.toFile());
            // depending on how far the previous attempt got this dir
            // may have been deleted
            this.createDir(retVal.getBagDataPath());
        }

        return retVal;
    }

    private PackageHelper packageStep(Context context, Map<String, String> properties, String lastEventClass, File bagDir
        )  throws Exception {
        PackageHelper retVal = new PackageHelper();

        if (lastEventClass == null || RESTART_FROM_PACKAGING.contains(lastEventClass) || RESTART_FROM_TAR_CHECKSUM.contains(lastEventClass)
            || RESTART_FROM_CHUNKING.contains(lastEventClass) || RESTART_FROM_ENC_CHECKSUM.contains(lastEventClass)) {
            retVal = this.doPackageStep(context, properties, bagDir, retVal, lastEventClass);

        } else {
            int numOfChunks =  0;
            if (properties.get("numOfChunks") != null) {
                numOfChunks = Integer.parseInt(properties.get("numOfChunks"));
            }
            String archiveDigest = null;
            if (properties.get("archiveDigest") != null) {
                archiveDigest = properties.get("archiveDigest");
            }
            retVal = this.skipPackageStep(context, lastEventClass, numOfChunks, archiveDigest, retVal);
        }

        return retVal;
    }

    private PackageHelper doPackageStep(Context context, Map<String, String> properties, File bagDir, PackageHelper retVal, String lastEventClass)  throws Exception {
        String tarHashAlgorithm = Verify.getAlgorithm();
        if (lastEventClass == null || RESTART_FROM_PACKAGING.contains(lastEventClass)) {
            logger.info("Creating bag ...");
            this.createBag(bagDir, properties.get("depositMetadata"), properties.get("vaultMetadata"), properties.get("externalMetadata"));
            // Tar the bag directory
            logger.info("Creating tar file ...");

            retVal.setTarFile(this.createTar(context, bagDir));
            logger.info("Tar file: " + retVal.getArchiveSize() + " bytes");
            logger.info("Tar file location: " + retVal.getTarFile().getAbsolutePath());

            eventStream.send(new PackageComplete(jobID, depositId)
                    .withUserId(userID)
                    .withNextState(3));
        } else {
            logger.debug("Last event is: " + lastEventClass + " skipping actual packaging");
            this.skipActualPackaging(context, retVal);
        }

        if (lastEventClass == null || RESTART_FROM_TAR_CHECKSUM.contains(lastEventClass)) {
            retVal.setTarHash(Verify.getDigest(retVal.getTarFile()));
            retVal.setArchiveSize(retVal.getTarFile().length());
            logger.info("Checksum algorithm: " + tarHashAlgorithm);
            logger.info("Checksum: " + retVal.getTarHash());
            eventStream.send(new ComputedDigest(jobID, depositId, retVal.getTarHash(), tarHashAlgorithm)
                    .withUserId(userID));
        } else {
            logger.debug("Last event is: " + lastEventClass + " skipping tar checksum");
            this.skipTarChecksum(properties.get("archiveDigest"), retVal);
        }

        logger.info("We have successfully created the Tar, so lets delete the Bag to save space");
        FileUtils.deleteDirectory(bagDir);
        HashMap<Integer, String> chunksDigest = null;
        if ((lastEventClass == null || RESTART_FROM_CHUNKING.contains(lastEventClass)) && context.isChunkingEnabled()) {
            chunksDigest = this.createChunks(retVal.getTarFile(), context, chunksDigest, tarHashAlgorithm);
        } else {
            logger.debug("Last event is: " + lastEventClass + " skipping chunking");
            int numOfChunks =  0;
            if (properties.get("numOfChunks") != null) {
                numOfChunks = Integer.parseInt(properties.get("numOfChunks"));
            }
            this.skipChunking(numOfChunks, context, retVal);
        }

        // Encryption
        if ((lastEventClass == null || RESTART_FROM_ENC_CHECKSUM.contains(lastEventClass)) && context.isEncryptionEnabled()) {
            logger.info("Encrypting file(s)...");
            if (context.isChunkingEnabled()) {
                retVal.setChunksIVs(this.encryptChunks(context, chunksDigest, tarHashAlgorithm));
            } else {
                EncryptionHelper helper = this.encryptFullTar(context, retVal.getTarFile());
                retVal.setIv(helper.getIv());
                retVal.setEncTarHash(helper.getEncTarHash());
            }
        } else {
            logger.debug("Last event is: " + lastEventClass + " skipping enc checksum");
        }

        return retVal;
    }

    /**********************************************************
    // we are skipping packaging due to a restart
    // we need to fill in some blanks for the later steps to be
    // able to complete
     **********************************************************/
    private PackageHelper skipPackageStep(Context context, String lastEventClass, int numOfChunks, String archiveDigest,
                                          PackageHelper retVal)  {
        logger.debug("Last event is: " + lastEventClass + " skipping packaging");
        retVal = this.skipChunking(numOfChunks, context, retVal);
        retVal.setChunksIVs(this.chunksIVs);
        retVal = this.skipActualPackaging(context, retVal);
        retVal = this.skipTarChecksum(archiveDigest, retVal);

        return retVal;
    }

    private PackageHelper skipActualPackaging(Context context, PackageHelper retVal) {

        String tarFileName = bagID + ".tar";
        Path tarPath = context.getTempDir().resolve(tarFileName);
        retVal.setTarFile(tarPath.toFile());

        return retVal;
    }

    private PackageHelper skipTarChecksum(String archiveDigest, PackageHelper retVal) {

        retVal.setTarHash(archiveDigest);
        retVal.setArchiveSize(retVal.getTarFile().length());

        return retVal;
    }

    private PackageHelper skipChunking(int numOfChunks, Context context, PackageHelper retVal) {

        this.chunkFiles = new File[numOfChunks];
        for (int i = 0; i < numOfChunks; i++) {
            String chunkFileName = bagID + ".tar." + (i + 1);
            Path chunkPath = context.getTempDir().resolve(chunkFileName);
            logger.debug("Mocked chunk file path: " + chunkPath.toAbsolutePath().toString());
            this.chunkFiles[i] = chunkPath.toFile();
        }

        return retVal;
    }
}