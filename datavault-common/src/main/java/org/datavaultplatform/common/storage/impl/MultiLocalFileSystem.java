package org.datavaultplatform.common.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.slf4j.Logger;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
public class MultiLocalFileSystem extends Device implements ArchiveStore {

    private static final String COMMA = ",";
    private final String rootPath;

    public MultiLocalFileSystem(String name, Map<String,String> config) throws FileNotFoundException {
        super(name, config);
        
        // Unpack the config parameters (in an implementation-specific way)
        rootPath = config.get(PropNames.ROOT_PATH);
        Assert.isTrue(rootPath != null, "The config to the MultiLocalFileSystem has a null 'rootPath' value");

        super.multipleCopies = true;

        String[] locationsArray = rootPath.split(COMMA);

        locations = new ArrayList<>();

        for (String rawLocation : locationsArray) {
            String location = rawLocation.trim();
            // Verify parameters are correct.
            File file = new File(location);
            if (!file.exists()) {
                throw new FileNotFoundException(location);
            }
            if (!file.isDirectory()) {
                throw new RuntimeException(String.format("The file[%s] is not a directory", location));
            }
            locations.add(location);
        }
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public long getUsableSpace() {
        long usageSpace = locations.stream()
                .map(File::new)
                .mapToLong(FileSystemUtils::getUsableSpace)
                .sum();
        return usageSpace;
    }

    @Override
    public void retrieve(String path, File working, Progress progress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void retrieve(String path, File working, Progress progress, String location) throws Exception {
        FileSystemUtils.retrieve(path, working, progress, location);
    }

    @Override
    public String store(String path, File working, Progress progress) throws Exception {
        log.info("Storing: "+working.toString());
        log.info("Nb of location: "+locations.size());
        for (String location : locations) {
            log.info("location: " + location);
            FileSystemUtils.store(path, working, progress, location);
        }

        return working.getName();
    }
    
    @Override
    public Verify.Method getVerifyMethod() {
        // Return the default verification method (copy back and check)
        return verificationMethod;
    }

    @Override
    public void delete(String path, File working, Progress progress, String location) throws Exception {
        FileSystemUtils.delete(path, location);
    }

    @Override
    public Logger getLogger() {
        return log;
    }
}
