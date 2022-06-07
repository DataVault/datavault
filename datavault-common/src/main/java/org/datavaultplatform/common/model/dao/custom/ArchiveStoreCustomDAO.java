package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.ArchiveStore;

public interface ArchiveStoreCustomDAO {

    public void save(ArchiveStore archiveStore);
    
    public void update(ArchiveStore archiveStore);
    
    public List<ArchiveStore> list();

    public ArchiveStore findById(String Id);

    public ArchiveStore findForRetrieval();

    public void deleteById(String Id);
    
}
