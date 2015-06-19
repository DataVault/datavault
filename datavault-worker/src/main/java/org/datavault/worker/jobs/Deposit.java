package org.datavault.worker.jobs;

import java.util.Map;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.datavault.common.job.Context;
import org.datavault.common.job.Job;
import org.datavault.worker.operations.Tar;
import org.datavault.worker.operations.Packager;
import org.apache.commons.io.FileUtils;

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
                Path bagPath = Paths.get(context.getTempDir(), bagID);
                File bagDir = bagPath.toFile();
                bagDir.mkdir();

                // Copy the target file to the bag directory
                System.out.println("\tCopying target to bag directory ...");
                String fileName = inputFile.getName();
                Path outputPath = bagPath.resolve(fileName);

                if (inputFile.isFile()) {
                    FileUtils.copyFile(inputFile, outputPath.toFile());
                } else if (inputFile.isDirectory()) {
                    FileUtils.copyDirectory(inputFile, outputPath.toFile());
                }

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