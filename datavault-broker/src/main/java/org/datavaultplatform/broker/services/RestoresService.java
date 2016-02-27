package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Restore;
import org.datavaultplatform.common.model.dao.RestoreDAO;

import java.util.Date;
import java.util.List;

public class RestoresService {

    private RestoreDAO restoreDAO;
    
    public List<Restore> getRestores() {
        return restoreDAO.list();
    }
    
    public void addRestore(Restore restore, Deposit deposit, String restorePath) {
        
        Date d = new Date();
        restore.setTimestamp(d);
        
        restore.setDeposit(deposit);
        restore.setStatus(Restore.Status.NOT_STARTED);
        
        restore.setRestorePath(restorePath);
        
        restoreDAO.save(restore);
    }
    
    public void updateRestore(Restore restore) {
        restoreDAO.update(restore);
    }
    
    public Restore getRestore(String restoreID) {
        return restoreDAO.findById(restoreID);
    }
    
    public void setRestoreDAO(RestoreDAO restoreDAO) {
        this.restoreDAO = restoreDAO;
    }

    public int count() { return restoreDAO.count(); }

    public int queueCount() { return restoreDAO.queueCount(); }

    public int inProgressCount() { return restoreDAO.inProgressCount(); }

    public List<Restore>inProgress() { return restoreDAO.inProgress(); }
}

