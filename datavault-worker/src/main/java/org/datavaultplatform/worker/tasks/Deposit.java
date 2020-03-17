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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    //private String vaultID;

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

        Deposit.RESTART_FROM_ENC_CHECKSUM.addAll(RESTART_FROM_TAR_CHECKSUM);
        Deposit.RESTART_FROM_ENC_CHECKSUM.add("org.datavaultplatform.common.event.deposit.ComputedDigest");

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
        int numOfChunks =  0;
        if (properties.get("numOfChunks") != null) {
            numOfChunks = Integer.parseInt(properties.get("numOfChunks"));
        }
        String archiveDigest = null;
        if (properties.get("archiveDigest") != null) {
            archiveDigest = properties.get("archiveDigest");
        }

        // file copy complete org.datavaultplatform.common.event.deposit.TransferComplete
        // bag complete
        // tar complete
        // chunk complete
        // encrypt complete org.datavaultplatform.common.event.deposit.PackageComplete
        // upload complete
        // validate complete org.datavaultplatform.common.event.deposit.Complete
        String lastEventClass = (this.getLastEvent() != null) ? this.getLastEvent().getEventClass() : null;
        if (lastEventClass != null) {
            logger.debug("The last event was: " + lastEventClass);
        }
        if (this.isRedeliver()) {
            eventStream.send(new Error(jobID, depositId, "Deposit stopped: the message had been redelivered, please investigate")
                .withUserId(userID));
            return;
        }
        
        // Deposit and Vault metadata to be stored in the bag
        // TODO: is there a better way to pass this to the worker?
        String depositMetadata = properties.get("depositMetadata");
        String vaultMetadata = properties.get("vaultMetadata");
        String externalMetadata = properties.get("externalMetadata");
        
        this.initStates();
        
        eventStream.send(new Start(jobID, depositId)
            .withUserId(userID)
            .withNextState(0));

        Path bagDataPath = null;
        File bagDir = null;

        userStores = this.setupUserFileStores();
        this.setupArchiveFileStores();
        if (lastEventClass == null || RESTART_FROM_TRANSFER.contains(lastEventClass)) {
            logger.info("bagID: " + bagID);
            this.calculateTotalDepositSize(context);

            // Create a new directory based on the broker-generated UUID
            Path bagPath = context.getTempDir().resolve(bagID);
            logger.debug("The is the bagPath " + bagPath.toString());
            bagDataPath = bagPath.resolve("data");
            logger.debug("The is the bagDataPath " + bagDataPath.toString());
            bagDir = this.createDir(bagPath);
            this.createDir(bagDataPath);

            this.copySelectedUserDataToBagDir(bagDataPath);
        } else {
            logger.debug("Last event is: " + lastEventClass + " skipping initial File copy");
            Path bagPath = context.getTempDir().resolve(bagID);
            bagDataPath = bagPath.resolve("data");
            bagDir = bagPath.toFile();
        }
        
        try {
            File tarFile = null;
            String tarHash = null;
            if (lastEventClass == null || RESTART_FROM_TRANSFER.contains(lastEventClass)) {
                this.copyAdditionalUserData(context, bagDataPath);
            } else {
                logger.debug("Last event is: " + lastEventClass + " skipping secondary File copy");
            }

//            if (lastEventClass == null) {
//                throw new Exception("Failed after file transfer");
//            }

            byte iv[] = null;
            String encTarHash = null;
            Map<Integer, byte[]> chunksIVs = null;
            Long archiveSize = null;
            if (lastEventClass == null || RESTART_FROM_PACKAGING.contains(lastEventClass)) {
                logger.info("Creating bag ...");
                this.createBag(bagDir, depositMetadata, vaultMetadata, externalMetadata);

                // Tar the bag directory
                logger.info("Creating tar file ...");
                tarFile = this.createTar(context, bagDir);
                tarHash = Verify.getDigest(tarFile);
                String tarHashAlgorithm = Verify.getAlgorithm();

                archiveSize = tarFile.length();
                logger.info("Tar file: " + archiveSize + " bytes");
                logger.info("Tar file location: " + tarFile.getAbsolutePath());
                logger.info("Checksum algorithm: " + tarHashAlgorithm);
                logger.info("Checksum: " + tarHash);

                eventStream.send(new PackageComplete(jobID, depositId)
                        .withUserId(userID)
                        .withNextState(3));

                eventStream.send(new ComputedDigest(jobID, depositId, tarHash, tarHashAlgorithm)
                        .withUserId(userID));


                logger.info("We have successfully created the Tar, so lets delete the Bag to save space");
                FileUtils.deleteDirectory(bagDir);

                HashMap<Integer, String> chunksDigest = null;
                if (context.isChunkingEnabled()) {
                    chunksDigest = this.createChunks(tarFile, context, chunksDigest, tarHashAlgorithm);
                }

                // Encryption
                if (context.isEncryptionEnabled()) {
                    logger.info("Encrypting file(s)...");
                    if (context.isChunkingEnabled()) {
                        chunksIVs = this.encryptChunks(context, chunksDigest, tarHashAlgorithm);
                    } else {
                        EncryptionHelper helper = this.encryptFullTar(context, tarFile);
                        iv = helper.getIv();
                        encTarHash = helper.getEncTarHash();
                    }
                }

//                if (lastEventClass == null) {
//                    throw new Exception("Failed after chunking / encryption");
//                }
            } else {
                logger.debug("Last event is: " + lastEventClass + " skipping packaging");

                // we need to mock the chunkfile for a restart after packaging
                //something like
                // for number of chunks
                //  add file.x to chunkFiles array
                this.chunkFiles = new File[numOfChunks];
                for (int i = 0; i < numOfChunks; i++) {
                    String chunkFileName = bagID + ".tar." + (i + 1);
                    Path chunkPath = context.getTempDir().resolve(chunkFileName);
                    logger.debug("Mocked chunk file path: " + chunkPath.toAbsolutePath().toString());
                    this.chunkFiles[i] = chunkPath.toFile();
                }
                chunksIVs = this.chunksIVs;
                String tarFileName = bagID + ".tar";
                Path tarPath = context.getTempDir().resolve(tarFileName);
                tarFile = tarPath.toFile();
                tarHash = archiveDigest;
                archiveSize = tarFile.length();
            }
            // Copy the resulting tar file to the archive area
            logger.info("Copying tar file(s) to archive ...");

            if (lastEventClass == null || RESTART_FROM_UPLOAD.contains(lastEventClass)) {
                this.uploadToStorage(context, tarFile);
            } else {
                logger.debug("Last event is: " + lastEventClass + " skipping upload");
                UploadComplete uc = (UploadComplete) this.getLastEvent();
                archiveIds = uc.getArchiveIds();
            }
            if (lastEventClass == null) {
                throw new Exception("Failed after upload");
            }
            logger.info("Verifying archive package ...");
            if (lastEventClass == null || RESTART_FROM_VALIDATION.contains(lastEventClass)) {
                this.verifyArchive(context, tarFile, tarHash, iv, chunksIVs, encTarHash);
            } else {
                logger.debug("Last event is: " + lastEventClass + " skipping validation");
            }

            //if (lastEventClass == null) {
            //    throw new Exception("Failed after validation");
            //}
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

        for (String archiveStoreId : archiveStores.keySet() ) {
            logger.debug("ArchiveStoreId: " + archiveStoreId);
            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);

            // Progress tracking (threaded)
            Progress progress = new Progress();
            ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, tarFile.length(), eventStream);
            Thread trackerThread = new Thread(tracker);
            trackerThread.start();
            String depId = this.depositId;
            if (chunkCount > 0) {
            		depId = depId + "." + chunkCount;
            }
            String archiveId;

            try {
            	eventStream.send(new StartCopyUpload(jobID, depositId, ((Device) archiveStore).name, chunkCount ).withUserId(userID));
	            if (((Device)archiveStore).hasDepositIdStorageKey()) {
	            		archiveId = ((Device) archiveStore).store(depId, tarFile, progress);
	            } else {
	            		archiveId = ((Device) archiveStore).store("/", tarFile, progress);
	            }
	            eventStream.send(new CompleteCopyUpload(jobID, depositId, ((Device) archiveStore).name, chunkCount ).withUserId(userID));
            } finally {
                // Stop the tracking thread
                tracker.stop();
                trackerThread.join();
            }

            logger.info("Copied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
            
            if (chunkCount > 0 && archiveIds.get(archiveStoreId) == null) {
            		logger.info("ArchiveId is: " + archiveId);
            		String separator = FileSplitter.CHUNK_SEPARATOR;
            		logger.info("Separator is: " + separator);
            		int beginIndex = archiveId.lastIndexOf(separator);
            		logger.info("BeginIndex is: " + beginIndex); 
            		archiveId = archiveId.substring(0, beginIndex);
            		logger.debug("Add to archiveIds: key: "+archiveStoreId+" ,value:"+archiveId);
            		archiveIds.put(archiveStoreId, archiveId);
            		logger.debug("archiveIds: "+archiveIds);
            } else if(chunkCount == 0) {
                archiveIds.put(archiveStoreId, archiveId);
            }
        }
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
    
    private void doArchive(Context context, File[] chunkFiles, String[] chunksHash, File tarFile, String tarHash, ArchiveStore archiveStore, 
            String archiveId, String location, boolean multipleCopies, Map<Integer, byte[]> ivs,
            String encTarHash, String[] encChunksHash, boolean doVerification) throws Exception {
        logger.debug("In doArchive");
        for (int i = 0; i < chunkFiles.length; i++) {

            File chunkFile = chunkFiles[i];
            logger.debug("Validating chunk file: " + chunkFile.getAbsolutePath());
            //String chunkHash = chunksHash[i];
            
            // Delete the existing temporary file
            logger.debug("Deleting original chunk: " + chunkFile.getAbsolutePath());
            chunkFile.delete();
            String archiveChunkId = archiveId+FileSplitter.CHUNK_SEPARATOR+(i+1);
            // Copy file back from the archive storage
            logger.debug("archiveChunkId: "+archiveChunkId);
            if (multipleCopies && location != null) {
            		copyBackFromArchive(archiveStore, archiveChunkId, chunkFile, location);
            } else {
            		copyBackFromArchive(archiveStore, archiveChunkId, chunkFile);
            }

            //need to mock archive ids and other stuff the upload sets
            //also keep tidying up the new code move the mocking and possibly the sections into methods

            // Decryption
            if(ivs != null) {
                for (Integer iv : ivs.keySet()) {
                    logger.debug("Ivs:" + ivs.get(iv));
                }
                if(doVerification) {
                    Encryption.decryptFile(context, chunkFile, ivs.get(i + 1));
                } else {
                    String encChunkHash = encChunksHash[i];

                    // Check hash of encrypted file
                    logger.debug("Verifying encrypted chunk file: "+chunkFile.getAbsolutePath());
                    eventStream.send(new StartChunkValidation(jobID, depositId, ((Device) archiveStore).name, i + 1 ).withUserId(userID));
                    verifyChunkFile(context.getTempDir(), chunkFile, encChunkHash);
                    eventStream.send(new CompleteChunkValidation(jobID, depositId, ((Device) archiveStore).name, i + 1 ).withUserId(userID));

                }
            } else {
                logger.debug("No IVs supplied.");
            }
                
            //logger.debug("Verifying chunk file: "+chunkFile.getAbsolutePath());
            //verifyChunkFile(context.getTempDir(), chunkFile, chunkHash);
        }

        if(doVerification) {
            FileSplitter.recomposeFile(chunkFiles, tarFile);

            // Verify the contents
            eventStream.send(new StartTarValidation(jobID, depositId).withUserId(userID));
            verifyTarFile(context.getTempDir(), tarFile, tarHash);
            eventStream.send(new CompleteTarValidation(jobID, depositId).withUserId(userID));
        }
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
                        copyBackFromArchive(archiveStore, archiveId, tarFile, loc);
                        
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
                    copyBackFromArchive(archiveStore, archiveId, tarFile);
                    
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

    /**
     * Not sure what this does yet the comment below suggests it copies an archive to the tmp dir
     * why are deposit would do this I'm not sure
     * 
     * @param archiveStore
     * @param archiveId
     * @param tarFile
     * @throws Exception
     */
    private void copyBackFromArchive(ArchiveStore archiveStore, String archiveId, File tarFile) throws Exception {

//        // Ask the driver to copy files to the temp directory
//        Progress progress = new Progress();
//        ((Device)archiveStore).retrieve(archiveId, tarFile, progress);
//        logger.info("Copied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
    		this.copyBackFromArchive(archiveStore, archiveId, tarFile, null);
    }
    
    private void copyBackFromArchive(ArchiveStore archiveStore, String archiveId, File tarFile, String location) throws Exception {

        	// Ask the driver to copy files to the temp directory
        	Progress progress = new Progress();
        	if (location == null) {
        		((Device)archiveStore).retrieve(archiveId, tarFile, progress);
        	} else {
        		((Device)archiveStore).retrieve(archiveId, tarFile, progress, location);
        	}
        logger.info("Copied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
    }
    
    private void verifyChunkFile(Path tempPath, File chunkFile, String origChunkHash) throws Exception {

        if (origChunkHash != null) {
            logger.info("Get Digest from: " + chunkFile.getAbsolutePath());
            // Compare the SHA hash
            String chunkHash = Verify.getDigest(chunkFile);
            logger.info("Checksum: " + chunkHash);
            if (!chunkHash.equals(origChunkHash)) {
                throw new Exception("checksum failed: " + chunkHash + " != " + origChunkHash);
            }
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
		for (String filePath : fileStorePaths) {
			String storageID = filePath.substring(0, filePath.indexOf('/'));
			String storagePath = filePath.substring(filePath.indexOf('/') + 1);

			try {
				UserStore userStore = userStores.get(storageID);
				depositTotalSize += userStore.getSize(storagePath);
			} catch (Exception e) {
				String msg = "Deposit failed: could not access user filesystem";
				logger.error(msg, e);
				eventStream.send(new Error(jobID, depositId, msg).withUserId(userID));
				throw new RuntimeException(e);
			}

		}
		
//		depositTotalSize += this.calculateUserUploads(depositTotalSize, context);

		// Store the calculated deposit size
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

        for (int i = 0; i < chunkFiles.length; i++){
            File chunk = chunkFiles[i];
            chunksHash[i] = Verify.getDigest(chunk);
            
            long chunkSize = chunk.length();
            logger.info("Chunk file " + i + ": " + chunkSize + " bytes");
            logger.info("Chunk file location: " + chunk.getAbsolutePath());
            logger.info("Checksum algorithm: " + tarHashAlgorithm);
            logger.info("Checksum: " + chunksHash[i]);
            
            chunksDigest.put(i+1, chunksHash[i]);
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
        
        for (int i = 0; i < chunkFiles.length; i++){
            File chunk = chunkFiles[i];
            
            // Generating IV
            byte[] chunkIV = Encryption.encryptFile(context, chunk);
            
            encChunksHash[i] = Verify.getDigest(chunk);

            logger.info("Chunk file " + i + ": " + chunk.length() + " bytes");
            logger.info("Encrypted chunk checksum: " + encChunksHash[i]);
            
            encChunksDigests.put(i+1, encChunksHash[i]);
            chunksIVs.put(i+1, chunkIV);
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
    		int chunkCount = 0;
    		for (File chunk : chunkFiles){
    			chunkCount++;
    			logger.debug("Copying chunk: "+chunk.getName());
    			this.copyToArchiveStorage(chunk, chunkCount);
    			logger.debug("archiveIds: "+archiveIds);
    		}
		} else {
			copyToArchiveStorage(tarFile);
		}
        TimeUnit.SECONDS.sleep(5);
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
}