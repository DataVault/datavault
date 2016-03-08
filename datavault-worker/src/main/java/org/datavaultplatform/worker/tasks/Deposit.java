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
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.worker.operations.Tar;
import org.datavaultplatform.worker.operations.Packager;
import org.datavaultplatform.worker.operations.ProgressTracker;
import org.datavaultplatform.worker.operations.Identifier;
import org.datavaultplatform.worker.queue.EventSender;

public class Deposit extends Task {

    EventSender events;
    
    @Override
    public void performAction(Context context) {
        
        EventSender eventStream = (EventSender)context.getEventStream();
        
        System.out.println("\tDeposit job - performAction()");
        
        Map<String, String> properties = getProperties();
        String depositId = properties.get("depositId");
        String bagID = properties.get("bagId");
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
        
        Device userFs;
        UserStore userStore;
        Device archiveFs;
        
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
            archiveFs = (Device)instance;
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
                System.out.println("\tCopying target to bag directory ...");
                String fileName = userStore.getName(filePath);
                File outputFile = bagPath.resolve(fileName).toFile();

                // Compute bytes to copy
                long bytes = userStore.getSize(filePath);
                
                eventStream.send(new ComputedSize(jobID, depositId, bytes));
                
                eventStream.send(new UpdateProgress(jobID, depositId, 0, bytes, "Starting transfer ...").withNextState(1));
                System.out.println("\tSize: " + bytes + " bytes (" +  FileUtils.byteCountToDisplaySize(bytes) + ")");
                
                // Progress tracking (threaded)
                Progress progress = new Progress();
                ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, bytes, eventStream);
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
                
                eventStream.send(new TransferComplete(jobID, depositId).withNextState(2));
                
                // Bag the directory in-place
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
                
                // Progress tracking (threaded)
                progress = new Progress();
                tracker = new ProgressTracker(progress, jobID, depositId, tarFile.length(), eventStream);
                trackerThread = new Thread(tracker);
                trackerThread.start();
                
                String archiveId;
                
                try {
                    archiveId = archiveFs.store("/", tarFile, progress);
                } finally {
                    // Stop the tracking thread
                    tracker.stop();
                    trackerThread.join();
                }
                
                System.out.println("\tCopied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
                
                // Cleanup
                System.out.println("\tCleaning up ...");
                FileUtils.deleteDirectory(bagDir);
                tarFile.delete();
                
                // TODO: should be a configurable step?
                System.out.println("\tValidating data ...");
                eventStream.send(new Complete(jobID, depositId, archiveId, archiveSize).withNextState(4));
                
                // Copy the tar file from the archive back to the temporary area
                Path tempPath = Paths.get(context.getTempDir());
                tarPath = tempPath.resolve(tarFileName);
                tarFile = tarPath.toFile();
                
                // Ask the driver to copy files to the temp directory
                progress = new Progress();
                archiveFs.retrieve(archiveId, tarFile, progress);
                System.out.println("\tCopied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");

                // Decompress to the temporary directory
                bagDir = Tar.unTar(tarFile, tempPath);
                
                // Validate the bagit directory
                if (!Packager.validateBag(bagDir)) {
                    throw new Exception("Bag is invalid");
                }
                
                // Cleanup
                System.out.println("\tCleaning up ...");
                FileUtils.deleteDirectory(bagDir);
                tarFile.delete();
                
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
}