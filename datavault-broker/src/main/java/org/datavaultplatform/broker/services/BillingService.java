package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.BillingInfo;
import org.datavaultplatform.common.model.dao.BillingDAO;

public class BillingService {

    private BillingDAO billingDAO;

    
    public void saveOrUpdateVault(BillingInfo billing) {
    	billingDAO.saveOrUpdateVault(billing);
    }


	public BillingDAO getBillingDAO() {
		return billingDAO;
	}


	public void setBillingDAO(BillingDAO billingDAO) {
		this.billingDAO = billingDAO;
	}
    
    
}

