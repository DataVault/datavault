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

    public org.datavault.common.model.Files getFilesListing(String filePath) {

        org.datavault.common.model.Files files = new org.datavault.common.model.Files();

        Map filesMap = new HashMap<String, String>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(filePath))) {
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

