package org.datavaultplatform.common.storage.impl;

import org.datavaultplatform.common.io.FileCopy;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class MultiLocalFileSystem extends Device implements ArchiveStore {

    private String rootPath = null;

    public MultiLocalFileSystem(String name, Map<String,String> config) throws FileNotFoundException {
        super(name, config);
        
        // Unpack the config parameters (in an implementation-specific way)
        rootPath = config.get("rootPath");

        super.multipleCopies = true;

        String[] locationsArray = rootPath.split(",");

        locations = new ArrayList<String>();

        for(String location : locationsArray){
            // Verify parameters are correct.
            File file = new File(location);
            if (!file.exists()) {
                throw new FileNotFoundException(location);
            }
            locations.add(location);
        }
    }

    @Override
    public long getUsableSpace() throws Exception {
        long usageSpace = 0;
        for(int i = 0; i < locations.size(); i++) {
            String location = locations.get(i);
            File file = new File(location);
            usageSpace += file.getUsableSpace();
        }
        return usageSpace;
    }

    @Override
    public void retrieve(String path, File working, Progress progress) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void retrieve(String path, File working, Progress progress, String location) throws Exception {
        // @TODO: copy from location
        Path absolutePath = getAbsolutePath(path, location);
        File file = absolutePath.toFile();

        if (file.isFile()) {
            FileCopy.copyFile(progress, file, working);
        } else if (file.isDirectory()) {
            FileCopy.copyDirectory(progress, file, working);
        }
    }

    @Override
    public String store(String path, File working, Progress progress) throws Exception {
        System.out.println("Storing: "+working.toString());
        System.out.println("Nb of location: "+locations.size());
        for(int i = 0; i < locations.size(); i++){
            String location = locations.get(i);
            System.out.println("location: "+location);
            Path absolutePath = getAbsolutePath(path, location);
            File retrieveFile = absolutePath.resolve(working.getName()).toFile();

            if (working.isFile()) {
                FileCopy.copyFile(progress, working, retrieveFile);
            } else if (working.isDirectory()) {
                FileCopy.copyDirectory(progress, working, retrieveFile);
            }
        }

        return working.getName();
    }
    
    @Override
    public Verify.Method getVerifyMethod() {
        // Return the default verification method (copy back and check)
        return verificationMethod;
    }
    
    private Path getAbsolutePath(String filePath, String location) {

        // Join the requested path to the root of the filesystem.
        // In future this path handling should be part of a filesystem-specific driver.
        Path base = Paths.get(location);
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

            if (isValidSubPath(absolute, location)) {
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
    
    private boolean isValidSubPath(Path path, String location) {

        // Check if the path is valid with respect to the base path.
        // For example, we don't want to allow path traversal ("../../abc").
        
        try {
            Path base = Paths.get(location);
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
    
    @Override
    public void delete(String path, File working, Progress progress, String location) throws Exception {
        Path absolutePath = getAbsolutePath(path, location);
        Files.deleteIfExists(absolutePath);
    }
}
