package org.datavault.broker.services;

import java.util.Date;
import org.datavault.common.model.Vault;
import org.datavault.common.model.dao.VaultDAO;

import java.util.List;

import org.datavault.common.model.Deposit;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:39
 */
public class VaultsService {

    private VaultDAO vaultDAO;
    
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

