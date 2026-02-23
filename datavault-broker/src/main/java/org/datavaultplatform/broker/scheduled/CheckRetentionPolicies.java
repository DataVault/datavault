package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.Vault;
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
        LOG.info("Initiating check of retention policies at " + start);

        // Get all the vaults
        List<Vault> vaults = vaultsService.getVaults();
        vaults.forEach(this::processVault);

        // End the check
        Instant end = clock.instant();
        LOG.info("Finished check of retention policies at [{}]", end);
        Duration time = Duration.between(start, end);
        LOG.info("Check took [{}] seconds", time.getSeconds());
    }

    private void processVault(Vault vault) {

        String vaultId = vault.getID();
        // Process each vault
        LOG.info("Checking retention policy of vault: {} ({}) with policy {}",
                vaultId,
                vault.getName(),
                vault.getRetentionPolicy().getID()
        );
        vaultsService.checkRetentionPolicy(vaultId);
        Vault checkedVault = vaultsService.getVault(vaultId);
        int status = checkedVault.getRetentionPolicyStatus();
        // TODO does this mapping happen elsewhere.
        String statusDesc = switch (status) {
            case 0 -> "UNCHECKED";
            case 1 -> "OK";
            case 2 -> "REVIEW";
            case 3 -> "ERROR";
            default -> "UNKNOWN[" + status + "]";
        };
        LOG.info("Status of vault {} is {}", vaultId, statusDesc);
    }
}
