package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.VaultReviewDAO;
import org.datavaultplatform.common.response.VaultReviewStatusInfo;
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
    public static final int MONTHS_BEFORE_REVIEW_DATE = -6;

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

    public VaultReview updateVaultReview(VaultReview vaultReview) {
        Assert.notNull(vaultReview, "The vaultReview cannot be null");
        Assert.notNull(vaultReview.getId(), "The vaultReview.id cannot be null");
        VaultReview originalVaultReview = vaultReviewDAO.findById(vaultReview.getId()).orElseThrow();

        // by doing this - we don't lose the original vaultId on the VaultReview
        originalVaultReview.setActionedDate(vaultReview.getActionedDate());
        originalVaultReview.setComment(vaultReview.getComment());
        originalVaultReview.setOldReviewDate(vaultReview.getOldReviewDate());

        return vaultReviewDAO.update(originalVaultReview);
    }

    public List<Vault> getVaultsForReview(List<Vault> vaults) {
        return getSafeStream(vaults)
                .filter(this::isVaultForReview)
                .toList();
    }

    /**
     * Determines if a Vault is currently eligible for a review action based on its review schedule.
     * <p>
     * Eligibility is defined by two criteria:
     * <ul>
     * <li><b>Temporal:</b> The current date must be within or after the "Review Window." 
     * The window starts {@code MONTHS_BEFORE_REVIEW_DATE} months before the {@code vault.reviewDate}.</li>
     * <li><b>Status:</b> No other review record must have been actioned/completed 
     * from the start of this current review window.</li>
     * </ul>
     * <p>
     * The window is treated as a "half-open" interval: {@code [windowStart, infinity)}.
     *
     * @param vault the Vault entity to check; if null or missing a review date, returns {@code false}.
     * @return {@code true} if the vault is due for review and hasn't been actioned yet in this VaultReviewTimeWindow, {@code false} otherwise.
     */
    private boolean isEligibleForReviewAction(Vault vault) {
        if (vault == null || vault.getReviewDate() == null) {
            return false;
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate reviewWindowStartDate = DateTimeUtils.getLocalDateAdjustedByMonths(vault.getReviewDate(), MONTHS_BEFORE_REVIEW_DATE);

        // Rule 1: Today is not before the Window Start Date
        // Rule 2: No review has happened since the window opened
        return !today.isBefore(reviewWindowStartDate) &&
                reviewHasNotHappenedAfterReviewWindowStart(vault, reviewWindowStartDate);
    }

    /*
     * A Vault is due for a review if:
     * 1) The current date is within or after the Review Window (X months before review date).
     * 2) No review has already been completed (Actioned) within this window.
     */
    public boolean isVaultForReview(Vault vault) {
        return isEligibleForReviewAction(vault);
    }

    /*
     * A Vault is due for a review email if:
     * 1) There is no review currently in progress (Underway).
     * 2) The current date is within or after the Review Window (X months before review date).
     * 3) No review has already been completed (Actioned) within this window.
     * There is a chance the latest VaultReview was created a while ago and is still open - we won't send reminder emails.
     */
    public boolean isDueForReviewEmail(Vault vault) {
        return vault != null && !vault.isVaultReviewUnderway()
                 && isEligibleForReviewAction(vault);
    }
    
    private boolean reviewHasNotHappenedAfterReviewWindowStart(Vault vault, LocalDate reviewWindowStartDate){
        Assert.notNull(vault, "The vault cannot be null");
        return getSafeStream(vault.getVaultReviews())
                .map(VaultReview::getActionedDate)
                .filter(Objects::nonNull)
                .map(DateTimeUtils::toLocalDate)
                .noneMatch(actionedLocalDate -> actionedLocalDate.isAfter(reviewWindowStartDate));
    }


    public List<VaultReview> findByVaultId(String vaultId) {
        return vaultReviewDAO.findByVaultId(vaultId);
    }

    public VaultReviewStatusInfo getCurrentVaultReviewStatus(String vaultId) {
        Assert.notNull(vaultId, "The vaultId cannot be null");

        VaultReviewStatusInfo statusInfo = new VaultReviewStatusInfo();
        VaultReview latestSubmitted = vaultReviewDAO.findLatestSubmittedVaultReview(vaultId).orElse(null);
        VaultReview underwayReview = vaultReviewDAO.findUnderwayVaultReview(vaultId).orElse(null);

        statusInfo.setUnderwayReview(underwayReview);
        statusInfo.setLatestSubmittedReview(latestSubmitted);
        return statusInfo;
    }

    public Optional<RefreshedVaultReview> refreshDepositsOnUnderwayVaultReview(String vaultId) {
        Assert.notNull(vaultId, "The vaultId cannot be null");
        return vaultReviewDAO.findUnderwayVaultReview(vaultId).map( underwayReview -> {
            Vault vault = underwayReview.getVault();
            Assert.notNull(vault, "The vault cannot be null");
            List<DepositReview> depositReviewsAdded = depositsReviewService.refreshDepositReviews(vault, underwayReview);
            return new RefreshedVaultReview(underwayReview, depositReviewsAdded);
        });
    }
}
