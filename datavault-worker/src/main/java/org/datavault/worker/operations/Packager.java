package org.datavault.worker.operations;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
                */
                
                String entryFileName = entry.getFileName().toString();
                
                // Copy any regular text files at the top level (but not directories)
                if (!Files.isDirectory(entry) && entryFileName.endsWith(".txt")) {
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
