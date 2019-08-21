package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.dao.RetrieveDAO;

import java.util.Date;
import java.util.List;

public class RetrievesService {

    private RetrieveDAO retrieveDAO;
    
    public List<Retrieve> getRetrieves() {
        return retrieveDAO.list();
    }
    
    public void addRetrieve(Retrieve retrieve, Deposit deposit, String retrievePath) {
        
        Date d = new Date();
        retrieve.setTimestamp(d);
        
        retrieve.setDeposit(deposit);
        retrieve.setStatus(Retrieve.Status.NOT_STARTED);
        
        retrieve.setRetrievePath(retrievePath);
        
        retrieveDAO.save(retrieve);
    }
    
    public void updateRetrieve(Retrieve retrieve) {
        retrieveDAO.update(retrieve);
    }
    
    public Retrieve getRetrieve(String retrieveID) {
        return retrieveDAO.findById(retrieveID);
    }
    
    public void setRetrieveDAO(RetrieveDAO retrieveDAO) {
        this.retrieveDAO = retrieveDAO;
    }

    public int count(String userId) { return retrieveDAO.count(userId); }

    public int queueCount(String userId) { return retrieveDAO.queueCount(userId); }

    public int inProgressCount(String userId) { return retrieveDAO.inProgressCount(userId); }

    public List<Retrieve>inProgress() { return retrieveDAO.inProgress(); }
}

