package org.datavaultplatform.common.model.dao;

import java.util.List;

import org.datavaultplatform.common.model.BillingInfo;
 
public interface BillingDAO {

    public void save(BillingInfo vault);
    
    public void update(BillingInfo vault);

    public List<BillingInfo> list();

    public List<BillingInfo> list(String sort, String order, String offset, String maxResult);

    public BillingInfo findById(String Id);
    
    public void saveOrUpdateVault(BillingInfo billing);

    public List<BillingInfo> search(String query, String sort, String order, String offset, String maxResult);

    public int count();

    public Long getTotalNumberOfVaults();

	public Long getTotalNumberOfVaults(String query);
	
}