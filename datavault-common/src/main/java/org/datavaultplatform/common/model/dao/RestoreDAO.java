package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Restore;

import java.util.List;

public interface RestoreDAO {

    public void save(Restore restore);
    
    public void update(Restore restore);
    
    public List<Restore> list();

    public Restore findById(String Id);

    public int count();

    public int inProgressCount();

    public List<Restore> inProgress();

    public int queueCount();
}
