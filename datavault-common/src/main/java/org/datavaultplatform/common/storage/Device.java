package org.datavaultplatform.common.storage;

import org.datavaultplatform.common.io.Progress;
import java.util.Map;
import java.io.File;

// A generic storage UserStore/system

public abstract class Device {
    
    // Some public information about a device or storage system
    public String name;
    
    // Some private configuration properties
    protected Map<String,String> config;
    
    public Device(String name, Map<String,String> config) {
        this.name = name;
        this.config = config;
    }
    
    // How much space is available for storage (in bytes)
    public abstract long getUsableSpace() throws Exception;
    
    // Copy an object (file/dir) to the working space
    // Progress information should be updated for monitoring as the copy occurs
    public abstract void retrieve(String path, File working, Progress progress) throws Exception;
    
    // Copy an object (file/dir) from the working space
    // Progress information should be updated for monitoring as the copy occurs
    public abstract String store(String path, File working, Progress progress) throws Exception;
}
