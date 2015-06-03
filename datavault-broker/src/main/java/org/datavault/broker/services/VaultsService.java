package org.datavault.broker.services;

import org.datavault.common.model.Vault;
import org.datavault.common.model.dao.VaultDAO;
import org.datavault.common.model.Deposit;
import org.datavault.common.model.dao.DepositDAO;

import java.util.Date;
import java.util.List;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:39
 */
public class VaultsService {

    private VaultDAO vaultDAO;
    private DepositDAO depositDAO;
    
    public List<Vault> getVaults() {
        return vaultDAO.list();
    }
    
    public void addVault(Vault vault) {
        Date d = new Date();
        vault.setCreationTime(d);
        vaultDAO.save(vault);
    }
    
    public Vault getVault(String vaultID) {
        return vaultDAO.findById(vaultID);
    }
    
    public void setVaultDAO(VaultDAO vaultDAO) {
        this.vaultDAO = vaultDAO;
    }
}

