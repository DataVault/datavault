package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.RetentionPolicy;

public interface RetentionPolicyDAO {

    public void save(RetentionPolicy retentionPolicy);
    
    public void update(RetentionPolicy retentionPolicy);
    
    public List<RetentionPolicy> list();

    public RetentionPolicy findById(String Id);

    public void delete(String id);
}
