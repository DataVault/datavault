package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.VaultReviewDAO;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class VaultsReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(VaultsReviewService.class);

    // The number of months before the review date at which people should be notified.
    // This could be moved into datavault.properties if they keep on changing their minds about the value.
    private static final int MONTHS_BEFORE_REVIEW_DATE = -6;

    private final VaultReviewDAO vaultReviewDAO;
    private final Clock clock;

    @Autowired
    public VaultsReviewService(VaultReviewDAO vaultReviewDAO, Clock clock) {
        this.vaultReviewDAO = vaultReviewDAO;
        this.clock = clock;
    }

    public VaultReview createVaultReview(Vault vault) {
        VaultReview vaultReview = new VaultReview();

        vaultReview.setCreationTime(Date.from(clock.instant()));
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
        return vaultReviewDAO.findById(vaultReviewID).orElse(null);
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
            if (vault == null) {
                continue;
            }
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

        LocalDate today = LocalDate.now(clock);
        LocalDate preReviewDate = DateTimeUtils.getDateAdjustedByMonths(vault.getReviewDate(), MONTHS_BEFORE_REVIEW_DATE);

        if (preReviewDate == null || !today.isAfter(preReviewDate)) {
            return false;
        }

        boolean currentReviewExists = vault.getVaultReviews().stream().anyMatch(vr -> {
            LocalDate actionedDate = DateTimeUtils.toLocalDate(vr.getActionedDate());
            return actionedDate != null && actionedDate.isAfter(preReviewDate);
        });
        return !currentReviewExists;
    }


    /*
     Returns true only if the vault is due for review.
     */
    public boolean dueForReviewEmail(Vault vault) {

        LocalDate today = LocalDate.now(clock);

        LocalDate preReviewEmailNotificationDate = DateTimeUtils.getDateAdjustedByMonths(vault.getReviewDate(), MONTHS_BEFORE_REVIEW_DATE);

        // easier to test when using LocalDates instead of Dates
        if (preReviewEmailNotificationDate == null || !today.isAfter(preReviewEmailNotificationDate)) {
            return false;
        }

        // Looks like it's due an email, but check if a review is already underway or has happened.
        boolean currentReviewExists = vault.getVaultReviews().stream().anyMatch(vr -> {
            LocalDate actionedDate = DateTimeUtils.toLocalDate(vr.getActionedDate());
            return actionedDate == null || actionedDate.isAfter(preReviewEmailNotificationDate);
        });
        return !currentReviewExists;
    }

    public List<VaultReview> findByVaultId(String vaultId) {
        return vaultReviewDAO.findByVaultId(vaultId);
    }

}
