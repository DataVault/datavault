package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.dao.FileStoreDAO;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FileStoreService {

    private final FileStoreDAO fileStoreDAO;

    public FileStoreService(FileStoreDAO fileStoreDAO) {
        this.fileStoreDAO = fileStoreDAO;
    }

    public List<FileStore> getFileStores() {
        return fileStoreDAO.list();
    }
    
    public void addFileStore(FileStore fileStore) {
        
        fileStoreDAO.save(fileStore);
    }
    
    public void updateFileStore(FileStore fileStore) {
        fileStoreDAO.update(fileStore);
    }
    
    public FileStore getFileStore(String fileStoreID) {
        return fileStoreDAO.findById(fileStoreID);
    }

    public void deleteFileStore(String fileStoreID) {
        fileStoreDAO.deleteById(fileStoreID);
    }
}

