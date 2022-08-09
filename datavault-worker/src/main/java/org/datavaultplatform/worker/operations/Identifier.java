package org.datavaultplatform.worker.operations;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;

/**
 * A set of methods to identify file / directory types
 */
@Slf4j
public class Identifier {
    
    /**
     * Identify the type of the passed in file
     * @param file The file we want to identify
     * @return The MediaType or null if an exception occured
     */
    public static String detectFile(File file) {
        try {
            TikaConfig tika = new TikaConfig();
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.toString());
            try (InputStream is = TikaInputStream.get(file.toPath())) {
                MediaType mediaType = tika.getDetector().detect(is, metadata);
                return mediaType.toString();
            }
        } catch (Exception e) {
            log.error("unexpected exception", e);
            return null;
        }
    }

    /**
     * Identify the types of the files in a passed in directory, the method calls itself if the top level dir contains other dirs.
     * @param basePath The base dir path
     * @param path The path to the dir
     * @param result Map containing the results the key is an id to each file and the value is the identified file type
     */
    private static void detectDirectory(Path basePath, Path path, HashMap<String, String> result) {
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)){

            for (Path entry : stream) {
                
                String entryKey = (basePath.toUri().relativize(entry.toUri())).getPath();
                File entryFile = entry.toFile();
                
                if (entryFile.isDirectory()) {
                    detectDirectory(basePath, entry, result);
                } else {
                    String detected = detectFile(entryFile);
                    result.put(entryKey, detected);
                }
            }
            
        } catch (Exception e) {
            log.error("unexpected exception", e);
        }
    }
    
    /**
     * Identify the types of the files in a passed in directory
     * @param path The pass to the directory
     * @return Map containing a key to each file and the value is the Media type or null
     */
    public static HashMap<String, String> detectDirectory(Path path) {
        HashMap<String, String> result = new HashMap<>();
        detectDirectory(path, path, result);
        return result;
    }
}
