package org.datavaultplatform.broker.services;

import java.util.List;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;

/**
 * User: Robin Taylor
 * Date: 19/03/2015
 * Time: 13:34
 */
public class FilesService {

    private Device fs;
    
    private boolean connect(FileStore fileStore) {
        try {
            fs = new LocalFileSystem(fileStore.getStorageClass(), fileStore.getProperties());
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

