package org.datavaultplatform.common.storage;

import org.datavaultplatform.common.io.Progress;

import java.io.File;
import java.util.List;
import java.util.Map;

// A generic storage UserStore/system

public abstract class Device {
    
    // Some public information about a device or storage system
    public String name;
    
    
    // Some private configuration properties
    protected Map<String,String> config;
    protected Boolean multipleCopies = false;
    protected List<String> locations = null;
    protected Boolean depositIdStorageKey = false;
    
    public Device(String name, Map<String,String> config) {
        this.name = name;
        this.config = config;
    }
    
    // How much space is available for storage (in bytes)
    public abstract long getUsableSpace() throws Exception;
    
    // Copy an object (file/dir) to the working space
    // Progress information should be updated for monitoring as the copy occurs
    public abstract void retrieve(String path, File working, Progress progress) throws Exception;
    
    // This method should be overridden by plugins that allow the retrieval of a specific copy id'd by the vaultId
    //public void retrieve(String path, File working, Progress progress, String depositId) throws Exception {
    	//	throw new UnsupportedOperationException();
    //}
    
    // This method should be overridden by plugins that allow the retrieval of multiple copies to get a specific copy
    public void retrieve(String path, File working, Progress progress, String location) throws Exception {
    		throw new UnsupportedOperationException();
    }
    // Copy an object (file/dir) from the working space
    // Progress information should be updated for monitoring as the copy occurs
    public abstract String store(String path, File working, Progress progress) throws Exception;
    
    //public String store(String path, File working, Progress progress, String depositId) throws Exception {
    //		throw new UnsupportedOperationException();
    //}
    
    public Boolean hasMultipleCopies() {
    		return this.multipleCopies;
    }
    
    public Boolean hasDepositIdStorageKey() {
		return this.depositIdStorageKey;
    }
    
    public List<String> getLocations() {
    		return this.locations;
    }
}
