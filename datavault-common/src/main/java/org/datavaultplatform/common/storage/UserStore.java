package org.datavaultplatform.common.storage;

// Interface for user facing storage systems

import java.util.List;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;


public interface UserStore {
    
    // Properties and methods relating to user storage
    
    // List objects available under a given path
    List<FileInfo> list(String path);
    
    // Check if the passed path or resource key is allowed
    boolean valid(String path);
    
    // Check if an object exists at the specified path
    boolean exists(String path) throws Exception;
    
    // Get the size of an object (file/dir) in bytes
    long getSize(String path) throws Exception;
    
    // Get the name of an object
    String getName(String path) throws Exception;
    
    // Check if the passed path is a directory/container
    boolean isDirectory(String path) throws Exception;

    static UserStore fromFileStore(FileStore fileStore, StorageClassNameResolver resolver) {
        UserStore userStore = StorageClassUtils.createStorage(
            fileStore.getStorageClass(),
            fileStore.getProperties(),
            UserStore.class,
            resolver);
        return userStore;
    }
}
