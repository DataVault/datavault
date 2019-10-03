package org.datavaultplatform.common.model.dao;

import java.util.List;

import org.datavaultplatform.common.model.Vault;
 
public interface VaultDAO {

    public void save(Vault vault);
    
    public void update(Vault vault);
    
    public void saveOrUpdateVault(Vault vault);

    public List<Vault> list();

    public List<Vault> list(String userId, String sort, String order, String offset, String maxResult);

    public Vault findById(String Id);

    public List<Vault> search(String userId, String query, String sort, String order, String offset, String maxResult);

    public int count(String userId);

    public int getRetentionPolicyCount(int status);

	public Long getTotalNumberOfVaults(String userId);

	public Long getTotalNumberOfVaults(String userId, String query);
	
	public List<Object[]> getAllProjectsSize();
}