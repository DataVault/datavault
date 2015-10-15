package org.datavaultplatform.worker.tasks;

import java.util.Map;
import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;

import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.worker.queue.EventSender;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateState;

import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.worker.operations.ProgressTracker;

public class Restore extends Task {
    
    @Override
    public void performAction(Context context) {
        
        EventSender eventStream = (EventSender)context.getEventStream();
        
        System.out.println("\tRestore job - performAction()");
        
        Map<String, String> properties = getProperties();
        String depositId = properties.get("depositId");
        String bagID = properties.get("bagId");
        String restorePath = properties.get("restorePath");

        if (this.isRedeliver()) {
            eventStream.send(new Error(jobID, depositId, "Restore stopped: the message had been redelivered, please investigate"));
            return;
        }
        
        ArrayList<String> states = new ArrayList<>();
        states.add("Computing free space");    // 0
        states.add("Transferring files");      // 1
        states.add("Data restore complete");   // 2
        eventStream.send(new InitStates(jobID, depositId, states));
        
        eventStream.send(new Event(jobID, depositId, "Data restore started"));
        eventStream.send(new UpdateState(jobID, depositId, 0)); // Debug
        
        System.out.println("\tbagID: " + bagID);
        System.out.println("\trestorePath: " + restorePath);
        
        Device fs;
        
        try {
            Class<?> clazz = Class.forName(fileStore.getStorageClass());
            Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
            Object instance = constructor.newInstance(fileStore.getStorageClass(), fileStore.getProperties());
            fs = (Device)instance;
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(jobID, depositId, "Restore failed: could not access active filesystem"));
            return;
        }
        
        try {
            if (!fs.exists(restorePath) || !fs.isDirectory(restorePath)) {
                // Target path must exist and be a directory
                System.out.println("\tTarget directory not found!");
            }

            // Find the archived data
            // TODO: a filesystem driver should transform a bagID to an actual path
            String tarFileName = bagID + ".tar";
            Path archivePath = Paths.get(context.getArchiveDir()).resolve(tarFileName);
            File archiveFile = archivePath.toFile();
            long archiveFileSize = archiveFile.length();
            
            // Check that there's enough free space ...
            try {
                long freespace = fs.getUsableSpace();
                System.out.println("\tFree space: " + freespace + " bytes (" +  FileUtils.byteCountToDisplaySize(freespace) + ")");
                if (freespace < archiveFileSize) {
                    eventStream.send(new Error(jobID, depositId, "Not enough free space to restore data!"));
                    return;
                }
            } catch (Exception e) {
                System.out.println("Unable to determine free space");
                eventStream.send(new Event(jobID, depositId, "Unable to determine free space"));
            }
            
            // Copy the tar file to the target restore area
            System.out.println("\tCopying tar file from archive ...");
            eventStream.send(new UpdateState(jobID, depositId, 1, 0, archiveFileSize, "Starting transfer ...")); // Debug
            
            // Progress tracking (threaded)
            Progress progress = new Progress();
            ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, 1, archiveFileSize, eventStream);
            Thread trackerThread = new Thread(tracker);
            trackerThread.start();

            try {
                // Ask the driver to copy files to the user directory
                fs.copyFromWorkingSpace(restorePath, archiveFile, progress);
            } finally {
                // Stop the tracking thread
                tracker.stop();
                trackerThread.join();
            }
            
            System.out.println("\tCopied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
            
            System.out.println("\tData restore complete: " + restorePath);
            eventStream.send(new Event(jobID, depositId, "Data restore completed"));
            eventStream.send(new UpdateState(jobID, depositId, 2)); // Debug
            
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(jobID, depositId, "Data restore failed: " + e.getMessage()));
        }
    }
}
