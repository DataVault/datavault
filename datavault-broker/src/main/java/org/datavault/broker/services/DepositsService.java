package org.datavault.broker.services;

import java.util.Date;
import org.datavault.common.model.Deposit;
import org.datavault.common.model.Vault;
import org.datavault.common.model.dao.DepositDAO;

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
}

