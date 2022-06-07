package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.DataManager;

public interface DataManagerCustomDAO {

    public void save(DataManager dataManager);
    
    public List<DataManager> list();

    public DataManager findById(String Id);

    public void deleteById(String Id);
}
