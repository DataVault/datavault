package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.ArchiveStore;
 
public interface ArchiveStoreDAO {

    public void save(ArchiveStore archiveStore);
    
    public void update(ArchiveStore archiveStore);
    
    public List<ArchiveStore> list();

    public ArchiveStore findById(String Id);

    public ArchiveStore findForRetrieval();

    public void deleteById(String Id);
    
}
