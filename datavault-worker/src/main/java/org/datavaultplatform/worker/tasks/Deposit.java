package org.datavaultplatform.worker.tasks;

import java.util.Map;
import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.deposit.Start;
import org.datavaultplatform.common.event.deposit.ComputedSize;
import org.datavaultplatform.common.event.deposit.TransferComplete;
import org.datavaultplatform.common.event.deposit.PackageComplete;
import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.*;
import org.datavaultplatform.worker.WorkerInstance;
import org.datavaultplatform.worker.operations.*;
import org.datavaultplatform.worker.queue.EventSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Deposit extends Task {

    private static final Logger logger = LoggerFactory.getLogger(Deposit.class);
    
    EventSender eventStream;
    HashMap<String, UserStore> userStores;
    //Device userFs;
    //UserStore userStore;
    ArchiveStore archiveFs;
    String depositId;
    String bagID;
    String userID;
    
    @Override
    public void performAction(Context context) {
        
        eventStream = (EventSender)context.getEventStream();
        
        logger.info("Deposit job - performAction()");
        
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
                eventStream.send(new Error(jobID, depositId, msg)
                    .withUserId(userID));
                return;
            }
        }
        
        for (String path : fileStorePaths) {
            System.out.println("fileStorePath: " + path);
        }
        for (String path : fileUploadPaths) {
            System.out.println("fileUploadPath: " + path);
        }
        
        // Connect to the archive storage
        try {
            Class<?> clazz = Class.forName(archiveFileStore.getStorageClass());
            Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
            Object instance = constructor.newInstance(archiveFileStore.getStorageClass(), archiveFileStore.getProperties());
            archiveFs = (ArchiveStore)instance;
        } catch (Exception e) {
            String msg = "Deposit failed: could not access archive filesystem";
            logger.error(msg, e);
            eventStream.send(new Error(jobID, depositId, msg)
                .withUserId(userID));
            return;
        }
        
        // Create a new directory based on the broker-generated UUID
        Path bagPath = context.getTempDir().resolve(bagID);
        File bagDir = bagPath.toFile();
        bagDir.mkdir();
        
        for (String filePath: fileStorePaths) {
        
            String storageID = filePath.substring(0, filePath.indexOf('/'));
            String storagePath = filePath.substring(filePath.indexOf('/')+1);
            
            logger.info("Deposit file: " + filePath);
            logger.info("Deposit storageID: " + storageID);
            logger.info("Deposit storagePath: " + storagePath);
            
            UserStore userStore = userStores.get(storageID);
            
            // TODO: handle namespace problems
            
            try {
                if (userStore.exists(storagePath)) {

                    // Copy the target file to the bag directory
                    eventStream.send(new UpdateProgress(jobID, depositId)
                        .withUserId(userID)
                        .withNextState(1));

                    logger.info("Copying target to bag directory ...");
                    copyFromUserStorage(userStore, storagePath, bagPath);
                    
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
            }
        }
        
        try {
            // MOCKUP
            // Add any directly uploaded files (direct move from temp dir)
            // moveFromUserUploads(context.getTempDir(), bagPath, userID);

            // Bag the directory in-place
            eventStream.send(new TransferComplete(jobID, depositId)
                .withUserId(userID)
                .withNextState(2));

            logger.info("Creating bag ...");
            Packager.createBag(bagDir);

            // Identify the deposit file types
            logger.info("Identifying file types ...");
            Path bagDataPath = bagDir.toPath().resolve("data");
            HashMap<String, String> fileTypes = Identifier.detectDirectory(bagDataPath);
            ObjectMapper mapper = new ObjectMapper();
            String fileTypeMetadata = mapper.writeValueAsString(fileTypes);

            // Add vault/deposit/type metadata to the bag
            Packager.addMetadata(bagDir, depositMetadata, vaultMetadata, fileTypeMetadata, externalMetadata);

            // Tar the bag directory
            logger.info("Creating tar file ...");
            String tarFileName = bagID + ".tar";
            Path tarPath = context.getTempDir().resolve(tarFileName);
            File tarFile = tarPath.toFile();
            Tar.createTar(bagDir, tarFile);
            String tarHash = Verify.getDigest(tarFile);
            String tarHashAlgorithm = Verify.getAlgorithm();

            eventStream.send(new PackageComplete(jobID, depositId)
                .withUserId(userID)
                .withNextState(3));

            long archiveSize = tarFile.length();
            logger.info("Tar file: " + archiveSize + " bytes");
            logger.info("Checksum algorithm: " + tarHashAlgorithm);
            logger.info("Checksum: " + tarHash);

            eventStream.send(new ComputedDigest(jobID, depositId, tarHash, tarHashAlgorithm)
                .withUserId(userID));

            // Create the meta directory for the bag information
            Path metaPath = context.getMetaDir().resolve(bagID);
            File metaDir = metaPath.toFile();
            metaDir.mkdir();

            // Copy bag meta files to the meta directory
            logger.info("Copying meta files ...");
            Packager.extractMetadata(bagDir, metaDir);

            // Copy the resulting tar file to the archive area
            logger.info("Copying tar file to archive ...");
            String archiveId = copyToArchiveStorage(tarFile);

            // Cleanup
            logger.info("Cleaning up ...");
            FileUtils.deleteDirectory(bagDir);

            eventStream.send(new UpdateProgress(jobID, depositId)
                .withUserId(userID)
                .withNextState(4));

            logger.info("Verifying archive package ...");
            logger.info("Verification method: " + archiveFs.getVerifyMethod());

            // Get the tar file

            if (archiveFs.getVerifyMethod() == Verify.Method.LOCAL_ONLY) {

                // Verify the contents of the temporary file
                verifyTarFile(context.getTempDir(), tarFile, null);

            } else if (archiveFs.getVerifyMethod() == Verify.Method.COPY_BACK) {

                // Delete the existing temporary file
                tarFile.delete();

                // Copy file back from the archive storage
                copyBackFromArchive(archiveId, tarFile);

                // Verify the contents
                verifyTarFile(context.getTempDir(), tarFile, tarHash);
            }

            logger.info("Deposit complete: " + archiveId);

            eventStream.send(new Complete(jobID, depositId, archiveId, archiveSize)
                .withUserId(userID)
                .withNextState(5));
        } catch (Exception e) {
            String msg = "Deposit failed: " + e.getMessage();
            logger.error(msg, e);
            eventStream.send(new Error(jobID, depositId, msg)
                .withUserId(userID));
        }
        
        // TODO: Disconnect from user and archive storage system?
    }
    
    private void copyFromUserStorage(UserStore userStore, String filePath, Path bagPath) throws Exception {

        String fileName = userStore.getName(filePath);
        File outputFile = bagPath.resolve(fileName).toFile();
        
        // Compute bytes to copy
        long expectedBytes = userStore.getSize(filePath);
        eventStream.send(new ComputedSize(jobID, depositId, expectedBytes)
            .withUserId(userID));
        
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
    
    private void moveFromUserUploads(Path tempPath, Path bagPath, String userID) throws Exception {

        // TODO: possible namespace clash
        File outputFile = bagPath.resolve("uploads").toFile();
        
        // TODO: this is a bit of a hack to escape the per-worker temp directory
        File uploadDir = tempPath.getParent().resolve("uploads").resolve(userID).toFile();
        if (uploadDir.exists()) {
            logger.info("Moving user uploads to bag directory");
            FileUtils.moveDirectory(uploadDir, outputFile);
        }
    }
    
    private String copyToArchiveStorage(File tarFile) throws Exception {

        // Progress tracking (threaded)
        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, tarFile.length(), eventStream);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();

        String archiveId;

        try {
            archiveId = ((Device)archiveFs).store("/", tarFile, progress);
        } finally {
            // Stop the tracking thread
            tracker.stop();
            trackerThread.join();
        }

        logger.info("Copied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
        
        return archiveId;
    }
    
    private void copyBackFromArchive(String archiveId, File tarFile) throws Exception {
        
        // Ask the driver to copy files to the temp directory
        Progress progress = new Progress();
        ((Device)archiveFs).retrieve(archiveId, tarFile, progress);
        logger.info("Copied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
    }
    
    private void verifyTarFile(Path tempPath, File tarFile, String origTarHash) throws Exception {

        if (origTarHash != null) {
            // Compare the SHA hash
            String tarHash = Verify.getDigest(tarFile);
            logger.info("Checksum: " + tarHash);
            if (!tarHash.equals(origTarHash)) {
                throw new Exception("checksum failed: " + tarHash + " != " + origTarHash);
            }
        }
        
        // Decompress to the temporary directory
        File bagDir = Tar.unTar(tarFile, tempPath);
        
        // Validate the bagit directory
        if (!Packager.validateBag(bagDir)) {
            throw new Exception("Bag is invalid");
        } else {
            logger.info("Bag is valid");
        }
        
        // Cleanup
        logger.info("Cleaning up ...");
        FileUtils.deleteDirectory(bagDir);
        tarFile.delete();
    }
}