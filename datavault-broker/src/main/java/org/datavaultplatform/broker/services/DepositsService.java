package org.datavaultplatform.broker.services;

import java.util.Date;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.DepositDAO;

import java.util.UUID;
import java.util.List;

public class DepositsService {

    private DepositDAO depositDAO;
    
    public List<Deposit> getDeposits(String sort, String userId) {
        return depositDAO.list(sort, userId);
    }
    
    public void addDeposit(Vault vault,
                           Deposit deposit,
                           String shortPath,
                           String origin) {
        
        Date d = new Date();
        deposit.setCreationTime(d);
        
        deposit.setVault(vault);
        deposit.setStatus(Deposit.Status.NOT_STARTED);
        
        // Set display values for the deposit path/origin
        deposit.setShortFilePath(shortPath);
        deposit.setFileOrigin(origin);
        
        // Generate a new UUID for this Bag.
        deposit.setBagId(UUID.randomUUID().toString());
        
        depositDAO.save(deposit);
    }
    
    public void updateDeposit(Deposit deposit) {
        depositDAO.update(deposit);
    }
    
    public Deposit getDeposit(String depositID) {
        return depositDAO.findById(depositID);
    }
    
    public void setDepositDAO(DepositDAO depositDAO) { this.depositDAO = depositDAO; }

    public int count(String userId) { return depositDAO.count(userId); }

    public int queueCount(String userId) { return depositDAO.queueCount(userId); }

    public int inProgressCount(String userId) { return depositDAO.inProgressCount(userId); }

    public List<Deposit> inProgress() { return depositDAO.inProgress(); }

    public List<Deposit> completed() { return depositDAO.completed(); }

    public List<Deposit> search(String query, String sort, String userId) { return this.depositDAO.search(query, sort, userId); }

    public Long size(String userId) { return depositDAO.size(userId); }
    
    // Get the specified Deposit object and validate it against the current User and Vault
    public Deposit getUserDeposit(User user, String depositID) throws Exception {
        
        Deposit deposit = getDeposit(depositID);
        
        if (deposit == null) {
            throw new Exception("Invalid Deposit ID");
        }
        
        Vault vault = deposit.getVault();
        
        if (vault == null) {
            throw new Exception("Vault does not exist");
        }
        
        return deposit;
    }
    
    public void deleteDeposit(String depositId) {
    	Deposit deposit = depositDAO.findById(depositId);
    	deposit.setSize(0);
    	depositDAO.update(deposit);
    }
}

