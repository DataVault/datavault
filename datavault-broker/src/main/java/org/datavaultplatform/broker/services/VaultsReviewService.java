package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.VaultReviewDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VaultsReviewService {

    private static final Logger logger = LoggerFactory.getLogger(VaultsReviewService.class);

    private VaultReviewDAO vaultReviewDAO;

    public void setVaultReviewDAO(VaultReviewDAO vaultReviewDAO) {
        this.vaultReviewDAO = vaultReviewDAO;
    }


    public void addVaultReview(VaultReview vaultReview) {
        vaultReviewDAO.save(vaultReview);
    }

    public List<VaultReview> getVaultReviews() {
        return vaultReviewDAO.list();
    }

    public VaultReview getVaultReview(String vaultReviewID) {
        return vaultReviewDAO.findById(vaultReviewID);
    }

    public List<VaultReview> search(String query) {
        return this.vaultReviewDAO.search(query);
    }
    
    public void updateVaultReview(VaultReview vaultReview) {
        vaultReviewDAO.update(vaultReview);
    }
    


    public int count() { return vaultReviewDAO.count(); }




}

