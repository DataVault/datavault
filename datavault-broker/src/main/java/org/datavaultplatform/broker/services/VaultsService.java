package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.VaultDAO;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;

import java.util.Date;
import java.util.List;

public class VaultsService {

    private VaultDAO vaultDAO;

    public List<Vault> getVaults() { return vaultDAO.list(); }

    public List<Vault> getVaults(String sort, String order) { return vaultDAO.list(sort, order); }

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

    public int getPolicyCount(int status) { return vaultDAO.getPolicyCount(status); }

    public Vault checkPolicy (String vaultID) throws Exception {
        // Get the vault
        Vault vault = vaultDAO.findById(vaultID);

        // Get the right policy engine
        Class clazz = Class.forName(vault.getPolicy().getEngine());
        RetentionPolicy policy = (RetentionPolicy)clazz.newInstance();

        // Check the policy
        policy.run(vault);

        // Record we checked it
        vault.setPolicyLastChecked(new Date());

        // Update and return the policy
        vaultDAO.update(vault);
        return vault;
    }
}

