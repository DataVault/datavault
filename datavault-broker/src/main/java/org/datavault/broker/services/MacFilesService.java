package org.datavault.broker.services;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import org.datavault.common.model.FileInfo;

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
    
    public List<FileInfo> getFilesListing(String filePath) {

        Path basePath = Paths.get(activeDir);
        Path completePath = getAbsolutePath(filePath);

        if (completePath == null) {
            throw new IllegalArgumentException("Path invalid");
        }

        ArrayList<FileInfo> files = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(completePath)) {
            for (Path entry : stream) {
                
                String entryFileName = entry.getFileName().toString();
                String entryAbsolutePath = entry.toString();
                
                // The "key" is the path under the base directory.
                // The API client can use this to request a sub-directory.
                String entryKey = (basePath.toUri().relativize(entry.toUri())).getPath();
                
                FileInfo info = new FileInfo(entryKey,
                                             entryAbsolutePath,
                                             entryFileName,
                                             Files.isDirectory(entry));
                files.add(info);
            }

        } catch (IOException e) {
            System.out.println(e.toString());
        }

        return files;
    }

    public Path getAbsolutePath(String filePath) {
        
        // Join the requested path to the root of the filesystem.
        // In future this path handling should be part of a filesystem-specific driver.
        Path base = Paths.get(activeDir);
        Path absolute;
        
        try {
            if (filePath.equals("")) {
                absolute = base;
            } else {
                // A leading '/' would cause the path to be treated as absolute
                while (filePath.startsWith("/")) {
                    filePath = filePath.replaceFirst("/", "");
                }

                absolute = base.resolve(filePath);
                absolute = Paths.get(absolute.toFile().getCanonicalPath());
            }

            if (isValidSubPath(absolute)) {
                return absolute;
            } else {
                // Path is invalid (doesn't exist in base)!
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean isValidSubPath(Path path) {
        
        // Check if the path is valid with respect to the base path.
        // For example, we don't want to allow path traversal ("../../abc").
        
        try {
            Path base = Paths.get(activeDir);
            Path canonicalBase = Paths.get(base.toFile().getCanonicalPath());
            Path canonicalPath = Paths.get(path.toFile().getCanonicalPath());
            
            if (canonicalPath.startsWith(canonicalBase)) {
                return true;
            } else {
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

