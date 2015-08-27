package org.datavault.worker.jobs;

import java.util.Map;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.datavault.common.job.Context;
import org.datavault.common.job.Job;
import org.datavault.worker.queue.EventSender;
import org.datavault.common.event.Event;
import org.datavault.common.event.Error;

import org.apache.commons.io.FileUtils;
import org.datavault.common.io.Progress;
import org.datavault.common.storage.impl.LocalFileSystem;
import org.datavault.worker.operations.ProgressTracker;

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
        
        LocalFileSystem fs;
        
        try {
            String name = "filesystem";
            String auth = "";
            fs = new LocalFileSystem(name, auth, context.getActiveDir());
        } catch (Exception e) {
            e.printStackTrace();
            eventStream.send(new Error(depositId, "Restore failed: could not access active filesystem"));
            return;
        }
        
        // Check that there's enough free space ...
        System.out.println("Free space: " + fs.getUsableSpace() + " bytes");
        
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
