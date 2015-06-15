package org.datavault.broker.services;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Robin Taylor
 * Date: 19/03/2015
 * Time: 13:34
 */
public class MacFilesService {

    private String activeDir;

    public void setactiveDir(String activeDir) {
        this.activeDir = activeDir;
    }
    
    public org.datavault.common.model.Files getFilesListing(String filePath) {

        // Join the requested path to the root of the filesystem.
        // In future this path handling should be part of a filesystem-specific driver.
        Path basePath = Paths.get(activeDir);
        Path completePath;
        
        if (filePath.equals("")) {
            completePath = basePath;
        } else {
            // A leading '/' will cause the path to be treated as absolute
            while (filePath.startsWith("/")) {
                filePath = filePath.replaceFirst("/", "");
            }
            
            completePath = basePath.resolve(filePath);
        }
        
        org.datavault.common.model.Files files = new org.datavault.common.model.Files();

        Map filesMap = new HashMap<String, String>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(completePath)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    filesMap.put(entry.toString(), "directory");
                } else {
                    filesMap.put(entry.toString(), "file");
                }

            }

        } catch (IOException e) {
            System.out.println(e.toString());
        }

        files.setFilesMap(filesMap);

        return files;


    }


}

