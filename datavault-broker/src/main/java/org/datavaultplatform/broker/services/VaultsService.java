package org.datavaultplatform.broker.services;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.VaultDAO;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;

public class VaultsService {

    private VaultDAO vaultDAO;

    public List<Vault> getVaults() { return vaultDAO.list(); }

    public List<Vault> getVaults(String sort, String order, String offset, String maxResult) { 
    	return vaultDAO.list(sort, order, offset, maxResult); 
    }

    public void addVault(Vault vault) {
        Date d = new Date();
        vault.setCreationTime(d);
        vaultDAO.save(vault);
    }
    
    public void updateVault(Vault vault) {
        vaultDAO.update(vault);
    }
    
    public void saveOrUpdateVault(Vault vault) {
        vaultDAO.saveOrUpdateVault(vault);
    }
    
    public Vault getVault(String vaultID) {
        return vaultDAO.findById(vaultID);
    }
    
    public void setVaultDAO(VaultDAO vaultDAO) {
        this.vaultDAO = vaultDAO;
    }

    public List<Vault> search(String query, String sort, String order, String offset, String maxResult) { return this.vaultDAO.search(query, sort, order, offset, maxResult); }

    public int count() { return vaultDAO.count(); }

    public int getRetentionPolicyCount(int status) { return vaultDAO.getRetentionPolicyCount(status); }

    public Vault checkRetentionPolicy (String vaultID) throws Exception {
        // Get the vault
        Vault vault = vaultDAO.findById(vaultID);

        // Get the right policy engine
        Class clazz = Class.forName(vault.getRetentionPolicy().getEngine());
        RetentionPolicy policy = (RetentionPolicy)clazz.newInstance();

        // Check the policy
        policy.run(vault);

        // Set the expiry date
        vault.setRetentionPolicyExpiry(policy.getReviewDate(vault));

        // Record when we checked it
        vault.setRetentionPolicyLastChecked(new Date());

        // Update and return the policy
        vaultDAO.update(vault);
        return vault;
    }
    
    // Get the specified Vault object and validate it against the current User
    public Vault getUserVault(User user, String vaultID) throws Exception {
        Vault vault = getVault(vaultID);

        if (vault == null) {
            throw new Exception("Vault '" + vaultID + "' does not exist");
        }

        return vault;
    }
 	
	public Long getTotalNumberOfVaults() {
		return vaultDAO.getTotalNumberOfVaults();
	}
	/**
	 * Total number of records after applying search filter
	 * @param query
	 * @return
	 */
	public Long getTotalNumberOfVaults(String query) {
		return vaultDAO.getTotalNumberOfVaults(query);
	}
	
	public Map<String, Long> getAllProjectsSize() {
		Map<String, Long> projectSizeMap = new HashMap<>();
		List<Object[]> allProjectsSize = vaultDAO.getAllProjectsSize();
		if(allProjectsSize != null) {
			for(Object[] projectsizeArray :allProjectsSize) {
				projectSizeMap.put((String)projectsizeArray[0], (Long)projectsizeArray[1]);
			}
		}
		return projectSizeMap;
	}
}

