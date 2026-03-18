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
import java.util.List;
import java.util.Optional;
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

        if (vaults == null) {
            return;
        }
        for (Vault vault : vaults) {
            if (vault != null) {
                checkVaultForDelete(vault, today);
            }
        }
    }

    protected void checkVaultForDelete(Vault vault, LocalDate today) throws Exception {
        Assert.notNull(vault, "The vault cannot be null");

        List<VaultReview> vaultReviews = vault.getVaultReviews();

        if (vaultReviews == null || vaultReviews.isEmpty()) {
            return;
        }

        LOG.info("Checking if Vault {}/{} has deposits to delete?", vault.getID(), vault.getName());

        // Get the most recent VaultReview - all VaultReviews are for the vault - DepositReviews are associated with Deposit.
        Optional<VaultReview> optMostRecentVaultReview = vault.getMostRecentVaultReview();
        if (optMostRecentVaultReview.isEmpty()) {
            return;
        }
        VaultReview mostRecentVaultReview = optMostRecentVaultReview.get();

        LOG.info("Processing most recent VaultReview with id {} for Vault {}/{}",
                mostRecentVaultReview.getId(), vault.getID(), vault.getName());

        // We only process VaultReviews that have been actioned (completed).
        // If a VaultReview is not actioned, it means it's still in progress and its associated DepositReviews
        // should not be considered for deletion by this scheduled task yet.
        if (mostRecentVaultReview.getActionedDate() != null) {
            LOG.info("Vault {} has a completed review", vault.getName());

            List<DepositReview> depositReviews = mostRecentVaultReview.getDepositReviews();
            if (depositReviews == null) {
                return;
            }
            // Iterate through DepositReviews associated with the most recent, completed VaultReview
            for (DepositReview dr : depositReviews) {
                if (dr == null) {
                    continue;
                }
                checkActionedDepositReview(vault, mostRecentVaultReview, dr, today);
            }
        }
    }

    protected void checkActionedDepositReview(Vault vault, VaultReview vaultReview, DepositReview dr, LocalDate today) throws Exception {
        Assert.notNull(vault, "The vault cannot be null");
        Assert.notNull(vaultReview, "The vaultReview cannot be null");
        Assert.notNull(dr, "The depositReview cannot be null");
        Assert.notNull(dr.getDeposit(), "The depositReview.deposit cannot be null");
        Assert.notNull(today, "The Date 'today' cannot be null");

        // we are only interested in DepositReviews that have not been actioned
        // when we save a depositReview with RETAIN - we set the actionedDate.
        // when we save a depositReivew with NOW - we set the actionedDate (and delete the deposit) 
        if (dr.getActionedDate() != null) {
            return;
        }
        String depositId = dr.getDeposit().getID();
        LOG.debug("Vault {} has an uncompleted depositReview for Deposit {}", vault.getName(), depositId);

        var deleteStatus = dr.getDeleteStatus();
        switch (deleteStatus) {
            case (DepositReviewDeleteStatus.ONREVIEW):
                LocalDate oldReviewDate = vaultReview.getOldReviewDate();
                if (oldReviewDate != null && today.isAfter(oldReviewDate)) {
                    LOG.info("Deleting Deposit [{}] because today is after OLD Review Date [{}]" , depositId, oldReviewDate);
                    depositReviewDeleteDeposit(dr);
                }
                break;

            case (DepositReviewDeleteStatus.ONEXPIRY):
                LocalDate retentionPolicyExpiryDate = DateTimeUtils.toLocalDate(vault.getRetentionPolicyExpiry());
                if (retentionPolicyExpiryDate != null && today.isAfter(retentionPolicyExpiryDate)) {
                    LOG.info("Deleting Deposit [{}] because today is after Retention Policy Expiry [{}]", depositId, retentionPolicyExpiryDate);
                    depositReviewDeleteDeposit(dr);
                }
                break;
            default:
                LOG.warn("unexpected DepositReview.deleteStatus [{}] for Deposit [{}]", deleteStatus, depositId);
        }
    }

    private void depositReviewDeleteDeposit(DepositReview dr) throws Exception {
        Assert.notNull(dr, "The depositReview cannot be null");
        Assert.notNull(dr.getDeposit(), "The depositReview.deposit cannot be null");

        Deposit deposit = dr.getDeposit();
        LOG.info("deleting deposit {}/{}", deposit.getID(), deposit.getName());
        adminDepositService.deleteDeposit(deposit, null);
        dr.setActionedDate(LocalDateTime.now(clock));
        depositsReviewService.updateDepositReview(dr);
    }
}
