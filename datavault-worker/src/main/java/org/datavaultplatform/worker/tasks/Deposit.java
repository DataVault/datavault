package org.datavaultplatform.worker.tasks;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.EventSender;
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
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.worker.operations.*;
import org.datavaultplatform.worker.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * A class that extends Task which is used to handle Deposits to the vault
 */
public class Deposit extends Task {

    private static final Logger logger = LoggerFactory.getLogger(Deposit.class);
    private static final Set<String> RESTART_FROM_BEGINNING = new HashSet<>();
    private static final Set<String> RESTART_FROM_TRANSFER = new HashSet<>();
    private static final Set<String> RESTART_FROM_PACKAGING = new HashSet<>();
    private static final Set<String> RESTART_FROM_TAR_CHECKSUM = new HashSet<>();
    private static final Set<String> RESTART_FROM_ENC_CHECKSUM = new HashSet<>();
    private static final Set<String> RESTART_FROM_CHUNKING = new HashSet<>();
    private static final Set<String> RESTART_FROM_UPLOAD = new HashSet<>();
    private static final Set<String> RESTART_FROM_VALIDATION = new HashSet<>();
    private static final Set<String> RESTART_FROM_COMPLETE = new HashSet<>();
    static {
        setupRestartHashes();
    }
    private EventSender eventSender;
    private HashMap<String, UserStore> userStores;

    // todo: I'm sure these two maps could be combined in some way.

    // Maps the model ArchiveStore ID to the storage equivalent
    private final HashMap<String, ArchiveStore> archiveStores = new HashMap<>();

    // Maps the model ArchiveStore ID to the generated Archive ID
    private final HashMap<String, String> archiveIds = new HashMap<>();

    private String depositId;
    private String bagID;
    private String userID;

    private final PackagerV2 packagerV2 = new PackagerV2();
    
    // Chunking attributes
    private File[] chunkFiles;
    private String[] chunksHash;
    private String[] encChunksHash;

