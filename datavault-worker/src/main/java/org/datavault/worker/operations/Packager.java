package org.datavault.worker.operations;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import gov.loc.repository.bagit.*;

public class Packager {
    
    // Create a bag from an existing directory.
    public static boolean createBag(File dir) throws Exception {

        BagFactory bagFactory = new BagFactory();
        PreBag preBag = bagFactory.createPreBag(dir);
        Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, false);
        
        boolean result = false;
        try {
            result = bag.verifyValid().isSuccess();
        } finally {
            bag.close();
        }
        
        return result;
    }
    
    // Extract the top-level metadata files from a bag and copy to a new directory.
    public static boolean extractMetaFiles(File bagDir, File metaDir) {
        
        // TODO: could we use the built-in "holey" bag methods instead?
        
        boolean result;
        Path bagPath = bagDir.toPath();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(bagPath)) {
            for (Path entry : stream) {
                
                /*
                Expected:
                - data (dir)
                - bag-info.txt
                - bagit.txt
                - manifest-md5.txt
                - tagmanifest-md5.txt
                - other metadata files or directories
                */
                
                String entryFileName = entry.getFileName().toString();

                if (Files.isDirectory(entry)) {
                    // Handle directories
                    if (entryFileName.equals("data")) {
                        // Create an empty "data" directory
                        Path metaDirPath = Paths.get(metaDir.toURI());
                        File emptyDataDir = metaDirPath.resolve("data").toFile();
                        emptyDataDir.mkdir();
                    } else {
                        FileUtils.copyDirectoryToDirectory(entry.toFile(), metaDir);
                    }
                
                } else if (!Files.isDirectory(entry)) {
                    // Handle files
                    FileUtils.copyFileToDirectory(entry.toFile(), metaDir);
                }
            }
            
            // All files copied
            result = true;
            
        } catch (IOException e) {
            System.out.println(e.toString());
            result = false;
        }
        
        return result;
    }
}
