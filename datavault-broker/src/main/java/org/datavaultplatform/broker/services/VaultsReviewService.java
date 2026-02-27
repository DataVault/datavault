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
import org.springframework.util.Assert;

import java.time.*;
import java.util.*;

import static org.datavaultplatform.common.util.Utils.getSafeStream;

@Service
@Transactional
public class VaultsReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(VaultsReviewService.class);

    // The number of months before the review date at which people should be notified.
    // This could be moved into datavault.properties if they keep on changing their minds about the value.
    private static final int MONTHS_BEFORE_REVIEW_DATE = -6;

    private final VaultReviewDAO vaultReviewDAO;
    private final DepositsReviewService depositsReviewService;
    private final Clock clock;

    @Autowired
    public VaultsReviewService(VaultReviewDAO vaultReviewDAO, DepositsReviewService depositsReviewService, Clock clock) {
        Assert.notNull(vaultReviewDAO, "vaultReviewDAO cannot be null");
        Assert.notNull(depositsReviewService, "depositsReviewService cannot be null");
        Assert.notNull(clock, "clock cannot be null");
        this.vaultReviewDAO = vaultReviewDAO;
        this.depositsReviewService = depositsReviewService;
        this.clock = clock;
    }

    public VaultReview createVaultReview(Vault vault) {
        Assert.notNull(vault, "The vault cannot be null");
        VaultReview vaultReview = new VaultReview();

        vaultReview.setCreationTime(LocalDateTime.now(clock));
        vaultReview.setVault(vault);

        vaultReviewDAO.save(vaultReview);

        depositsReviewService.addDepositReviews(vault, vaultReview);
        return vaultReview;
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
        return getSafeStream(vaults)
                .filter(Objects::nonNull)
                .filter(this::isVaultForReview)
                .toList();
    }
//    /**
//     * Shared logic: Is the vault within its review window and not yet completed?
//     */
//    private boolean isEligibleForReviewAction(Vault vault) {
//        if (vault == null || vault.getReviewDate() == null) {
//            return false;
//        }
//
//        LocalDate today = LocalDate.now(clock);
//        LocalDate windowStart = DateTimeUtils.getDateAdjustedByMonths(vault.getReviewDate(), MONTHS_BEFORE_REVIEW_DATE);
//
//        // 1) Must be within or after the window
//        // 2) No review completed within this window
//        return !today.isBefore(windowStart) &&
//                reviewHasNotHappenedAfterReviewWindowStart(vault, windowStart);
//    }
    
    /*
     Returns true if the vault is due for review, or is currently being reviewed.     
     */
    /*
     * A Vault is due for a review if:
     * 1) The current date is within or after the Review Window (X months before review date).
     * 2) No review has already been completed (Actioned) within this window.
     */
    public boolean isVaultForReview(Vault vault) {
        if (vault == null || vault.getReviewDate() == null) {
            return false;
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate reviewWindowStartDate = DateTimeUtils.getDateAdjustedByMonths(vault.getReviewDate(), MONTHS_BEFORE_REVIEW_DATE);

        // might be due for review email on reviewWindowStartDate
        if (today.isBefore(reviewWindowStartDate)) {
            return false;
        }

        // Looks like it's due a review, but check if a review has happened.
        boolean vaultNeedsReview = reviewHasNotHappenedAfterReviewWindowStart(vault, reviewWindowStartDate);
        return vaultNeedsReview;
    }

    /*
     * A Vault is due for a review email if:
     * 1) The current date is within or after the Review Window (X months before review date).
     * 2) There is no review currently in progress (Underway).
     * 3) No review has already been completed (Actioned) within this window.
     * There is a chance the latest VaultReview was created a while ago and is still open - we won't send reminder emails.
     */
    public boolean dueForReviewEmail(Vault vault) {

        if (vault == null || vault.getReviewDate() == null) {
            return false;
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate reviewWindowStartDate = DateTimeUtils.getDateAdjustedByMonths(vault.getReviewDate(), MONTHS_BEFORE_REVIEW_DATE);

        // might be due for review email on reviewWindowStartDate
        if (today.isBefore(reviewWindowStartDate)) {
            return false;
        }
        
        // Looks like it's due an email, but check if a review is already underway or has happened.
        boolean vaultNeedsReviewEmail = !vault.isVaultReviewUnderway() && reviewHasNotHappenedAfterReviewWindowStart(vault, reviewWindowStartDate);
        return vaultNeedsReviewEmail;
    }
    
    private boolean reviewHasNotHappenedAfterReviewWindowStart(Vault vault, LocalDate reviewWindowStartDate){
        Assert.notNull(vault, "The vault cannot be null");
        return getSafeStream(vault.getVaultReviews())
                .filter(Objects::nonNull)
                .map(VaultReview::getActionedDate)
                .filter(Objects::nonNull)
                .map(DateTimeUtils::toLocalDate)
                .noneMatch(actionedLocalDate -> actionedLocalDate.isAfter(reviewWindowStartDate));
    }


    public List<VaultReview> findByVaultId(String vaultId) {
        return vaultReviewDAO.findByVaultId(vaultId);
    }

}
