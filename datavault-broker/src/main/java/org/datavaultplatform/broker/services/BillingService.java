package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.BillingInfo;
import org.datavaultplatform.common.model.dao.BillingDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class BillingService {

    private final BillingDAO billingDAO;

	@Autowired
	public BillingService(BillingDAO billingDAO) {
		this.billingDAO = billingDAO;
	}


	public void saveOrUpdateVault(BillingInfo billing) {
    	billingDAO.save(billing);
    }


	public BillingDAO getBillingDAO() {
		return billingDAO;
	}
}

