package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import org.datavaultplatform.common.model.PendingVault;

public interface PendingVaultCustomDAO {
    public void save(PendingVault pendingVault);

    public PendingVault findById(String Id);

    public void update(PendingVault vault);
    
    public List<PendingVault> list();

    public List<PendingVault> list(String userId, String sort, String order, String offset, String maxResult);

    public List<PendingVault> search(String userId, String query, String sort, String order, String offset, String maxResult, String confirmed);

    public int count(String userId);
    
    public int getTotalNumberOfPendingVaults(String userId, String confirmed);

	public int getTotalNumberOfPendingVaults(String userId, String query, String confirmed);

    public void deleteById(String Id);
}
