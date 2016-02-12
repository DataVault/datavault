package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.Dataset;
 
public interface DatasetDAO {

    public void save(Dataset dataset);
    
    public void update(Dataset dataset);
    
    public List<Dataset> list();

    public Dataset findById(String Id);
}
