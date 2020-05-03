package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.VaultReviewDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VaultsReviewService {

    private static final Logger logger = LoggerFactory.getLogger(VaultsReviewService.class);

    private VaultReviewDAO vaultReviewDAO;

    public void setVaultReviewDAO(VaultReviewDAO vaultReviewDAO) {
        this.vaultReviewDAO = vaultReviewDAO;
    }

    public VaultReview createVaultReview(Vault vault) {
        VaultReview vaultReview = new VaultReview();

        vaultReview.setCreationTime(new Date());
        vaultReview.setVault(vault);

        vaultReviewDAO.save(vaultReview);

        return vaultReview;
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

    public List<Vault> getVaultsForReview(List<Vault> vaults) {
        List<Vault> vaultsForReview = new ArrayList<>();

        Date today = new Date();

        for (Vault vault : vaults) {

            Calendar c = Calendar.getInstance();
            c.setTime(vault.getReviewDate());
            c.add(Calendar.MONTH, -6);
            Date reviewDateMinus6 = c.getTime();

            if (today.after(reviewDateMinus6)) {
                // Now look to see if the vault has already been reviewed ie. does it have a current review record
                // already actioned.
                VaultReview vaultReview = null;

                // If we find a record that has been actioned after the review Date (minus6) then we know it
                // has already been reviewed, so don't list it.
                for (VaultReview vr : vault.getVaultReviews()) {

                    if ((vr.getActionedDate() != null) && ((vr.getActionedDate().after(reviewDateMinus6)))) {
                        vaultReview = vr;
                    }
                }

                if (vaultReview == null) {
                    vaultsForReview.add(vault);
                }
            }
        }

        return vaultsForReview;

    }
    







}