    private static void setupRestartHashes() {
        RESTART_FROM_BEGINNING.add("org.datavaultplatform.common.event.deposit.Start");

        RESTART_FROM_TRANSFER.addAll(RESTART_FROM_BEGINNING);
        RESTART_FROM_TRANSFER.add("org.datavaultplatform.common.event.deposit.ComputedSize");

        RESTART_FROM_PACKAGING.addAll(RESTART_FROM_TRANSFER);
        RESTART_FROM_PACKAGING.add("org.datavaultplatform.common.event.deposit.TransferComplete");

        RESTART_FROM_TAR_CHECKSUM.addAll(RESTART_FROM_PACKAGING);
        RESTART_FROM_TAR_CHECKSUM.add("org.datavaultplatform.common.event.deposit.PackageComplete");

        RESTART_FROM_CHUNKING.addAll(RESTART_FROM_TAR_CHECKSUM);
        RESTART_FROM_CHUNKING.add("org.datavaultplatform.common.event.deposit.ComputedDigest");

        RESTART_FROM_ENC_CHECKSUM.addAll(RESTART_FROM_CHUNKING);
        RESTART_FROM_ENC_CHECKSUM.add("org.datavaultplatform.common.event.deposit.ComputedChunks");


        RESTART_FROM_UPLOAD.addAll(RESTART_FROM_ENC_CHECKSUM);
        RESTART_FROM_UPLOAD.add("org.datavaultplatform.common.event.deposit.ComputedEncryption");
        RESTART_FROM_UPLOAD.add("org.datavaultplatform.common.event.deposit.StartCopyUpload");
        RESTART_FROM_UPLOAD.add("org.datavaultplatform.common.event.deposit.CompleteCopyUpload");

        RESTART_FROM_VALIDATION.addAll(RESTART_FROM_UPLOAD);
        RESTART_FROM_VALIDATION.add("org.datavaultplatform.common.event.deposit.UploadComplete");
        RESTART_FROM_VALIDATION.add("org.datavaultplatform.common.event.deposit.StartChunkValidation");
        RESTART_FROM_VALIDATION.add("org.datavaultplatform.common.event.deposit.CompleteChunkValidation");
        RESTART_FROM_VALIDATION.add("org.datavaultplatform.common.event.deposit.StartTarValidation");
        RESTART_FROM_VALIDATION.add("org.datavaultplatform.common.event.deposit.CompleteTarValidation");

        RESTART_FROM_COMPLETE.addAll(RESTART_FROM_VALIDATION);
        RESTART_FROM_COMPLETE.add("org.datavaultplatform.common.event.deposit.ValidationComplete");
    }
    /* (non-Javadoc)
     * @see org.datavaultplatform.common.task.Task#performAction(org.datavaultplatform.common.task.Context)
     */
    @Override
    public void performAction(Context context) {
        this.InitialLogging(context);
        eventSender = context.getEventSender();
        Map<String, String> properties = getProperties();
        depositId = properties.get(PropNames.DEPOSIT_ID);
        bagID = properties.get(PropNames.BAG_ID);
        userID = properties.get(PropNames.USER_ID);

        String lastEventClass = (this.getLastEvent() != null) ? this.getLastEvent().getEventClass() : null;
        if (lastEventClass != null) {
            logger.debug("The last event was: " + lastEventClass);
        }
        if (this.isRedeliver()) {
            eventSender.send(new Error(jobID, depositId, "Deposit stopped: the message had been redelivered, please investigate")
                .withUserId(userID));
            return;
        }
        
        this.initStates();
        
        eventSender.send(new Start(jobID, depositId)
            .withUserId(userID)
            .withNextState(0));

        StorageClassNameResolver resolver = context.getStorageClassNameResolver();
        userStores = this.setupUserFileStores(resolver);
        this.setupArchiveFileStores(resolver);
        DepositTransferHelper uploadHelper = this.initialTransferStep(context,lastEventClass);
        Path bagDataPath = uploadHelper.bagDataPath();
        File bagDir = uploadHelper.bagDir();

        try {
            if (lastEventClass == null || RESTART_FROM_TRANSFER.contains(lastEventClass)) {
                this.copyAdditionalUserData(context, bagDataPath);
            } else {
                logger.debug("Last event is: " + lastEventClass + " skipping secondary File copy");
            }

            PackageHelper packageHelper = this.packageStep(context, properties, lastEventClass, bagDir);
            File tarFile = packageHelper.getTarFile();
            String tarHash = packageHelper.getTarHash();
            byte[] iv = packageHelper.getIv();
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

            logger.info("Deposit ID [{}] complete", depositId);

            logger.debug("The jobID: " + jobID);
            logger.debug("The depositId: " + depositId);
            logger.debug("The archiveIds:" + archiveIds);
            eventSender.send(new Complete(jobID, depositId, archiveIds, archiveSize)
                .withUserId(userID)
                .withNextState(5));
        } catch (Exception e) {
            String msg = "Deposit failed: " + e.getMessage();
            logger.error(msg, e);
            eventSender.send(new Error(jobID, depositId, msg)
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
        eventSender.send(new UpdateProgress(jobID, depositId, 0, expectedBytes, "Starting transfer ...")
            .withUserId(userID));
        
        logger.info("Size: " + expectedBytes + " bytes (" +  FileUtils.byteCountToDisplaySize(expectedBytes) + ")");
        
        // Progress tracking (threaded)
        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, expectedBytes, eventSender);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();

        try {
            // Ask the driver to copy files to our working directory
            logger.debug("CopyFromUserStorage filePath:" + filePath);
            logger.debug("CopyFromUserStorage outputFile:" + outputFile.getAbsolutePath());
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
     */
    private long getUserUploadsSize(Path tempPath, String userID, String uploadPath) {
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
        copyToArchiveStorage(tarFile, Optional.empty());
    }
    
    /**
     * @param tarFile
     * @throws Exception
     */
    private void copyToArchiveStorage(File tarFile, Optional<Integer> optChunkNumber) throws Exception {

        TaskExecutor<HashMap<String,String>> executor = new TaskExecutor<>(2, "Device upload failed.");
        for (String archiveStoreId : archiveStores.keySet() ) {
            logger.debug("ArchiveStoreId: " + archiveStoreId);
            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
            // add task to executor
            // add future to futures list
            DeviceTracker dt = new DeviceTracker(archiveStore, archiveStoreId,
                optChunkNumber, depositId,
                jobID, eventSender,
                tarFile, userID);
            logger.debug("Creating device task:" + archiveStore.getClass());
            executor.add(dt);
        }

        executor.execute(archiveIds::putAll);
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
                    if (firstArchiveStore || context.isMultipleValidationEnabled()) {
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
                    Utils.checkFileHash("enc-tar", tarFile, encTarHash);

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
        if (noOfThreads < 0 ) {
            noOfThreads = 25;
        }
        logger.debug("Number of threads: " + noOfThreads);
        TaskExecutor<Object> executor = new TaskExecutor<>(noOfThreads, "Chunk download failed.");
        for (int i = 0; i < chunkFiles.length; i++) {
            int chunkNumber = i + 1;
            // if less that max threads started, start new one
            File chunkFile = chunkFiles[i];
            //String chunkHash = chunksHash[i];

            ChunkDownloadTracker cdt = new ChunkDownloadTracker(
                archiveId, archiveStore,
                chunkFile, context,
                chunkNumber, doVerification,
                encChunksHash, ivs,
                location, multipleCopies);
            logger.debug("Creating chunk download task:" + chunkNumber);
            executor.add(cdt);
        }
        executor.execute(result -> {});


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

        Utils.checkFileHash("tar", tarFile, origTarHash);
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
    
    private void setupArchiveFileStores(StorageClassNameResolver resolver) {
    	// Connect to the archive storage(s). Look out! There are two classes called archiveStore.
    	for (org.datavaultplatform.common.model.ArchiveStore archiveFileStore : archiveFileStores ) {
    		try {
            ArchiveStore archiveStore = StorageClassUtils.createStorage(
                archiveFileStore.getStorageClass(),
                archiveFileStore.getProperties(),
                ArchiveStore.class,
                resolver);
            archiveStores.put(archiveFileStore.getID(), archiveStore);

        } catch (Exception e) {
    			String msg = "Deposit failed: could not access archive filesystem : " + archiveFileStore.getStorageClass();
	            logger.error(msg, e);
	            eventSender.send(new Error(jobID, depositId, msg).withUserId(userID));
	            throw new RuntimeException(e);
	        }
    	}
    }
    
	private void calculateTotalDepositSize(Context context) {
		// Calculate the total deposit size of selected files
		long depositTotalSize = 0;

        if (fileStorePaths != null && ! fileStorePaths.isEmpty()) {
            for (String filePath : fileStorePaths) {
                String storageID = filePath.substring(0, filePath.indexOf('/'));
                String storagePath = filePath.substring(filePath.indexOf('/') + 1);

                try {
                    UserStore userStore = userStores.get(storageID);
                    long fileSize = userStore.getSize(storagePath);
                    //logger.info("FileSize = '" + fileSize + "'");
                    //if (fileSize == 0) {
                    //    String msg = "Deposit failed: file size is 0";
                    //    logger.error(msg);
                    //    eventSender.send(new Error(jobID, depositId, msg).withUserId(userID));
                    //    throw new RuntimeException(new Exception(msg));
                    //}
                    depositTotalSize += fileSize;

                } catch (Exception e) {
                    String msg = "Deposit failed: could not access user filesystem";
                    logger.error(msg, e);
                    eventSender.send(new Error(jobID, depositId, msg).withUserId(userID));
                    throw new RuntimeException(e);
                }

            }

//		depositTotalSize += this.calculateUserUploads(depositTotalSize, context);

            // Store the calculated deposit size
            eventSender.send(new ComputedSize(jobID, depositId, depositTotalSize)
                    .withUserId(userID));
        } else {
            String msg = "Deposit failed: could not access local user filesystem";
            Exception e = new Exception(msg);
            logger.error(msg);
            eventSender.send(new Error(jobID, depositId, msg).withUserId(userID));
            throw new RuntimeException(e);
        }
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
//            eventSender.send(new Error(jobID, depositId, msg).withUserId(userID));
//            throw new RuntimeException(e);
//        }
//
//        return depositSize;
//	}
	
	private HashMap<String, UserStore> setupUserFileStores(StorageClassNameResolver resolver) {
		userStores = new HashMap<>();
        
        for (String storageID : userFileStoreClasses.keySet()) {
            
            String storageClass = userFileStoreClasses.get(storageID);
            Map<String, String> storageProperties = userFileStoreProperties.get(storageID);
            
            // Connect to the user storage devices
            try {
                UserStore userStore = StorageClassUtils.createStorage(
                    storageClass,
                    storageProperties,
                    UserStore.class,
                    resolver);
                userStores.put(storageID, userStore);
                logger.info("Connected to user store: " + storageID + ", class: " + storageClass);
            } catch (Exception e) {
                String msg = "Deposit failed: could not access user filesystem";
                logger.error(msg, e);
                eventSender.send(new Error(jobID, depositId, msg)
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
		 long depositIndex = 0L;
	        
	        for (String filePath: fileStorePaths) {
	        
	            String storageID = filePath.substring(0, filePath.indexOf('/'));
	            String storagePath = filePath.substring(filePath.indexOf('/')+1);
	            
	            logger.info("Deposit file: " + filePath);
	            logger.info("Deposit storageID: " + storageID);
	            logger.info("Deposit storagePath: " + storagePath);
	            
	            UserStore userStore = userStores.get(storageID);

	            // Retrieve the data straight into the Bag data directory.
	            Path depositPath = bagDataPath;
	            // If there are multiple deposits then create a subdirectory for each one
	            if (fileStorePaths.size() > 1) {
	                depositIndex += 1;
	                depositPath = bagDataPath.resolve(Long.toString(depositIndex));
	                depositPath.toFile().mkdir();
	            }
	            
	            try {
	                if (userStore.exists(storagePath)) {

	                    // Copy the target file to the bag directory
	                    eventSender.send(new UpdateProgress(jobID, depositId)
	                        .withUserId(userID)
	                        .withNextState(1));

	                    logger.info("Copying target to bag directory ...");
	                    copyFromUserStorage(userStore, storagePath, depositPath);
	                    
	                } else {
	                    logger.error("File does not exist.");
	                    eventSender.send(new Error(jobID, depositId, "Deposit failed: file not found")
	                        .withUserId(userID));
	                }
	            } catch (Exception e) {
	                String msg = "Deposit failed: " + e.getMessage();
	                logger.error(msg, e);
	                eventSender.send(new Error(jobID, depositId, msg)
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
	
	private HashMap<Integer, String> createChunks(File tarFile, Context context, String tarHashAlgorithm) throws Exception {
      logger.info("Chunking tar file ...");
      chunkFiles = FileSplitter.splitFile(tarFile, context.getChunkingByteSize());
      chunksHash = new String[chunkFiles.length];
      HashMap<Integer, String> chunksDigest = new HashMap<>();

      int noOfThreads = context.getNoChunkThreads();
        if (noOfThreads < 0 ) {
            noOfThreads = 25;
        }

        logger.debug("Number of threads:" + noOfThreads);
        TaskExecutor<ChecksumHelper> executor = new TaskExecutor<>(noOfThreads,"Chunk encryption failed.");
        for (int i = 0; i < chunkFiles.length; i++){
            int chunkNumber = i + 1;
            File chunk = chunkFiles[i];
            ChecksumTracker ct = new ChecksumTracker(chunk, chunkNumber);

            logger.debug("Creating chunk checksum task:" + chunkNumber);
            executor.add(ct);

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

      executor.execute( result -> {
            int chunkNumber = result.chunkNumber();
            int i = chunkNumber - 1;
            chunksHash[i] = result.chunkHash();
            chunksDigest.put(chunkNumber, chunksHash[i]);
            File chunk = result.chunk();
            long chunkSize = chunk.length();
            logger.info("Chunk file " + chunkNumber + ": " + chunkSize + " bytes");
            logger.info("Chunk file location: " + chunk.getAbsolutePath());
            logger.info("Checksum algorithm: " + tarHashAlgorithm);
            logger.info("Checksum: " + chunksHash[i]);
        });

        if(!context.isEncryptionEnabled()) {
            eventSender.send(new ComputedChunks(jobID, depositId, chunksDigest, tarHashAlgorithm)
                    .withUserId(userID));
        }
        
        logger.info(chunkFiles.length + " chunk files created.");
        return chunksDigest;
        
	}
	
	private HashMap<Integer, byte[]> encryptChunks(Context context, HashMap<Integer, String> chunksDigest, String tarHashAlgorithm) throws Exception {
		HashMap<Integer, String> encChunksDigests = new HashMap<>();
		HashMap<Integer, byte[]> chunksIVs = new HashMap<>();
        encChunksHash = new String[chunkFiles.length];

        int noOfThreads = context.getNoChunkThreads();
        if (noOfThreads < 0 ) {
            noOfThreads = 25;
        }

        logger.debug("Number of threads:" + noOfThreads);

        TaskExecutor<EncryptionChunkHelper> executor = new TaskExecutor<>(noOfThreads, "Chunk encryption failed.");
        for (int i = 0; i < chunkFiles.length; i++){
            int chunkNumber = i + 1;
            EncryptionTracker et = new EncryptionTracker(chunkNumber, chunkFiles[i], context);
            logger.debug("Creating chunk encryption task:" + chunkNumber);
            executor.add(et);
        }

        executor.execute(result -> {
            int chunkNumber = result.getChunkNumber();
            int i = chunkNumber - 1;
            encChunksHash[i] = result.getEncTarHash();
            encChunksDigests.put(chunkNumber, encChunksHash[i]);
            chunksIVs.put(chunkNumber, result.getIv());
        });

        
        logger.info(chunkFiles.length + " chunk files encrypted.");
        eventSender.send(new ComputedEncryption(jobID, depositId, chunksIVs, null, 
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
//            eventSender.send(new ComputedEncryption(jobID, depositId,null,
//                    iv, context.getEncryptionMode().toString(),
//                    encTarHash, null, null, null)
//                        .withUserId(userID));
//        }
//	}
	
	private EncryptionHelper encryptFullTar(Context context, File tarFile) throws Exception {

      byte[] iv = Encryption.encryptFile(context, tarFile);
      String encTarHash = Verify.getDigest(tarFile);

      logger.info("Encrypted Tar file: " + tarFile.length() + " bytes");
      logger.info("Encrypted tar checksum: " + encTarHash);

      eventSender.send(new ComputedEncryption(jobID, depositId, null,
          iv, context.getEncryptionMode().toString(),
          encTarHash, null, null, null)
          .withUserId(userID));
      EncryptionHelper retVal = new EncryptionHelper(iv, encTarHash);
      return retVal;
  }

	private void uploadToStorage(Context context, File tarFile) throws Exception {
        logger.debug("Uploading to storage.");

		if ( context.isChunkingEnabled() ) {
        int noOfThreads = context.getNoChunkThreads();
        if (noOfThreads < 0 ) {
            noOfThreads = 25;
        }
        logger.debug("Number of threads:" + noOfThreads);
        TaskExecutor<HashMap<String, String>> executor = new TaskExecutor<>(noOfThreads, "Chunk upload failed.");

    		// kick of 10 (maybe more) tasks at a time?  each task would kick off 3 tasks of their own
        for (int i = 0; i < chunkFiles.length; i++) {
            int chunkNumber = i + 1;
            File chunk = chunkFiles[i];
            // kick of 10 (maybe more) tasks at a time?  each task would kick off 3 tasks of their own
            ChunkUploadTracker cut = new ChunkUploadTracker(chunkNumber, chunk,
                archiveStores, depositId,
                eventSender, jobID,
                tarFile, userID);
            logger.debug("Creating chunk upload task:" + chunkNumber);
            executor.add(cut);
        }

        executor.execute(result -> {
            logger.debug("returned archiveIds: " + result);
            archiveIds.putAll(result);
            logger.debug("archiveIds: " + archiveIds);
        });

        logger.debug("final archiveIds: " + archiveIds);
		} else {
			copyToArchiveStorage(tarFile);
		}
    eventSender.send(new UploadComplete(jobID, depositId, archiveIds).withUserId(userID));
		eventSender.send(new UpdateProgress(jobID, depositId)
        .withUserId(userID)
        .withNextState(4));
	}
	
	private void verifyArchive(Context context, File tarFile, String tarHash, byte[] iv, Map<Integer, byte[]> chunksIVs, String encTarHash) throws Exception {
		if( context.isChunkingEnabled() ) {
        verifyArchive(context, chunkFiles, chunksHash, tarFile, tarHash, chunksIVs, encChunksHash);
    } else {
        verifyArchive(context, tarFile, tarHash, iv, encTarHash);
    }
    eventSender.send(new ValidationComplete(jobID, depositId).withUserId(userID));

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
	      eventSender.send(new InitStates(jobID, depositId, states)
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
        eventSender.send(new TransferComplete(jobID, depositId)
            .withUserId(userID)
            .withNextState(2));
	}

	private DepositTransferHelper initialTransferStep(Context context, String lastEventClass) {
        final DepositTransferHelper retVal;
        if (lastEventClass == null || RESTART_FROM_TRANSFER.contains(lastEventClass)) {
            logger.info("bagID: " + bagID);
            this.calculateTotalDepositSize(context);

            // Create a new directory based on the broker-generated UUID
            Path bagPath = context.getTempDir().resolve(bagID);
            logger.debug("The is the bagPath " + bagPath);
            retVal = new DepositTransferHelper(bagPath.resolve("data"), this.createDir(bagPath));
            logger.debug("The is the bagDataPath " + retVal.bagDataPath().toString());

            this.createDir(retVal.bagDataPath());

            this.copySelectedUserDataToBagDir(retVal.bagDataPath());
        } else {
            logger.debug("Last event is: " + lastEventClass + " skipping initial File copy");
            Path bagPath = context.getTempDir().resolve(bagID);
            logger.debug("Setting bag path to: " + bagPath);
            logger.debug("Setting bag data path to: " + bagPath.resolve("data"));
            retVal = new DepositTransferHelper(bagPath.resolve("data"), bagPath.toFile());

            // depending on how far the previous attempt got this dir
            // may have been deleted
            this.createDir(retVal.bagDataPath());
        }

        return retVal;
    }

    private PackageHelper packageStep(Context context, Map<String, String> properties,
        String lastEventClass, File bagDir)  throws Exception {
        final PackageHelper retVal = new PackageHelper();

        if (lastEventClass == null || RESTART_FROM_PACKAGING.contains(lastEventClass) || RESTART_FROM_TAR_CHECKSUM.contains(lastEventClass)
            || RESTART_FROM_CHUNKING.contains(lastEventClass) || RESTART_FROM_ENC_CHECKSUM.contains(lastEventClass)) {
            this.doPackageStep(context, properties, bagDir, retVal, lastEventClass);

        } else {
            int numOfChunks =  0;
            if (properties.get(PropNames.NUM_OF_CHUNKS) != null) {
                numOfChunks = Integer.parseInt(properties.get(PropNames.NUM_OF_CHUNKS));
            }
            String archiveDigest = null;
            if (properties.get(PropNames.ARCHIVE_DIGEST) != null) {
                archiveDigest = properties.get(PropNames.ARCHIVE_DIGEST);
            }
            this.skipPackageStep(context, lastEventClass, numOfChunks, archiveDigest, retVal);
        }

        return retVal;
    }

    private void doPackageStep(Context context, Map<String, String> properties, File bagDir, PackageHelper packageHelper, String lastEventClass)  throws Exception {
        String tarHashAlgorithm = Verify.getAlgorithm();
        if (lastEventClass == null || RESTART_FROM_PACKAGING.contains(lastEventClass)) {
            logger.info("Creating bag ...");
            this.createBag(bagDir,
                properties.get(PropNames.DEPOSIT_METADATA),
                properties.get(PropNames.VAULT_METADATA),
                properties.get(PropNames.EXTERNAL_METADATA));
            // Tar the bag directory
            logger.info("Creating tar file ...");

            packageHelper.setTarFile(this.createTar(context, bagDir));
            logger.info("Tar file: " + packageHelper.getArchiveSize() + " bytes");
            logger.info("Tar file location: " + packageHelper.getTarFile().getAbsolutePath());

            eventSender.send(new PackageComplete(jobID, depositId)
                    .withUserId(userID)
                    .withNextState(3));
        } else {
            logger.debug("Last event is: " + lastEventClass + " skipping actual packaging");
            this.skipActualPackaging(context, packageHelper);
        }

        if (lastEventClass == null || RESTART_FROM_TAR_CHECKSUM.contains(lastEventClass)) {
            packageHelper.setTarHash(Verify.getDigest(packageHelper.getTarFile()));
            packageHelper.setArchiveSize(packageHelper.getTarFile().length());
            logger.info("Checksum algorithm: " + tarHashAlgorithm);
            logger.info("Checksum: " + packageHelper.getTarHash());
            eventSender.send(new ComputedDigest(jobID, depositId, packageHelper.getTarHash(), tarHashAlgorithm)
                    .withUserId(userID));
        } else {
            logger.debug("Last event is: " + lastEventClass + " skipping tar checksum");
            this.skipTarChecksum(properties.get(PropNames.ARCHIVE_DIGEST), packageHelper);
        }

        logger.info("We have successfully created the Tar, so lets delete the Bag to save space");
        FileUtils.deleteDirectory(bagDir);
        HashMap<Integer, String> chunksDigest = new HashMap<>();
        if ((lastEventClass == null || RESTART_FROM_CHUNKING.contains(lastEventClass)) && context.isChunkingEnabled()) {
            Map<Integer, String> chunksDigestData = this.createChunks(
                packageHelper.getTarFile(), context, tarHashAlgorithm);
            chunksDigest.putAll(chunksDigestData);
        } else {
            logger.debug("Last event is: " + lastEventClass + " skipping chunking");
            int numOfChunks =  0;
            if (properties.get(PropNames.NUM_OF_CHUNKS) != null) {
                numOfChunks = Integer.parseInt(properties.get(PropNames.NUM_OF_CHUNKS));
            }
            this.skipChunking(numOfChunks, context);
        }

        // Encryption
        if ((lastEventClass == null || RESTART_FROM_ENC_CHECKSUM.contains(lastEventClass)) && context.isEncryptionEnabled()) {
            logger.info("Encrypting file(s)...");
            if (context.isChunkingEnabled()) {
                logger.info("Encrypting [{}] chunk files ", chunksDigest.size());
                packageHelper.setChunksIVs(this.encryptChunks(context, chunksDigest, tarHashAlgorithm));
            } else {
                logger.info("Encrypting single non-chunk file [{}]", packageHelper.getTarFile());
                EncryptionHelper helper = this.encryptFullTar(context, packageHelper.getTarFile());
                packageHelper.setIv(helper.getIv());
                packageHelper.setEncTarHash(helper.getEncTarHash());
            }
        } else {
            logger.debug("Last event is: " + lastEventClass + " skipping enc checksum");
        }
    }

    /**********************************************************
    // we are skipping packaging due to a restart
    // we need to fill in some blanks for the later steps to be
    // able to complete
     **********************************************************/
    private void skipPackageStep(Context context, String lastEventClass, int numOfChunks,
        String archiveDigest, PackageHelper packageHelper)  {
        logger.debug("Last event is: " + lastEventClass + " skipping packaging");
        this.skipChunking(numOfChunks, context);
        packageHelper.setChunksIVs(this.chunksIVs);
        this.skipActualPackaging(context, packageHelper);
        this.skipTarChecksum(archiveDigest, packageHelper);

    }

    private void skipActualPackaging(Context context, PackageHelper packageHelper) {

        String tarFileName = bagID + ".tar";
        Path tarPath = context.getTempDir().resolve(tarFileName);
        packageHelper.setTarFile(tarPath.toFile());

    }

    private void skipTarChecksum(String archiveDigest, PackageHelper packageHelper) {

        packageHelper.setTarHash(archiveDigest);
        packageHelper.setArchiveSize(packageHelper.getTarFile().length());

    }

    private void skipChunking(int numOfChunks, Context context) {

        this.chunkFiles = new File[numOfChunks];
        for (int i = 0; i < numOfChunks; i++) {
            int chunkNumber = i + 1;
            String chunkFileName = bagID + ".tar." + chunkNumber;
            Path chunkPath = context.getTempDir().resolve(chunkFileName);
            logger.debug("Mocked chunk file path: " + chunkPath.toAbsolutePath());
            this.chunkFiles[i] = chunkPath.toFile();
        }

    }
}