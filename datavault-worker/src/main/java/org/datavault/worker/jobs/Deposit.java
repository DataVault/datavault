package org.datavault.worker.jobs;

import java.util.Map;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.datavault.common.job.Context;
import org.datavault.common.job.Job;

public class Deposit extends Job {

    @Override
    public void performAction(Context context) {
        
        System.out.println("\tDeposit job - performAction()");
        
        Map<String, String> properties = getProperties();
        String bagID = properties.get("bagId");
        String filePath = properties.get("filePath");
        
        System.out.println("\tbagID: " + bagID);
        System.out.println("\tfilePath: " + filePath);
        
        Path basePath = Paths.get(context.getActiveDir());
        File inputFile = basePath.resolve(filePath).toFile();
        
        System.out.println("\tDeposit file: " + inputFile.toString());

        try {
            if (inputFile.exists()) {

                // Create a new directory based on the broker-generated UUID
                java.nio.file.Path bagPath = java.nio.file.Paths.get(context.getTempDir(), bagID);
                java.io.File bagDir = bagPath.toFile();
                bagDir.mkdir();

                // Copy the target file to the bag directory
                System.out.println("\tCopying target to bag directory ...");
                String fileName = inputFile.getName();
                java.nio.file.Path outputPath = bagPath.resolve(fileName);

                if (inputFile.isFile()) {
                    org.apache.commons.io.FileUtils.copyFile(inputFile, outputPath.toFile());
                } else if (inputFile.isDirectory()) {
                    org.apache.commons.io.FileUtils.copyDirectory(inputFile, outputPath.toFile());
                }

                // Bag the directory in-place
                System.out.println("\tCreating bag ...");
                org.datavault.worker.operations.Packager.createBag(bagDir);

                // Tar the bag directory
                System.out.println("\tCreating tar file ...");
                String tarFileName = bagID + ".tar";
                java.nio.file.Path tarPath = java.nio.file.Paths.get(context.getTempDir()).resolve(tarFileName);
                java.io.File tarFile = tarPath.toFile();
                org.datavault.worker.operations.Tar.createTar(bagDir, tarFile);

                // Copy the resulting tar file to the archive area
                System.out.println("\tCopying tar file to archive ...");
                java.nio.file.Path archivePath = java.nio.file.Paths.get(context.getArchiveDir()).resolve(tarFileName);
                org.apache.commons.io.FileUtils.copyFile(tarFile, archivePath.toFile());
                
                System.out.println("\tDeposit complete: " + archivePath);
            } else {
                System.err.println("\tFile does not exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}