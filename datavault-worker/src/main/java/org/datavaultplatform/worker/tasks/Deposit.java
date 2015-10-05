package org.datavaultplatform.worker.tasks;

import java.util.Map;
import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.worker.operations.Tar;
import org.datavaultplatform.worker.operations.Packager;
import org.datavaultplatform.worker.queue.EventSender;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateState;
import org.datavaultplatform.common.event.deposit.Start;
import org.datavaultplatform.common.event.deposit.ComputedSize;
import org.datavaultplatform.common.event.deposit.TransferComplete;
import org.datavaultplatform.common.event.deposit.PackageComplete;
import org.datavaultplatform.common.event.deposit.Complete;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.io.FileCopy;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.worker.operations.ProgressTracker;

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
        
        // Deposit and Vault metadata to be stored in the bag
        // TODO: is there a better way to pass this to the worker?
        String depositMetadata = properties.get("depositMetadata");
        String vaultMetadata = properties.get("vaultMetadata");
        
        ArrayList<String> states = new ArrayList<>();
        states.add("Calculating size");   // 0
        states.add("Transferring files"); // 1
        states.add("Packaging data");     // 2
        states.add("Storing in archive"); // 3
        states.add("Deposit complete");   // 4
        eventStream.send(new InitStates(jobID, depositId, states));
        
        eventStream.send(new Start(jobID, depositId));
        
        System.out.println("\tbagID: " + bagID);
        System.out.println("\tfilePath: " + filePath);
        
        Device fs;
        
        try {
            Class<?> clazz = Class.forName(fileStore.getStorageClass());
            Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
            Object instance = constructor.newInstance(fileStore.getStorageClass(), fileStore.getProperties());
            fs = (Device)instance;
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(jobID, depositId, "Deposit failed: could not access active filesystem"));
            return;
        }
        
        System.out.println("\tDeposit file: " + filePath);

        try {
            if (fs.exists(filePath)) {

                // Create a new directory based on the broker-generated UUID
                Path bagPath = Paths.get(context.getTempDir(), bagID);
                File bagDir = bagPath.toFile();
                bagDir.mkdir();

                // Copy the target file to the bag directory
                System.out.println("\tCopying target to bag directory ...");
                String fileName = fs.getName(filePath);
                File outputFile = bagPath.resolve(fileName).toFile();

                // Compute bytes to copy
                eventStream.send(new UpdateState(jobID, depositId, 0)); // Debug
                long bytes = fs.getSize(filePath);
                
                eventStream.send(new ComputedSize(jobID, depositId, bytes));
                eventStream.send(new UpdateState(jobID, depositId, 1, 0, bytes, null)); // Debug
                System.out.println("\tSize: " + bytes + " bytes (" +  FileUtils.byteCountToDisplaySize(bytes) + ")");
                
                // Progress tracking (threaded)
                Progress progress = new Progress();
                ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, 1, bytes, eventStream);
                Thread trackerThread = new Thread(tracker);
                trackerThread.start();
                
                try {
                    // Ask the driver to copy files to our working directory
                    fs.copyToWorkingSpace(filePath, outputFile, progress);
                } finally {
                    // Stop the tracking thread
                    tracker.stop();
                    trackerThread.join();
                }
                
                eventStream.send(new TransferComplete(jobID, depositId));
                eventStream.send(new UpdateState(jobID, depositId, 2)); // Debug
                
                // Bag the directory in-place
                System.out.println("\tCreating bag ...");
                Packager.createBag(bagDir);

                // Add vault/deposit metadata to the bag
                Packager.addMetadata(bagDir, depositMetadata, vaultMetadata);
                
                // Tar the bag directory
                System.out.println("\tCreating tar file ...");
                String tarFileName = bagID + ".tar";
                Path tarPath = Paths.get(context.getTempDir()).resolve(tarFileName);
                File tarFile = tarPath.toFile();
                Tar.createTar(bagDir, tarFile);

                eventStream.send(new PackageComplete(jobID, depositId));
                eventStream.send(new UpdateState(jobID, depositId, 3)); // Debug
                
                // Create the meta directory for the bag information
                Path metaPath = Paths.get(context.getMetaDir(), bagID);
                File metaDir = metaPath.toFile();
                metaDir.mkdir();
                
                // Copy bag meta files to the meta directory
                System.out.println("\tCopying meta files ...");
                Packager.extractMetadata(bagDir, metaDir);
                
                // Copy the resulting tar file to the archive area
                System.out.println("\tCopying tar file to archive ...");
                Path archivePath = Paths.get(context.getArchiveDir()).resolve(tarFileName);
                
                // Progress tracking (threaded)
                progress = new Progress();
                tracker = new ProgressTracker(progress, jobID, depositId, 3, tarFile.length(), eventStream);
                trackerThread = new Thread(tracker);
                trackerThread.start();
                
                try {
                    FileCopy.copyFile(progress, tarFile, archivePath.toFile());
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
                
                System.out.println("\tDeposit complete: " + archivePath);
                eventStream.send(new Complete(jobID, depositId));
                eventStream.send(new UpdateState(jobID, depositId, 4)); // Debug
                
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