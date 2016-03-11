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
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.*;
import org.datavaultplatform.worker.operations.*;
import org.datavaultplatform.worker.queue.EventSender;

public class Deposit extends Task {

    EventSender eventStream;
    Device userFs;
    UserStore userStore;
    ArchiveStore archiveFs;
    String depositId;
    String bagID;
    
    @Override
    public void performAction(Context context) {
        
        eventStream = (EventSender)context.getEventStream();
        
        System.out.println("\tDeposit job - performAction()");
        
        Map<String, String> properties = getProperties();
        depositId = properties.get("depositId");
        bagID = properties.get("bagId");
        String filePath = properties.get("filePath");

        if (this.isRedeliver()) {
            eventStream.send(new Error(jobID, depositId, "Deposit stopped: the message had been redelivered, please investigate"));
            return;
        }
        
        // Deposit and Vault metadata to be stored in the bag
        // TODO: is there a better way to pass this to the worker?
        String depositMetadata = properties.get("depositMetadata");
        String vaultMetadata = properties.get("vaultMetadata");
        
        ArrayList<String> states = new ArrayList<>();
        states.add("Calculating size");     // 0
        states.add("Transferring");         // 1
        states.add("Packaging");            // 2
        states.add("Storing in archive");   // 3
        states.add("Verifying");            // 4
        states.add("Complete");             // 5
        eventStream.send(new InitStates(jobID, depositId, states));
        
        eventStream.send(new Start(jobID, depositId).withNextState(0));
        
        System.out.println("\tbagID: " + bagID);
        System.out.println("\tfilePath: " + filePath);
        
        // Connect to the user storage
        try {
            Class<?> clazz = Class.forName(userFileStore.getStorageClass());
            Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
            Object instance = constructor.newInstance(userFileStore.getStorageClass(), userFileStore.getProperties());
            userFs = (Device)instance;
            userStore = (UserStore)userFs;
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(jobID, depositId, "Deposit failed: could not access user filesystem"));
            return;
        }
        
