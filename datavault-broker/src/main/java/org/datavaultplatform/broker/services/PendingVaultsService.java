package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.PendingVaultDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class PendingVaultsService {
    private PendingVaultDAO pendingVaultDAO;
    private final Logger logger = LoggerFactory.getLogger(PendingVaultsService.class);

    public void setPendingVaultDAO(PendingVaultDAO pendingVaultDAO) {
        this.pendingVaultDAO = pendingVaultDAO;
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

    public PendingVault getVault(String vaultID) {
        return pendingVaultDAO.findById(vaultID);
    }

    // Get the specified Vault object and validate it against the current User
    // DAS copied this from the Vaults service it doesn't appear to do any validation
    // dunno if Digirati handed it in like that or we've changed it yet
    public PendingVault getUserVault(User user, String vaultID) throws Exception {
        PendingVault vault = getVault(vaultID);

        if (vault == null) {
            throw new Exception("Vault '" + vaultID + "' does not exist");
        }

        return vault;
    }
}
