package org.datavaultplatform.worker.operations;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

/**
 * A set of methods to indentify file / directory types
 */
public class Identifier {
    
    /**
     * Identify the type of the passed in file
     * @param file The file we want to identify
     * @return The MediaType or null if an exception occured
     */
    public static String detectFile(File file) {
        InputStream is = null;
        try {
            TikaConfig tika = new TikaConfig();
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, file.toString());
            is = TikaInputStream.get(file);
            MediaType mediaType = tika.getDetector().detect(is, metadata);
            return mediaType.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception e) {}
            }
        }
    }
    
    /**
     * Identify the types of the files in a passed in directory, the method calls itself if the top level dir contains other dirs.
     * @param basePath The base dir path
     * @param path The path to the dir
     * @param result Map containing the results the key is an id to each file and the value is the identified file type
     */
    private static void detectDirectory(Path basePath, Path path, HashMap<String, String> result) {
        
        DirectoryStream<Path> stream = null;
        
        try {
            stream = Files.newDirectoryStream(path);

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
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try { stream.close(); } catch (Exception e) {}
            }
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
