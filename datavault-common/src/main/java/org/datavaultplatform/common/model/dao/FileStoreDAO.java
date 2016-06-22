package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.FileStore;
 
public interface FileStoreDAO {

    public void save(FileStore fileStore);
    
    public void update(FileStore fileStore);
    
    public List<FileStore> list();

    public FileStore findById(String Id);

    public void deleteById(String Id);
}
