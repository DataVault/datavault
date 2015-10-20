package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.dao.ArchiveStoreDAO;

import java.util.List;

public class ArchiveStoreService {

    private ArchiveStoreDAO archiveStoreDAO;
    
    public List<ArchiveStore> getArchiveStores() {
        return archiveStoreDAO.list();
    }
    
    public void addArchiveStore(ArchiveStore archiveStore) {
        
        archiveStoreDAO.save(archiveStore);
    }
    
    public void updateArchiveStore(ArchiveStore archiveStore) {
        archiveStoreDAO.update(archiveStore);
    }
    
    public ArchiveStore getArchiveStore(String archiveStoreID) {
        return archiveStoreDAO.findById(archiveStoreID);
    }
    
    public void setArchiveStoreDAO(ArchiveStoreDAO archiveStoreDAO) {
        this.archiveStoreDAO = archiveStoreDAO;
    }
}

