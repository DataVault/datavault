package org.datavault.worker.jobs;

import java.util.Map;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.datavault.common.job.Context;
import org.datavault.common.job.Job;
import org.datavault.worker.operations.Tar;
import org.datavault.worker.operations.Packager;
import org.datavault.worker.queue.EventSender;
import org.datavault.common.event.Event;
import org.datavault.common.event.Error;
import org.datavault.common.event.deposit.Start;
import org.datavault.common.event.deposit.ComputedSize;
import org.datavault.common.event.deposit.TransferComplete;
import org.datavault.common.event.deposit.PackageComplete;
import org.datavault.common.event.deposit.Complete;

import org.apache.commons.io.FileUtils;
import org.datavault.common.io.FileCopy;
import org.datavault.common.io.Progress;
import org.datavault.common.storage.Auth;
import org.datavault.common.storage.impl.LocalFileSystem;
import org.datavault.worker.operations.ProgressTracker;

public class Deposit extends Job {

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
        
        eventStream.send(new Start(depositId));
        
        System.out.println("\tbagID: " + bagID);
        System.out.println("\tfilePath: " + filePath);
        
        LocalFileSystem fs;
        
        try {
            String name = "filesystem";
            Auth auth = null;
            HashMap<String,String> config = new HashMap<>();
            config.put("rootPath", context.getActiveDir());
            fs = new LocalFileSystem(name, auth, config);
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(depositId, "Deposit failed: could not access active filesystem"));
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
                long bytes = fs.getSize(filePath);
                
                eventStream.send(new ComputedSize(depositId, bytes));
                System.out.println("\tSize: " + bytes + " bytes (" +  FileUtils.byteCountToDisplaySize(bytes) + ")");
                
                // Progress tracking (threaded)
                Progress progress = new Progress();
                ProgressTracker tracker = new ProgressTracker(progress, depositId, eventStream);
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
                
                eventStream.send(new TransferComplete(depositId));
                
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

                eventStream.send(new PackageComplete(depositId));
                
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
                progress = new Progress();
                FileCopy.copyFile(progress, tarFile, archivePath.toFile());
                System.out.println("\tCopied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
                
                // Cleanup
                System.out.println("\tCleaning up ...");
                FileUtils.deleteDirectory(bagDir);
                tarFile.delete();
                
                System.out.println("\tDeposit complete: " + archivePath);
                eventStream.send(new Complete(depositId));
                
            } else {
                System.err.println("\tFile does not exist.");
                eventStream.send(new Error(depositId, "Deposit failed: file not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(depositId, "Deposit failed: " + e.getMessage()));
        }
    }
}