package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;

/**
 * A class that extends Task which is used to handle Deposits to the vault
 */
public class Deposit extends Task {

    private static final Logger logger = LoggerFactory.getLogger(Deposit.class);

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
    
    /* (non-Javadoc)
     * @see org.datavaultplatform.common.task.Task#performAction(org.datavaultplatform.common.task.Context)
     */
    @Override
    public void performAction(Context context) {
        
        eventStream = (EventSender)context.getEventStream();
        
        logger.info("Deposit job - performAction()");
        logger.info("chunking: "+context.isChunkingEnabled());
        logger.info("chunks byte size: "+context.getChunkingByteSize());
        logger.info("encryption: "+context.isEncryptionEnabled());
        logger.info("encryption mode: "+context.getEncryptionMode());
        
        Map<String, String> properties = getProperties();
        depositId = properties.get("depositId");
        bagID = properties.get("bagId");
        userID = properties.get("userId");
        
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
        
        ArrayList<String> states = new ArrayList<>();
        states.add("Calculating size");     // 0
        states.add("Transferring");         // 1
        states.add("Packaging");            // 2
        states.add("Storing in archive");   // 3
        states.add("Verifying");            // 4
        states.add("Complete");             // 5
        eventStream.send(new InitStates(jobID, depositId, states)
            .withUserId(userID));
        
        eventStream.send(new Start(jobID, depositId)
            .withUserId(userID)
            .withNextState(0));
        
        logger.info("bagID: " + bagID);
//        try {
//        		JSONObject jsnobject = new JSONObject(vaultMetadata);
//        		this.vaultID = jsnobject.getString("id");
//        		logger.info("vaultId: " + this.vaultID);
//		} catch (JSONException e1) {
//			/* TODO not sure what to do here.  Vault id should never be unavailable as each deposit needs to be attached to a vault
//			at the moment the vault id is only used by the S3 plugin and may be used by the TSM plugin in the future*/
//			e1.printStackTrace();
//		}
        
        userStores = new HashMap<>();
        
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

                System.exit(1);
            }
        }
        
        for (String path : fileStorePaths) {
            System.out.println("fileStorePath: " + path);
        }
        for (String path : fileUploadPaths) {
            System.out.println("fileUploadPath: " + path);
        }

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
                eventStream.send(new Error(jobID, depositId, msg)
                        .withUserId(userID));

                System.exit(1);
            }
        }
        
        // Calculate the total deposit size of selected files
        long depositTotalSize = 0;
        for (String filePath: fileStorePaths) {
        
            String storageID = filePath.substring(0, filePath.indexOf('/'));
            String storagePath = filePath.substring(filePath.indexOf('/')+1);
            
            try {
                UserStore userStore = userStores.get(storageID);
                depositTotalSize += userStore.getSize(storagePath);
            } catch (Exception e) {
                String msg = "Deposit failed: could not access user filesystem";
                logger.error(msg, e);
                eventStream.send(new Error(jobID, depositId, msg)
                    .withUserId(userID));

                System.exit(1);
            }
        }
        
        // Add size of any user uploads
        try {
            for (String path : fileUploadPaths) {
                depositTotalSize += getUserUploadsSize(context.getTempDir(), userID, path);
            }
        } catch (Exception e) {
            String msg = "Deposit failed: could not access user uploads";
            logger.error(msg, e);
            eventStream.send(new Error(jobID, depositId, msg)
                .withUserId(userID));
            return;
        }
        
        // Store the calculated deposit size
        eventStream.send(new ComputedSize(jobID, depositId, depositTotalSize)
            .withUserId(userID));
        
        // Create a new directory based on the broker-generated UUID
        Path bagPath = context.getTempDir().resolve(bagID);
        File bagDir = bagPath.toFile();
        bagDir.mkdir();

        // Create a subdirectory 'data'
        Path bagDataPath = bagPath.resolve("data");
        File bagDataDir = bagDataPath.toFile();
        bagDataDir.mkdir();
        
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

                System.exit(1);
            }
        }
        
        try {

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

            logger.info("Creating bag ...");
            packagerV2.createBag(bagDir);

            // Identify the deposit file types
            //logger.info("Identifying file types ...");
            //HashMap<String, String> fileTypes = Identifier.detectDirectory(bagDataPath);
            //ObjectMapper mapper = new ObjectMapper();
            //String fileTypeMetadata = mapper.writeValueAsString(fileTypes);
            
            // Add vault/deposit/type metadata to the bag
            packagerV2.addMetadata(bagDir, depositMetadata, vaultMetadata, null, externalMetadata);

            // Tar the bag directory
            logger.info("Creating tar file ...");
            String tarFileName = bagID + ".tar";
            Path tarPath = context.getTempDir().resolve(tarFileName);
            File tarFile = tarPath.toFile();
            Tar.createTar(bagDir, tarFile);
            String tarHash = Verify.getDigest(tarFile);
            String tarHashAlgorithm = Verify.getAlgorithm();

            long archiveSize = tarFile.length();
            logger.info("Tar file: " + archiveSize + " bytes");
            logger.info("Checksum algorithm: " + tarHashAlgorithm);
            logger.info("Checksum: " + tarHash);

            eventStream.send(new PackageComplete(jobID, depositId)
                .withUserId(userID)
                .withNextState(3));

            eventStream.send(new ComputedDigest(jobID, depositId, tarHash, tarHashAlgorithm)
                .withUserId(userID));

            logger.info("We have successfully created the Tar, so lets delete the Bag to save space");
            FileUtils.deleteDirectory(bagDir);

            if ( context.isChunkingEnabled() ) {
                logger.info("Chunking tar file ...");
                chunkFiles = FileSplitter.spliteFile(tarFile, context.getChunkingByteSize());
                chunksHash = new String[chunkFiles.length];
                HashMap<Integer, String> chunksDigest = new HashMap<Integer, String>();

                for (int i = 0; i < chunkFiles.length; i++){
                    File chunk = chunkFiles[i];
                    chunksHash[i] = Verify.getDigest(chunk);
                    
                    long chunkSize = chunk.length();
                    logger.info("Chunk file " + i + ": " + chunkSize + " bytes");
                    logger.info("Checksum algorithm: " + tarHashAlgorithm);
                    logger.info("Checksum: " + chunksHash[i]);
                    
                    chunksDigest.put(i+1, chunksHash[i]);
                }
                
                logger.info(chunkFiles.length + " chunk files created.");
                
                eventStream.send(new ComputedChunks(jobID, depositId, chunksDigest, tarHashAlgorithm)
                        .withUserId(userID));
            }

            // Encryption
            HashMap<Integer, byte[]> chunksIVs = null;
            byte iv[] = null;
            String encTarHash = null;
            if(context.isEncryptionEnabled()) {
                logger.info("Encrypting file(s)...");

                if (context.isChunkingEnabled()) {
                    HashMap<Integer, String> encChunksDigests = new HashMap<Integer, String>();
                    chunksIVs = new HashMap<Integer, byte[]>();
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
                            context.getEncryptionMode().toString(), null, encChunksDigests)
                                .withUserId(userID));
                } else {
                    iv = Encryption.encryptFile(context, tarFile);
                    
                    encTarHash = Verify.getDigest(tarFile);
                    
                    logger.info("Encrypted Tar file: " + tarFile.length() + " bytes");
                    logger.info("Encrypted tar checksum: " + encTarHash);
                    
                    eventStream.send(new ComputedEncryption(jobID, depositId, null, 
                            iv, context.getEncryptionMode().toString(),
                            encTarHash, null)
                                .withUserId(userID));
                }
            }

            // The following bit of code was commented out by Robin on 22/11/18. Feel free to remove it.

            // Create the meta directory for the bag information
            //Path metaPath = context.getMetaDir().resolve(bagID);
            //File metaDir = metaPath.toFile();
            //metaDir.mkdir();

            // Copy bag meta files to the meta directory
            //logger.info("Copying meta files ...");
            //Packager.extractMetadata(bagDir, metaDir);

            // Copy the resulting tar file to the archive area
            logger.info("Copying tar file(s) to archive ...");
            
            if ( context.isChunkingEnabled() ) {
            		int chunkCount = 0;
                for (File chunk : chunkFiles){
                		chunkCount++;
                    logger.debug("Copying chunk: "+chunk.getName());
                    copyToArchiveStorage(chunk, chunkCount);
                    logger.debug("archiveIds: "+archiveIds);
                }
            } else {
                copyToArchiveStorage(tarFile);
            }

            eventStream.send(new UpdateProgress(jobID, depositId)
                .withUserId(userID)
                .withNextState(4));

            logger.info("Verifying archive package ...");
            if( context.isChunkingEnabled() ) {
                verifyArchive(context, chunkFiles, chunksHash, tarFile, tarHash, chunksIVs, encChunksHash);
            } else {
                verifyArchive(context, tarFile, tarHash, iv, encTarHash);
            }

            logger.info("Deposit complete");

            eventStream.send(new Complete(jobID, depositId, archiveIds, archiveSize)
                .withUserId(userID)
                .withNextState(5));
        } catch (Exception e) {
            String msg = "Deposit failed: " + e.getMessage();
            logger.error(msg, e);
            eventStream.send(new Error(jobID, depositId, msg)
                .withUserId(userID));

            System.exit(1);
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
            	eventStream.send(new StartCopyUpload(jobID, depositId, ((Device) archiveStore).name ).withUserId(userID));
	            if (((Device)archiveStore).hasDepositIdStorageKey()) {
	            		archiveId = ((Device) archiveStore).store(depId, tarFile, progress);
	            } else {
	            		archiveId = ((Device) archiveStore).store("/", tarFile, progress);
	            }
	            eventStream.send(new CompleteCopyUpload(jobID, depositId, ((Device) archiveStore).name ).withUserId(userID));
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
            HashMap<Integer, byte[]> ivs, String[] encChunksHash) throws Exception {

        for (String archiveStoreId : archiveStores.keySet() ) {

            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
            String archiveId = archiveIds.get(archiveStoreId);
            
            if (archiveStore.getVerifyMethod() != Verify.Method.COPY_BACK && archiveStore.getVerifyMethod() != Verify.Method.CLOUD ) {
                throw new Exception("Wrong Verify Method: [" + archiveStore.getVerifyMethod() + "] has to be " + Verify.Method.COPY_BACK);
            }
            
            if (((Device)archiveStore).hasMultipleCopies()) {
                for (String loc : ((Device)archiveStore).getLocations()) {
                    this.doArchive(context, chunkFiles, chunksHash, tarFile, tarHash, archiveStore, archiveId, loc, true, ivs, null, encChunksHash);
                }
            } else {   
            	if (archiveStore.getVerifyMethod() != Verify.Method.CLOUD) {
            		this.doArchive(context, chunkFiles, chunksHash, tarFile, tarHash, archiveStore, archiveId, ivs, null, encChunksHash);
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
            String archiveId, HashMap<Integer, byte[]> ivs, String encTarHash, String[] encChunksHash) throws Exception {
        this.doArchive(context, chunkFiles, chunksHash, tarFile, tarHash, archiveStore, archiveId, null,  false, ivs, encTarHash, encChunksHash);
    }
    
    private void doArchive(Context context, File[] chunkFiles, String[] chunksHash, File tarFile, String tarHash, ArchiveStore archiveStore, 
            String archiveId, String location, boolean multipleCopies, HashMap<Integer, byte[]> ivs, 
            String encTarHash, String[] encChunksHash) throws Exception {
        
        for (int i = 0; i < chunkFiles.length; i++) {
            File chunkFile = chunkFiles[i];
            String chunkHash = chunksHash[i];
            
            // Delete the existing temporary file
            chunkFile.delete();
            String archiveChunkId = archiveId+FileSplitter.CHUNK_SEPARATOR+(i+1);
            // Copy file back from the archive storage
            logger.debug("archiveChunkId: "+archiveChunkId);
            if (multipleCopies && location != null) {
            		copyBackFromArchive(archiveStore, archiveChunkId, chunkFile, location);
            } else {
            		copyBackFromArchive(archiveStore, archiveChunkId, chunkFile);
            }

            // Decryption
            if(ivs != null) {
                //String encChunkHash = encChunksHash[i];
                
                // Check hash of encrypted file
                //logger.debug("Verifying encrypted chunk file: "+chunkFile.getAbsolutePath());
                //verifyChunkFile(context.getTempDir(), chunkFile, encChunkHash);
                
                // TODO: get aesKey from secure place
                Encryption.decryptFile(context, chunkFile, ivs.get(i+1));
            }
                
            //logger.debug("Verifying chunk file: "+chunkFile.getAbsolutePath());
            //verifyChunkFile(context.getTempDir(), chunkFile, chunkHash);
        }
        
        FileSplitter.recomposeFile(chunkFiles, tarFile);

        // Verify the contents
        verifyTarFile(context.getTempDir(), tarFile, tarHash);
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
    
}