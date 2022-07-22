package org.datavaultplatform.common.storage;

// Interface for user facing storage systems

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;


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

    static UserStore fromFileStore(FileStore fileStore) throws Exception {
        Class<?> clazz = Class.forName(fileStore.getStorageClass());

        if (!UserStore.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                String.format("The class [%s] does not implement [%s]", clazz.getName(),
                    UserStore.class.getName()));
        }
        Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
        Object instance = constructor.newInstance(fileStore.getStorageClass(),
            fileStore.getProperties());
        UserStore userStore = (UserStore) instance;
        return userStore;
    }
}
