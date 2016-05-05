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

public class Identifier {
    
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
    
    private static void detectDirectory(Path basePath, Path path, HashMap<String, String> result) {
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(path);

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
        }
    }
    
    public static HashMap<String, String> detectDirectory(Path path) {
        HashMap<String, String> result = new HashMap<>();
        detectDirectory(path, path, result);
        return result;
    }
}
