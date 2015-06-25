package org.datavault.worker.jobs;

import java.util.Map;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.datavault.common.job.Context;
import org.datavault.common.job.Job;
import org.datavault.worker.operations.Tar;
import org.datavault.worker.operations.Packager;
import org.datavault.worker.queue.EventSender;

import org.apache.commons.io.FileUtils;
import org.datavault.common.event.Event;
import org.datavault.common.event.deposit.NotifySize;


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
        
        eventStream.send(new Event(depositId, "Deposit started"));
        
        System.out.println("\tbagID: " + bagID);
        System.out.println("\tfilePath: " + filePath);
        
        File inputFile = Paths.get(filePath).toFile();
        
        System.out.println("\tDeposit file: " + inputFile.toString());

        try {
            if (inputFile.exists()) {

                // Create a new directory based on the broker-generated UUID
                Path bagPath = Paths.get(context.getTempDir(), bagID);
                File bagDir = bagPath.toFile();
                bagDir.mkdir();

                // Copy the target file to the bag directory
                System.out.println("\tCopying target to bag directory ...");
                String fileName = inputFile.getName();
                File outputFile = bagPath.resolve(fileName).toFile();

                long bytes = 0;
                
                if (inputFile.isFile()) {
                    FileUtils.copyFile(inputFile, outputFile);
                    bytes = FileUtils.sizeOf(outputFile);
                } else if (inputFile.isDirectory()) {
                    FileUtils.copyDirectory(inputFile, outputFile);
                    bytes = FileUtils.sizeOfDirectory(outputFile);
                }
                
                eventStream.send(new NotifySize(depositId, bytes));
                
                System.out.println("\tSize: " + bytes + " bytes (" +  FileUtils.byteCountToDisplaySize(bytes) + ")");
                
                // Bag the directory in-place
                System.out.println("\tCreating bag ...");
                Packager.createBag(bagDir);

                // Tar the bag directory
                System.out.println("\tCreating tar file ...");
                String tarFileName = bagID + ".tar";
                Path tarPath = Paths.get(context.getTempDir()).resolve(tarFileName);
                File tarFile = tarPath.toFile();
                Tar.createTar(bagDir, tarFile);

                // Create the meta directory for the bag information
                Path metaPath = Paths.get(context.getMetaDir(), bagID);
                File metaDir = metaPath.toFile();
                metaDir.mkdir();
                
                // Copy bag meta files to the meta directory
                System.out.println("\tCopying meta files ...");
                Packager.extractMetaFiles(bagDir, metaDir);
                
                // Copy the resulting tar file to the archive area
                System.out.println("\tCopying tar file to archive ...");
                Path archivePath = Paths.get(context.getArchiveDir()).resolve(tarFileName);
                FileUtils.copyFile(tarFile, archivePath.toFile());
                
                // Cleanup
                System.out.println("\tCleaning up ...");
                FileUtils.deleteDirectory(bagDir);
                tarFile.delete();
                
                System.out.println("\tDeposit complete: " + archivePath);
            } else {
                System.err.println("\tFile does not exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}