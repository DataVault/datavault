package org.datavault.common.storage;

import org.datavault.common.model.FileInfo;
import java.util.List;

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
    public abstract boolean validPath(String path);
    
    // Get the size of an object (file/dir) in bytes
    public abstract long getSize(String path);
    
    // How much space is available for storage (in bytes)
    public abstract long getUsableSpace();
}
