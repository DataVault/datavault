package org.datavaultplatform.worker.jobs;

import java.util.Map;
import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

import org.datavaultplatform.common.job.Context;
import org.datavaultplatform.common.job.Job;
import org.datavaultplatform.worker.queue.EventSender;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.Error;

import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;

public class Restore extends Job {
    
    @Override
    public void performAction(Context context) {
        
        EventSender eventStream = (EventSender)context.getEventStream();
        
        System.out.println("\tRestore job - performAction()");
        
        Map<String, String> properties = getProperties();
        String depositId = properties.get("depositId");
        String bagID = properties.get("bagId");
        String restorePath = properties.get("restorePath");
        
        eventStream.send(new Event(depositId, "Data restore started"));
        
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
            eventStream.send(new Error(depositId, "Restore failed: could not access active filesystem"));
            return;
        }
        
        /*        
        try {
            fs = new SFTPFileSystem(fileStore.getStorageClass(), fileStore.getProperties());
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(depositId, "Deposit failed: could not access active filesystem"));
            return;
        }
        */
        
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
            
            // Check that there's enough free space ...
            try {
                long freespace = fs.getUsableSpace();
                System.out.println("\tFree space: " + freespace + " bytes (" +  FileUtils.byteCountToDisplaySize(freespace) + ")");
                if (freespace < archiveFile.length()) {
                    eventStream.send(new Error(depositId, "Not enough free space to restore data!"));
                    return;
                }
            } catch (Exception e) {
                System.out.println("Unable to determine free space");
                eventStream.send(new Event(depositId, "Unable to determine free space"));
            }
            
            // Copy the tar file to the target restore area
            System.out.println("\tCopying tar file from archive ...");
            
            Progress progress = new Progress();
            // TODO: live progress tracking similar to deposit?
            // Need a more generalised "Task" db structure (new table?)
            
            fs.copyFromWorkingSpace(restorePath, archiveFile, progress);
            
            System.out.println("\tCopied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
            
            System.out.println("\tData restore complete: " + restorePath);
            eventStream.send(new Event(depositId, "Data restore completed"));
            
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(depositId, "Data restore failed: " + e.getMessage()));
        }
    }
}
