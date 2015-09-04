package org.datavaultplatform.broker.services;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.storage.Device;

/**
 * User: Robin Taylor
 * Date: 19/03/2015
 * Time: 13:34
 */
public class FilesService {

    private Device fs;
    
    private boolean connect(FileStore fileStore) {
        try {
            Class<?> clazz = Class.forName(fileStore.getStorageClass());
            Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
            Object instance = constructor.newInstance(fileStore.getStorageClass(), fileStore.getProperties());
            fs = (Device)instance;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<FileInfo> getFilesListing(String filePath, FileStore fileStore) {
        if (connect(fileStore)) {
            return fs.list(filePath);
        } else {
            return null;
        }
    }
    
    public boolean validPath(String filePath, FileStore fileStore) {
        connect(fileStore);
        return fs.valid(filePath);
    }
}

