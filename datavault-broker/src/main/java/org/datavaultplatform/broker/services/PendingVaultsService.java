package org.datavaultplatform.broker.services;

import java.util.List;

import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.PendingVaultDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class PendingVaultsService {
	private final Logger logger = LoggerFactory.getLogger(PendingVaultsService.class);
	
    private PendingVaultDAO pendingVaultDAO;
    private VaultsService vaultsService;
    

    public void setPendingVaultDAO(PendingVaultDAO pendingVaultDAO) {
        this.pendingVaultDAO = pendingVaultDAO;
    }
    
    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void addPendingVault(PendingVault vault) {
        Date d = new Date();
        vault.setCreationTime(d);
        if (vault != null) {
            if (vault.getId() == null || vault.getId().isEmpty()) {
                logger.info("Saving a new pending vault");
                pendingVaultDAO.save(vault);
            } else {
                logger.info("Updating an existing pending vault (" + vault.getId() + ")");
                pendingVaultDAO.update(vault);
            }
        }

    }

    public PendingVault getPendingVault(String vaultID) {
        return pendingVaultDAO.findById(vaultID);
    }
    
    public int getTotalNumberOfPendingVaults(String userId) {
        return pendingVaultDAO.getTotalNumberOfPendingVaults(userId);
    }

    /**
     * Total number of records after applying search filter
     *
     * @param query
     * @return
     */
    public int getTotalNumberOfPendingVaults(String userId, String query) {
        return pendingVaultDAO.getTotalNumberOfPendingVaults(userId, query);
    }

    
    public List<PendingVault> search(String userId, String query, String sort, String order, String offset, String maxResult) {
        return this.pendingVaultDAO.search(userId, query, sort, order, offset, maxResult);
    }

    public int count(String userId) {
        return pendingVaultDAO.count(userId);
    }

    // Get the specified Vault object and validate it against the current User
    // DAS copied this from the Vaults service it doesn't appear to do any validation
    // dunno if Digirati handed it in like that or we've changed it yet
    public PendingVault getUserPendingVault(User user, String vaultID) throws Exception {
        PendingVault vault = getPendingVault(vaultID);

        if (vault == null) {
            throw new Exception("Vault '" + vaultID + "' does not exist");
        }

        return vault;
    }
    
    public List<PendingVault> getPendingVaults() {
        return pendingVaultDAO.list();
    }
    
    public int getTotalNumberOfPendingVaults() {
    	return pendingVaultDAO.list() != null ? pendingVaultDAO.list().size() : 0;
    }


    public List<PendingVault> getPendingVaults(String userId, String sort, String order, String offset, String maxResult) {
        return pendingVaultDAO.list(userId, sort, order, offset, maxResult);
    }

}
