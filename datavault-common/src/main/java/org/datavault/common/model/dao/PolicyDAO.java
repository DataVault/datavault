package org.datavault.common.model.dao;

import java.util.List;
import org.datavault.common.model.Policy;
 
public interface PolicyDAO {

    public void save(Policy policy);
    
    public void update(Policy policy);
    
    public List<Policy> list();

    public Policy findById(String Id);
}
