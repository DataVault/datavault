package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by stuartlewis on 01/06/2016.
 */
@Component
public class CheckRetentionPolicies implements ScheduledTask {

    private final VaultsService vaultsService;

    private static final Logger log = LoggerFactory.getLogger(CheckRetentionPolicies.class);

    public CheckRetentionPolicies(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    @Scheduled(cron = ScheduledUtils.SCHEDULE_5_RETENTION_CHECK)
    public void execute() {
        // Start the check
        Date start = new Date();
        log.info("Initiating check of retention policies at " + start);

        // Get all the vaults
        List<Vault> vaults = vaultsService.getVaults();
        for (Vault v : vaults) {
            // Process each vault
            log.info("Checking retention policiy of vault: " + v.getID() + " (" + v.getName() + ") with policy " + v.getRetentionPolicy().getID());
            vaultsService.checkRetentionPolicy(v.getID());
            Vault checked = vaultsService.getVault(v.getID());
            int status = checked.getRetentionPolicyStatus();
            switch (status) {
                case 0:
                    log.info("Status of vault " + v.getID() + " is " + "UNCHECKED");
                    break;
                case 1:
                    log.info("Status of vault " + v.getID() + " is " + "OK");
                    break;
                case 2:
                    log.info("Status of vault " + v.getID() + " is " + "REVIEW");
                    break;
                case 3:
                    log.info("Status of vault " + v.getID() + " is " + "ERROR");
                    break;
            }
        }

        // End the check
        Date end = new Date();
        log.info("Finished check of retention policies at " + start);
        log.info("Check took " + TimeUnit.MILLISECONDS.toSeconds(end.getTime() - start.getTime()) + " seconds");
    }
}
