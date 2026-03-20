package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.RetentionPoliciesService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.datavaultplatform.common.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Created by stuartlewis on 01/06/2016.
 */
@Component
public class CheckRetentionPolicies implements ScheduledTask {

    private static final Logger LOG = LoggerFactory.getLogger(CheckRetentionPolicies.class);
    private final VaultsService vaultsService;
    private final Clock clock;

    public CheckRetentionPolicies(VaultsService vaultsService, Clock clock) {
        Assert.notNull(vaultsService, "VaultsService must not be null");
        Assert.notNull(clock, "clock must not be null");
        this.vaultsService = vaultsService;
        this.clock = clock;
    }

    @Override
    @Scheduled(cron = ScheduledUtils.SCHEDULE_5_RETENTION_CHECK)
    @Transactional
    public void execute() {
        // Start the check
        Instant start = clock.instant();
        LOG.info("Initiating check of retention policies.");

        checkRetentionPoliciesForVaults();
        
        // End the check
        Instant end = clock.instant();
        Duration time = Duration.between(start, end);
        LOG.info("Finished check of retention policies. Took [{}]seconds", time.toSeconds());
    }
    
    private void checkRetentionPoliciesForVaults(){
        // Get all the vaults
        Utils.getSafeStream(vaultsService.getVaults())
                .forEach(this::checkRetentionPoliciesForVault);
    }

    private void checkRetentionPoliciesForVault(Vault vault) {

        String vaultId = vault.getID();
        RetentionPolicy retentionPolicy = vault.getRetentionPolicy();
        String retentionPolicyDesc = retentionPolicy == null ? "null" : String.valueOf(retentionPolicy.getID());
        // Process each vault
        LOG.info("Checking retention policy of vault: {} ({}) with retention Vaj {}",
                vaultId,
                vault.getName(),
                retentionPolicyDesc
        );
        vaultsService.checkRetentionPolicy(vaultId, RetentionPoliciesService.RetentionPolicyUpdateReason.TASK_UPDATE);
        Vault checkedVault = vaultsService.getVault(vaultId);
        if (checkedVault == null) {
            return;
        }
        int status = checkedVault.getRetentionPolicyStatus();
        String statusDesc = RetentionPolicyStatus.getDescription(status);
        LOG.info("Status of vault {} is {}", vaultId, statusDesc);
    }
}
