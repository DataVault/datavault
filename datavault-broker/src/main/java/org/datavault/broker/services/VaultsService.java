package org.datavault.broker.services;

import org.datavault.common.model.Vault;
import org.datavault.common.model.dao.VaultDAO;

import java.util.Date;
import java.util.List;

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

