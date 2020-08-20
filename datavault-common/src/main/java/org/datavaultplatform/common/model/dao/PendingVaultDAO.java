package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.Vault;

public interface PendingVaultDAO {
    public void save(PendingVault pendingVault);

    public PendingVault findById(String Id);
}
