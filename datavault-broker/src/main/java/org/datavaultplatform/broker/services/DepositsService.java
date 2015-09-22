package org.datavaultplatform.broker.services;

import java.util.Date;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.DepositDAO;

import java.util.UUID;
import java.util.List;

public class DepositsService {

    private DepositDAO depositDAO;
    
    public List<Deposit> getDeposits() {
        return depositDAO.list();
    }
    
    public void addDeposit(Vault vault, Deposit deposit) {
        
        Date d = new Date();
        deposit.setCreationTime(d);
        
        deposit.setVault(vault);
        deposit.setStatus(Deposit.Status.NOT_STARTED);
        
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
    
    public void setDepositDAO(DepositDAO depositDAO) {
        this.depositDAO = depositDAO;
    }

    public int count() { return depositDAO.count(); }

    public Long size() { return depositDAO.size(); }
}

