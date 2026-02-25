package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Created by stuartlewis on 01/06/2016.
 */
@Component
public class CheckRetentionPolicies implements ScheduledTask {

    private static final Logger LOG = LoggerFactory.getLogger(CheckRetentionPolicies.class);
    private final VaultsService vaultsService;
    private final Clock clock;

    public CheckRetentionPolicies(VaultsService vaultsService, Clock clock) {
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
        List<Vault> vaults = vaultsService.getVaults();
        for(Vault vault : vaults){
            if(vault == null){
                continue;
            }
            checkRetentionPoliciesForVault(vault);
        }
    }

    private void checkRetentionPoliciesForVault(Vault vault) {

        String vaultId = vault.getID();
        // Process each vault
        LOG.info("Checking retention policy of vault: {} ({}) with retention policy {}",
                vaultId,
                vault.getName(),
                vault.getRetentionPolicy().getID()
        );
        vaultsService.checkRetentionPolicy(vaultId);
        Vault checkedVault = vaultsService.getVault(vaultId);
        int status = checkedVault.getRetentionPolicyStatus();
        String statusDesc = RetentionPolicyStatus.getDescription(status);
        LOG.info("Status of vault {} is {}", vaultId, statusDesc);
    }
}
