package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.dao.PendingVaultDAO;

public class PendingVaultsService {
    private PendingVaultDAO pendingVaultDAO;

    public void setPendingVaultDAO(PendingVaultDAO pendingVaultDAO) {
        this.pendingVaultDAO = pendingVaultDAO;
    }

    public void addPendingVault(PendingVault vault) {
        pendingVaultDAO.save(vault);
    }
}
