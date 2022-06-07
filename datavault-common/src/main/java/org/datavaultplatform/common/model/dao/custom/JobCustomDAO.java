package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.Job;

public interface JobCustomDAO {

    public void save(Job job);
    
    public void update(Job job);
    
    public List<Job> list();

    public Job findById(String Id);

    public int count();
}
