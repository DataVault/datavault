package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.Job;
 
public interface JobDAO {

    public void save(Job job);
    
    public void update(Job job);
    
    public List<Job> list();

    public Job findById(String Id);

    public int count();
}
