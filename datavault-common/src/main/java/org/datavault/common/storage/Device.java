package org.datavault.common.storage;

import org.datavault.common.model.FileInfo;
import org.datavault.common.io.Progress;
import java.util.List;
import java.io.File;

// A generic storage Device/system

public abstract class Device {
    
    // Some public information about a device or storage system
    public String name;
    
    // Some private configuration properties
    private String auth;
    private String config;
    
    public Device(String name, String auth, String config) {
        this.name = name;
        this.auth = auth;
        this.config = config;
    }
    
    // List objects available under a given path
    public abstract List<FileInfo> list(String path);
    
    // Check if the passed path or resource key is allowed
    public abstract boolean valid(String path);
    
    // Check if an object exists at the specified path
    public abstract boolean exists(String path);
    
    // Get the size of an object (file/dir) in bytes
    public abstract long getSize(String path);
    
    // Get the name of an object
    public abstract String getName(String path);
    
    // Check if the passed path is a directory/container
    public abstract boolean isDirectory(String path);
    
    // How much space is available for storage (in bytes)
    public abstract long getUsableSpace();
    
    // Copy an object (file/dir) to the working space
    // Progress information should be updated for monitoring as the copy occurs
    public abstract void copyToWorkingSpace(String path, File working, Progress progress) throws Exception;
    
    // Copy an object (file/dir) from the working space
    // Progress information should be updated for monitoring as the copy occurs
    public abstract void copyFromWorkingSpace(String path, File working, Progress progress) throws Exception;
}
