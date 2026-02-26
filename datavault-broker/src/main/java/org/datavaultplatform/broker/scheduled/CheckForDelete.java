package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.AdminDepositService;
import org.datavaultplatform.broker.services.DepositsReviewService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * As part of the Review process, deposits can be flagged for deletion at a later date. Check the deposits
 * and delete any that are due for deletion.
 */

@Component
public class CheckForDelete implements ScheduledTask {

    private static final Logger LOG = LoggerFactory.getLogger(CheckForDelete.class);

    private final DepositsReviewService depositsReviewService;
    private final AdminDepositService adminDepositService;
    private final VaultsService vaultsService;
    private final Clock clock;

    @Autowired
    public CheckForDelete(VaultsService vaultsService,
                           DepositsReviewService depositsReviewService,
                           AdminDepositService adminDepositService, Clock clock) {
        this.vaultsService = vaultsService;
        this.depositsReviewService = depositsReviewService;
        this.adminDepositService = adminDepositService;
        this.clock = clock;
    }

    @Override
    @Scheduled(cron = ScheduledUtils.SCHEDULE_3_DELETE)
    @Transactional
    public void execute() throws Exception {

        long start = clock.millis();
        LocalDate today = LocalDate.now(clock);

        LOG.info("Initiating check of Vaults with deposits to delete");

        checkVaultsForDelete(today);

        long end = clock.millis();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(end - start);
        LOG.info("Finished check of Vaults with deposits to delete. Took [{}] seconds", seconds);
    }

    private void checkVaultsForDelete(LocalDate today) throws Exception {
        List<Vault> vaults = vaultsService.getVaults();
        for (Vault vault : vaults) {
            checkVaultForDelete(vault, today);
        }
    }

    private void checkVaultForDelete(Vault vault, LocalDate today) throws Exception {

        List<VaultReview> vaultReviews = vault.getVaultReviews();

        if (vaultReviews == null || vaultReviews.isEmpty()) {
            return;
        }

        LOG.info("Checking if Vault {}/{} has deposits to delete?", vault.getID(), vault.getName());

        // Get the most recent VaultReview - all VaultReviews are for the vault - DepositReviews are associated with Deposit.
        // Sort by creation time in descending order to get the most recent first.
        vaultReviews.sort(Comparator.comparing(VaultReview::getCreationTime).reversed());
        VaultReview mostRecentVaultReview = vaultReviews.get(0);

        LOG.info("Processing most recent VaultReview with id {} for Vault {}/{}",
                mostRecentVaultReview.getId(), vault.getID(), vault.getName());

        // We only process VaultReviews that have been actioned (completed).
        // If a VaultReview is not actioned, it means it's still in progress and its associated DepositReviews
        // should not be considered for deletion by this scheduled task yet.
        if (mostRecentVaultReview.getActionedDate() != null) {
            LOG.info("Vault {} has a completed review", vault.getName());

            // Iterate through DepositReviews associated with the most recent, completed VaultReview
            for (DepositReview dr : mostRecentVaultReview.getDepositReviews()) {
                if (dr == null) {
                    continue;
                }
                checkDepositReview(vault, mostRecentVaultReview, dr, today);
            }
        }
    }

    private void checkDepositReview(Vault vault, VaultReview vaultReview, DepositReview dr, LocalDate today) throws Exception {
        Assert.notNull(vault, "The vault cannot be null");
        Assert.notNull(vaultReview, "The vaultReview cannot be null");
        Assert.notNull(dr, "The depositReview cannot be null");
        Assert.notNull(today, "The Date 'today' cannot be null");

        if (dr.getActionedDate() != null) {
            return;
        }
        LOG.debug("Vault {} has an uncompleted depositReview for Deposit {}", vault.getName(), dr.getDeposit().getID());

        var deleteStatus = dr.getDeleteStatus();
        switch (deleteStatus) {
            case (DepositReviewDeleteStatus.ONREVIEW):
                //TODO - do I need to check getOldReviewDate is not null
                if (today.isAfter(vaultReview.getOldReviewDate())) {
                    LOG.info("Deleting Deposit [{}] because today is after Old Review Date", dr.getDeposit().getID());
                    depositReviewDeleteDeposit(dr);
                }
                break;

            case (DepositReviewDeleteStatus.ONEXPIRY):
                //TODO - do I need to check getRetentionPolicyExpiry is not null
                if (today.isAfter(DateTimeUtils.toLocalDate(vault.getRetentionPolicyExpiry()))) {
                    LOG.info("Deleting Deposit [{}] because today is after Retention Policy Expiry", dr.getDeposit().getID());
                    depositReviewDeleteDeposit(dr);
                }
                break;
            default:
                LOG.warn("unexpected DepositReview.deleteStatus {}", deleteStatus);
        }
    }

    private void depositReviewDeleteDeposit(DepositReview dr) throws Exception {
        Deposit deposit = dr.getDeposit();
        LOG.info("deleting deposit {}/{}", deposit.getID(), deposit.getName());
        adminDepositService.deleteDeposit(deposit, null);
        dr.setActionedDate(LocalDateTime.now(clock));
        depositsReviewService.updateDepositReview(dr);
    }
}
