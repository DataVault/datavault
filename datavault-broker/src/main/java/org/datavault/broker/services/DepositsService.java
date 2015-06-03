package org.datavault.broker.services;

import org.datavault.common.model.Deposit;
import org.datavault.common.model.dao.DepositDAO;

import java.util.List;

public class DepositsService {

    private DepositDAO depositDAO;
    
    public List<Deposit> getDeposits() {
        return depositDAO.list();
    }
    
    public void addDeposit(Deposit deposit) {
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

