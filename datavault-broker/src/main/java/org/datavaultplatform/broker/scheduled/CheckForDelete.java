package org.datavaultplatform.broker.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.service.AdminDepositService;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * As part of the Review process, deposits can be flagged for deletion at a later date. Check the deposits
 * and delete any that are due for deletion.
 */

@Component
public class CheckForDelete implements ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(CheckForDelete.class);

    private final VaultsService vaultsService;

    private final DepositsReviewService depositsReviewService;
    private final AdminDepositService adminDepositService;

    @Autowired
    public CheckForDelete(VaultsService vaultsService, 
        DepositsReviewService depositsReviewService, 
        AdminDepositService adminDepositService) {
        this.vaultsService = vaultsService;
        this.depositsReviewService = depositsReviewService;
        this.adminDepositService = adminDepositService;
    }

    @Override
    @Scheduled(cron = ScheduledUtils.SCHEDULE_3_DELETE)
    @Transactional
    public void execute() throws Exception {

        Date today = new Date();
        log.info("Initiating check of Vaults with deposits to delete at " + today);

        List<Vault> vaults = vaultsService.getVaults();

        for (Vault vault : vaults) {

            List<VaultReview> vaultReviews = vault.getVaultReviews();

            if (!vaultReviews.isEmpty()) {

                log.info("Does Vault " + vault.getName() + " have deposits to delete?");

                // Get the most recent VaultReview
                vaultReviews.sort(Comparator.comparing(VaultReview::getCreationTime));
                VaultReview vaultReview = vaultReviews.get(0);

                log.info("Does VaultReview with id " + vaultReview.getId() + " have deposits to delete?") ;

                if (vaultReview.getActionedDate() != null ) {
                    log.info("Vault " + vault.getName() + " has a completed review");

                    for (DepositReview dr : vaultReview.getDepositReviews()) {
                        if (dr.getActionedDate() == null) {
                            log.info("Vault " + vault.getName() + " has an uncompleted depositReview");

                            switch (dr.getDeleteStatus()) {
                                case (DepositReviewDeleteStatus.ONREVIEW):
                                    if (today.after(vaultReview.getOldReviewDate())) {
                                        log.info("deleting deposit " + dr.getDeposit().getID());
                                        deleteDeposit(dr.getDeposit());
                                        dr.setActionedDate(today);
                                        depositsReviewService.updateDepositReview(dr);
                                    }
                                    break;

                                case (DepositReviewDeleteStatus.ONEXPIRY):
                                    if (today.after(vault.getRetentionPolicyExpiry())) {
                                        log.info("deleting deposit " + dr.getDeposit().getID());
                                        deleteDeposit(dr.getDeposit());
                                        dr.setActionedDate(today);
                                        depositsReviewService.updateDepositReview(dr);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        }

        Date end = new Date();
        log.info("Finished check of Vaults with deposits to delete at " + today);
        log.info("Check took " + TimeUnit.MILLISECONDS.toSeconds(end.getTime() - today.getTime()) + " seconds");
    }

    // todo : move this method to a service class
    private void deleteDeposit(Deposit deposit) throws Exception {
        adminDepositService.deleteDeposit(deposit, null);
    }
}
