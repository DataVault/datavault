package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.VaultDAO;

import java.util.Date;
import java.util.List;

public class VaultsService {

    private VaultDAO vaultDAO;

    public List<Vault> getVaults() { return vaultDAO.list(); }

    public List<Vault> getVaults(String sort, String order) { return vaultDAO.list(sort, order); }

    public List<Vault> getVaultsForGroup(String groupID) { return vaultDAO.findByGroup(groupID); }

    public void addVault(Vault vault) {
        Date d = new Date();
        vault.setCreationTime(d);
        vaultDAO.save(vault);
    }
    
    public void updateVault(Vault vault) {
        vaultDAO.update(vault);
    }
    
    public Vault getVault(String vaultID) {
        return vaultDAO.findById(vaultID);
    }
    
    public void setVaultDAO(VaultDAO vaultDAO) {
        this.vaultDAO = vaultDAO;
    }

    public List<Vault> search(String query, String sort, String order) { return this.vaultDAO.search(query, sort, order); }

    public int count() { return vaultDAO.count(); }
}

