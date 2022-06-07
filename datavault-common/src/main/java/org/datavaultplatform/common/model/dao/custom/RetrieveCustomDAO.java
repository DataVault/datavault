package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.Retrieve;

public interface RetrieveCustomDAO {

    public void save(Retrieve retrieve);
    
    public void update(Retrieve retrieve);
    
    public List<Retrieve> list(String userId);

    public Retrieve findById(String Id);

    public int count(String userId);

    public int inProgressCount(String userId);

    public List<Retrieve> inProgress();

    public int queueCount(String userId);
}
