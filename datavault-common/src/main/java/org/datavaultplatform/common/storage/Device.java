package org.datavaultplatform.common.storage;

import org.datavaultplatform.common.io.Progress;
import org.slf4j.Logger;
import org.springframework.util.Assert;

import java.io.File;
import java.util.List;
import java.util.Map;

// A generic storage UserStore/system

public abstract class Device {
    
    // Some public information about a device or storage system
    public final String name;
    
    
    // Some private configuration properties
    protected final Map<String,String> config;
    protected Boolean multipleCopies = false;
    protected List<String> locations = null;
    protected Boolean depositIdStorageKey = false;
    
    public Device(String name, Map<String,String> config) {
        Assert.isTrue(name != null, "The device name cannot be null");
        Assert.isTrue(config != null, "The device config map cannot be null");
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

    public void retrieve(String path, File working, Progress progress, List<String> locations) throws Exception {
        Assert.isTrue(locations != null, "locations must not be null");

        int numLocations = locations.size();
        int finalLocation = numLocations - 1;
        boolean retrieved = false;

        for (int i = 0; !retrieved && i < numLocations; i++) {
            String location = locations.get(i);
            String context = String.format("retrieve[%s] from location(%d/%d)[%s]", path, i + 1, numLocations, location);
            try {
                retrieve(path, working, progress, location);
                getLogger().info("{} Retrieved!", context);
                retrieved = true;
            } catch (Exception ex) {
                if (i == finalLocation) {
                    getLogger().error("{} error using final location", context, ex);
                    throw ex;
                } else {
                    if (getLogger().isTraceEnabled()) {
                        getLogger().trace("{} problem", context, ex);
                    } else {
                        getLogger().info("{} problem [{}]", context, ex.getMessage());
                    }
                    getLogger().info("{} trying next location...", context);
                }
            }
        }
    }
    // Copy an object (file/dir) from the working space
    // Progress information should be updated for monitoring as the copy occurs
    public String store(String path, File working, Progress progress) throws Exception {
        throw new UnsupportedOperationException();
    }

    public String store(String path, File working, Progress progress, String timeStampDirname) throws Exception {
        throw new UnsupportedOperationException();
    }
    
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

	//This method should be overridden by plugins that allow delete
	public void delete(String depositId, File working, Progress progress, String optFilePath) throws Exception {
		throw new UnsupportedOperationException();
	}
	
	public void delete(String depositId, File working, Progress progress) throws Exception {
		throw new UnsupportedOperationException();
	}

    public abstract Logger getLogger();
}
