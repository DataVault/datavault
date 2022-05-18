package org.datavaultplatform.broker.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * As part of the Review process, deposits can be flagged for deletion at a later date. Check the deposits
 * and delete any that are due for deletion.
 */

@Component
public class CheckForDelete implements ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(CheckForDelete.class);

    private final VaultsService vaultsService;
    private final VaultsReviewService vaultsReviewService;
    private final DepositsReviewService depositsReviewService;
    private final ArchiveStoreService archiveStoreService;
    private final RolesAndPermissionsService rolesAndPermissionsService;
    private final UsersService usersService;
    private final JobsService jobsService;
    private final Sender sender;

    @Autowired
    public CheckForDelete(VaultsService vaultsService, VaultsReviewService vaultsReviewService,
        DepositsReviewService depositsReviewService, ArchiveStoreService archiveStoreService,
        RolesAndPermissionsService rolesAndPermissionsService, UsersService usersService,
        JobsService jobsService, Sender sender) {
        this.vaultsService = vaultsService;
        this.vaultsReviewService = vaultsReviewService;
        this.depositsReviewService = depositsReviewService;
        this.archiveStoreService = archiveStoreService;
        this.rolesAndPermissionsService = rolesAndPermissionsService;
        this.usersService = usersService;
        this.jobsService = jobsService;
        this.sender = sender;
    }

    @Override
    @Scheduled(cron = ScheduledUtils.SCHEDULE_3_DELETE)
    public void execute() throws Exception {

        Date today = new Date();
        log.info("Initiating check of Vaults with deposits to delete at " + today);

        List<Vault> vaults = vaultsService.getVaults();

        for (Vault vault : vaults) {

            List<VaultReview> vaultReviews = vault.getVaultReviews();

            if (!vaultReviews.isEmpty()) {

                log.info("Does Vault " + vault.getName() + " have deposits to delete?");

                // Get the most recent VaultReview
                vaultReviews.sort(new VaultReviewComparator());
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
        log.info("Delete deposit with name " + deposit.getName());

        List<Job> jobs = deposit.getJobs();
        for (Job job : jobs) {
            if (job.isError() == false && job.getState() != job.getStates().size() - 1) {
                // There's an in-progress job for this deposit
                throw new IllegalArgumentException("Job in-progress for this Deposit");
            }
        }

        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        if (archiveStores.size() == 0) {
            throw new Exception("No configured archive storage");
        }

        log.info("Delete deposit archiveStores : {}", archiveStores);
        archiveStores = archiveStoreService.addArchiveSpecificOptions(archiveStores);

        // Create a job to track this delete
        Job job = new Job("org.datavaultplatform.worker.tasks.Delete");
        jobsService.addJob(deposit, job);

        // Ask the worker to process the data delete

        HashMap<String, String> deleteProperties = new HashMap<>();
        deleteProperties.put("depositId", deposit.getID());
        deleteProperties.put("bagId", deposit.getBagId());
        deleteProperties.put("archiveSize", Long.toString(deposit.getArchiveSize()));
        // We have no record of who requested the delete, is that acceptable?
        deleteProperties.put("userId", null);
        deleteProperties.put("numOfChunks", Integer.toString(deposit.getNumOfChunks()));
        for (Archive archive : deposit.getArchives()) {
            deleteProperties.put(archive.getArchiveStore().getID(), archive.getArchiveId());
        }

        // Add a single entry for the user file storage
        Map<String, String> userFileStoreClasses = new HashMap<>();
        Map<String, Map<String, String>> userFileStoreProperties = new HashMap<>();
        //userFileStoreClasses.put(storageID, userStore.getStorageClass());
        //userFileStoreProperties.put(storageID, userStore.getProperties());

        Task deleteTask = new Task(
                job, deleteProperties, archiveStores,
                userFileStoreProperties, userFileStoreClasses,
                null, null,
                null,
                null, null,
                null, null, null);
        ObjectMapper mapper = new ObjectMapper();
        String jsonDelete = mapper.writeValueAsString(deleteTask);
        sender.send(jsonDelete);

    }
}
