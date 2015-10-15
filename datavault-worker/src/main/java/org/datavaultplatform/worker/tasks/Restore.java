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
import org.datavaultplatform.common.storage.UserStore;
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
        String archiveId = properties.get("archiveId");
        long archiveSize = Long.parseLong(properties.get("archiveSize"));

        if (this.isRedeliver()) {
            eventStream.send(new Error(jobID, depositId, "Restore stopped: the message had been redelivered, please investigate"));
            return;
        }
        
        ArrayList<String> states = new ArrayList<>();
        states.add("Computing free space");    // 0
        states.add("Retrieving from archive"); // 1
        states.add("Transferring files");      // 2
        states.add("Data restore complete");   // 3
        eventStream.send(new InitStates(jobID, depositId, states));
        
        eventStream.send(new Event(jobID, depositId, "Data restore started"));
        eventStream.send(new UpdateState(jobID, depositId, 0)); // Debug
        
        System.out.println("\tbagID: " + bagID);
        System.out.println("\trestorePath: " + restorePath);
        
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
            eventStream.send(new Error(jobID, depositId, "Restore failed: could not access active filesystem"));
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
            eventStream.send(new Error(jobID, depositId, "Restore failed: could not access archive filesystem"));
            return;
        }
        
        try {
            if (!userStore.exists(restorePath) || !userStore.isDirectory(restorePath)) {
                // Target path must exist and be a directory
                System.out.println("\tTarget directory not found!");
            }
            
            // Check that there's enough free space ...
            try {
                long freespace = userFs.getUsableSpace();
                System.out.println("\tFree space: " + freespace + " bytes (" +  FileUtils.byteCountToDisplaySize(freespace) + ")");
                if (freespace < archiveSize) {
                    eventStream.send(new Error(jobID, depositId, "Not enough free space to restore data!"));
                    return;
                }
            } catch (Exception e) {
                System.out.println("Unable to determine free space");
                eventStream.send(new Event(jobID, depositId, "Unable to determine free space"));
            }

            // Retrieve the archived data
            String tarFileName = bagID + ".tar";
                        
            // Copy the tar file from the archive to the temporary area
            Path tarPath = Paths.get(context.getTempDir()).resolve(tarFileName);
            File tarFile = tarPath.toFile();
            
            eventStream.send(new UpdateState(jobID, depositId, 1, 0, archiveSize, "Starting transfer ...")); // Debug
            
            // Progress tracking (threaded)
            Progress progress = new Progress();
            ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, 1, archiveSize, eventStream);
            Thread trackerThread = new Thread(tracker);
            trackerThread.start();

            try {
                // Ask the driver to copy files to the temp directory
                archiveFs.retrieve(archiveId, tarFile, progress);
            } finally {
                // Stop the tracking thread
                tracker.stop();
                trackerThread.join();
            }
            
            System.out.println("\tCopied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
            
            // Copy the tar file to the target restore area
            System.out.println("\tCopying tar file from archive ...");
            eventStream.send(new UpdateState(jobID, depositId, 2, 0, archiveSize, "Starting transfer ...")); // Debug
            
            // Progress tracking (threaded)
            progress = new Progress();
            tracker = new ProgressTracker(progress, jobID, depositId, 2, archiveSize, eventStream);
            trackerThread = new Thread(tracker);
            trackerThread.start();
            
            try {
                // Ask the driver to copy files to the user directory
                // TODO this is a direct copy that doesn't use the temp directory.
                // Need to instantiate the archive store and call retrieve()
                userFs.store(restorePath, tarFile, progress);
            } finally {
                // Stop the tracking thread
                tracker.stop();
                trackerThread.join();
            }
            
            System.out.println("\tCopied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
            
            // Cleanup
            System.out.println("\tCleaning up ...");
            tarFile.delete();
            
            System.out.println("\tData restore complete: " + restorePath);
            eventStream.send(new Event(jobID, depositId, "Data restore completed"));
            eventStream.send(new UpdateState(jobID, depositId, 3)); // Debug
            
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(jobID, depositId, "Data restore failed: " + e.getMessage()));
        }
    }
}
