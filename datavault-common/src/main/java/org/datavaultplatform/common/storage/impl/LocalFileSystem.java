package org.datavaultplatform.common.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.storage.Verify;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

@Slf4j
public class LocalFileSystem extends Device implements UserStore, ArchiveStore {

    public static final String ROOT_PATH = "rootPath";
    private final String rootPath;


    public LocalFileSystem(String name, Map<String,String> config) throws FileNotFoundException {
        super(name, config);
        
        // Unpack the config parameters (in an implementation-specific way)
        rootPath = config.get(ROOT_PATH);
        Assert.isTrue(rootPath != null, () -> "rootPath cannot be null");
        // Verify parameters are correct.
        File file = new File(rootPath);
        if (!file.exists()) {
            log.error("LocalFileSystem: rootPath does not exist: {}", rootPath);
            throw new FileNotFoundException(rootPath);
        }
        Assert.isTrue(file.isDirectory(), () -> String.format("rootPath is not a directory[%s]", rootPath));
        Assert.isTrue(file.canRead(), () -> String.format("rootPath is not readable[%s]", rootPath));
        Assert.isTrue(file.canWrite(), () -> String.format("rootPath is not writable[%s]", rootPath));
    }

    @Override
    public List<FileInfo> list(String path) {
        
        Path basePath = Paths.get(rootPath);
        Path completePath = getAbsolutePath(path);

        if (completePath == null) {
            throw new IllegalArgumentException("Path invalid [" + path + "]");
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
            log.warn("Error listing files in path [" + path + "]", e);
        }

        return files;
    }
    
    @Override
    public boolean valid(String path) {
        Path absolutePath = getAbsolutePath(path);
        return (absolutePath != null);
    }

    @Override
    public boolean exists(String path) {
        Path absolutePath = getAbsolutePath(path);
        File file = absolutePath.toFile();
        return file.exists();
    }
    
    @Override
    public long getSize(String path) {
        Path absolutePath = getAbsolutePath(path);
        File file = absolutePath.toFile();
        
        if (file.isDirectory()) {
            return FileUtils.sizeOfDirectory(file);
        } else {
            return FileUtils.sizeOf(file);
        }
    }

    @Override
    public boolean isDirectory(String path) {
        Path absolutePath = getAbsolutePath(path);
        File file = absolutePath.toFile();
        return file.isDirectory();
    }
    
    @Override
    public String getName(String path) {
        Path absolutePath = getAbsolutePath(path);
        File file = absolutePath.toFile();
        return file.getName();
    }
    
    @Override
    public long getUsableSpace() {
        return FileSystemUtils.getUsableSpace(new File(rootPath));
    }

    @Override
    public void retrieve(String path, File working, Progress progress) throws Exception {
        FileSystemUtils.retrieve(path, working, progress, this.rootPath);
    }

    @Override
    public String store(String path, File working, Progress progress) throws Exception {
        return FileSystemUtils.store(path, working, progress, this.rootPath);
    }
    
    @Override
    public Verify.Method getVerifyMethod() {
        // Return the default verification method (copy back and check)
        return verificationMethod;
    }
    
    private Path getAbsolutePath(String filePath) {
        return FileSystemUtils.getAbsolutePath(filePath, this.rootPath);
    }

    
    @Override
    public void delete(String path, File working, Progress progress) throws Exception {
        FileSystemUtils.delete(path, this.rootPath);
    }

    @Override
    public Logger getLogger() {
        return this.log;
    }
}
