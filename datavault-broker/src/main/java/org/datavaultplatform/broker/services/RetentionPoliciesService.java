package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.dao.RetentionPolicyDAO;

import java.util.List;

public class RetentionPoliciesService {

    private RetentionPolicyDAO retentionPolicyDAO;
    
    public List<RetentionPolicy> getRetentionPolicies() {
        return retentionPolicyDAO.list();
    }
    
    public void addRetentionPolicy(RetentionPolicy retentionPolicy) {
        retentionPolicyDAO.save(retentionPolicy);
    }
    
    public void updateRetentionPolicy(RetentionPolicy retentionPolicy) {
        retentionPolicyDAO.update(retentionPolicy);
    }
    
    public RetentionPolicy getPolicy(String policyID) {
        return retentionPolicyDAO.findById(policyID);
    }
    
    public void setRetentionPolicyDAO(RetentionPolicyDAO retentionPolicyDAO) {
        this.retentionPolicyDAO = retentionPolicyDAO;
    }

    public void delete(String policyID) {
        retentionPolicyDAO.delete(policyID);
    }
}

