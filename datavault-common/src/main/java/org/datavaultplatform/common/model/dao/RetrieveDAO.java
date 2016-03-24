package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Retrieve;

import java.util.List;

public interface RetrieveDAO {

    public void save(Retrieve retrieve);
    
    public void update(Retrieve retrieve);
    
    public List<Retrieve> list();

    public Retrieve findById(String Id);

    public int count();

    public int inProgressCount();

    public List<Retrieve> inProgress();

    public int queueCount();
}