        // Connect to the archive storage
        try {
            Class<?> clazz = Class.forName(archiveFileStore.getStorageClass());
            Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
            Object instance = constructor.newInstance(archiveFileStore.getStorageClass(), archiveFileStore.getProperties());
            archiveFs = (ArchiveStore)instance;
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(jobID, depositId, "Deposit failed: could not access archive filesystem"));
            return;
        }
        
        System.out.println("\tDeposit file: " + filePath);

        try {
            if (userStore.exists(filePath)) {

                // Create a new directory based on the broker-generated UUID
                Path bagPath = Paths.get(context.getTempDir(), bagID);
                File bagDir = bagPath.toFile();
                bagDir.mkdir();
                
                // Copy the target file to the bag directory
                eventStream.send(new UpdateProgress(jobID, depositId).withNextState(1));
                System.out.println("\tCopying target to bag directory ...");
                copyFromUserStorage(filePath, bagPath);
                
                // Bag the directory in-place
                eventStream.send(new TransferComplete(jobID, depositId).withNextState(2));
                System.out.println("\tCreating bag ...");
                Packager.createBag(bagDir);

                // Identify the deposit file types
                Path bagDataPath = bagDir.toPath().resolve("data");
                HashMap<String, String> fileTypes = Identifier.detectDirectory(bagDataPath);
                ObjectMapper mapper = new ObjectMapper();
                String fileTypeMetadata = mapper.writeValueAsString(fileTypes);
                
                // Add vault/deposit/type metadata to the bag
                Packager.addMetadata(bagDir, depositMetadata, vaultMetadata, fileTypeMetadata);
                
                // Tar the bag directory
                System.out.println("\tCreating tar file ...");
                String tarFileName = bagID + ".tar";
                Path tarPath = Paths.get(context.getTempDir()).resolve(tarFileName);
                File tarFile = tarPath.toFile();
                Tar.createTar(bagDir, tarFile);

                eventStream.send(new PackageComplete(jobID, depositId).withNextState(3));
                
                long archiveSize = tarFile.length();
                System.out.println("\tTar file: " + archiveSize + " bytes");
                
                // Create the meta directory for the bag information
                Path metaPath = Paths.get(context.getMetaDir(), bagID);
                File metaDir = metaPath.toFile();
                metaDir.mkdir();
                
                // Copy bag meta files to the meta directory
                System.out.println("\tCopying meta files ...");
                Packager.extractMetadata(bagDir, metaDir);
                
                // Copy the resulting tar file to the archive area
                System.out.println("\tCopying tar file to archive ...");
                String archiveId = copyToArchiveStorage(tarFile);
                
                // Cleanup
                System.out.println("\tCleaning up ...");
                FileUtils.deleteDirectory(bagDir);
                
                eventStream.send(new UpdateProgress(jobID, depositId).withNextState(4));
                System.out.println("\tVerifying archive package ...");
                System.out.println("\tVerification method: " + archiveFs.getVerifyMethod());
                
                // Get the tar file
                Path tempPath = Paths.get(context.getTempDir());
                
                if (archiveFs.getVerifyMethod() == Verify.Method.LOCAL_ONLY) {
                    
                    // Verify the contents of the temporary file
                    verifyTarFile(tempPath, tarFile);
                    
                } else if (archiveFs.getVerifyMethod() == Verify.Method.COPY_BACK) {

                    // Delete the existing temporary file
                    tarFile.delete();
                    
                    // Copy file back from the archive storage
                    copyBackFromArchive(archiveId, tarFile);
                    
                    // Veriy the contents
                    verifyTarFile(tempPath, tarFile);
                }
                
                System.out.println("\tDeposit complete: " + archiveId);
                eventStream.send(new Complete(jobID, depositId, archiveId, archiveSize).withNextState(5));
                
            } else {
                System.err.println("\tFile does not exist.");
                eventStream.send(new Error(jobID, depositId, "Deposit failed: file not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(jobID, depositId, "Deposit failed: " + e.getMessage()));
        }
    }
    
    private void copyFromUserStorage(String filePath, Path bagPath) throws Exception {

        String fileName = userStore.getName(filePath);
        File outputFile = bagPath.resolve(fileName).toFile();
        
        // Compute bytes to copy
        long expectedBytes = userStore.getSize(filePath);
        eventStream.send(new ComputedSize(jobID, depositId, expectedBytes));
        
        // Display progress bar
        eventStream.send(new UpdateProgress(jobID, depositId, 0, expectedBytes, "Starting transfer ..."));
        System.out.println("\tSize: " + expectedBytes + " bytes (" +  FileUtils.byteCountToDisplaySize(expectedBytes) + ")");
        
        // Progress tracking (threaded)
        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, expectedBytes, eventStream);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();
        
        try {
            // Ask the driver to copy files to our working directory
            userFs.retrieve(filePath, outputFile, progress);
        } finally {
            // Stop the tracking thread
            tracker.stop();
            trackerThread.join();
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

        System.out.println("\tCopied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
        
        return archiveId;
    }
    
    private void copyBackFromArchive(String archiveId, File tarFile) throws Exception {
        
        // Ask the driver to copy files to the temp directory
        Progress progress = new Progress();
        ((Device)archiveFs).retrieve(archiveId, tarFile, progress);
        System.out.println("\tCopied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
    }
    
    private void verifyTarFile(Path tempPath, File tarFile) throws Exception {

        // Decompress to the temporary directory
        File bagDir = Tar.unTar(tarFile, tempPath);
        
        // Validate the bagit directory
        if (!Packager.validateBag(bagDir)) {
            throw new Exception("Bag is invalid");
        } else {
            System.out.println("\tBag is valid");
        }
        
        // Cleanup
        System.out.println("\tCleaning up ...");
        FileUtils.deleteDirectory(bagDir);
        tarFile.delete();
    }
}