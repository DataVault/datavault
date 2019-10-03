package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Retrieve;

import java.util.List;

public interface RetrieveDAO {

    public void save(Retrieve retrieve);
    
    public void update(Retrieve retrieve);
    
    public List<Retrieve> list(String userId);

    public Retrieve findById(String Id);

    public int count(String userId);

    public int inProgressCount(String userId);

    public List<Retrieve> inProgress();

    public int queueCount(String userId);
}
