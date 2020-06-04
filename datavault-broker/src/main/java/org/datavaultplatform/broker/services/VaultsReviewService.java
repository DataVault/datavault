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

        for (Vault vault : vaults) {
            if (isVaultForReview(vault)) {
                vaultsForReview.add(vault);
            }
        }

        return vaultsForReview;
    }

    /*
     Returns true if the vault is due for review, or is currently being reviewed.
     */
    public boolean isVaultForReview(Vault vault) {

        Date today = new Date();

        Calendar c = Calendar.getInstance();
        c.setTime(vault.getReviewDate());
        c.add(Calendar.MONTH, -3);
        Date reviewDateMinus3 = c.getTime();

        if (today.after(reviewDateMinus3)) {

            // Looks like its due for review, but has the review already happened.

            boolean currentReviewExists = false;

            for (VaultReview vr : vault.getVaultReviews()) {

                if ((vr.getActionedDate() != null) && ((vr.getActionedDate().after(reviewDateMinus3)))) {

                    // A review has already happened since the review date
                    currentReviewExists = true;
                }
            }

            if (!currentReviewExists) {
                return true;
            }
        }

        return false;
    }


    /*
     Returns true only if the vault is due for review.
     */
    public boolean dueForReviewEmail(Vault vault) {

        Date today = new Date();

        Calendar c = Calendar.getInstance();
        c.setTime(vault.getReviewDate());
        c.add(Calendar.MONTH, -3);
        Date reviewDateMinus3 = c.getTime();

        if (today.after(reviewDateMinus3)) {

            // Looks like its due an email, but check if a review is already underway or has happened.

            boolean currentReviewExists = false;
            for (VaultReview vr : vault.getVaultReviews()) {

                if ((vr.getActionedDate() == null)) {
                    // A review is underway
                    currentReviewExists = true;
                } else {
                    if (vr.getActionedDate().after(reviewDateMinus3)) {
                        // A review has already happened since the review date
                        currentReviewExists = true;
                    }
                }
            }

            if (!currentReviewExists) {
                return true;
            }
        }

        return false;
    }

}

