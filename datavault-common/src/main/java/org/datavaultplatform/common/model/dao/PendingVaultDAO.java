package org.datavaultplatform.common.model.dao;

import java.util.List;

import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.Vault;

public interface PendingVaultDAO {
    public void save(PendingVault pendingVault);

    public PendingVault findById(String Id);

    public void update(PendingVault vault);
    
    public List<PendingVault> list();

    public List<PendingVault> list(String userId, String sort, String order, String offset, String maxResult);

    public List<PendingVault> search(String userId, String query, String sort, String order, String offset, String maxResult);

    public int count(String userId);
    
    public int getTotalNumberOfPendingVaults(String userId);

	public int getTotalNumberOfPendingVaults(String userId, String query);

    public void deleteById(String Id);
}
