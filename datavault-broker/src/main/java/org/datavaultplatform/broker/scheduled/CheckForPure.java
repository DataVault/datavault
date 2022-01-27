package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.EmailService;
import org.datavaultplatform.broker.services.VaultsPureService;
import org.datavaultplatform.broker.services.VaultsReviewService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.util.RoleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CheckForPure {
    private static final Logger log = LoggerFactory.getLogger(CheckForPure.class);

    private VaultsService vaultsService;
    private VaultsPureService vaultsPureService;
    private EmailService emailService;

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void setVaultsPureService(VaultsPureService vaultsPureService) {
        this.vaultsPureService = vaultsPureService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }



    @Scheduled(cron = "${pure.schedule}")
    /*
    To start with loop through the vaults and produce / send xml to Pure group
    for Vaults that haven't already done that.
    Once that is done think about extending so that ones that have produced xml check if it has been loaded into Pure
     */
    public void checkAll() throws Exception {

        Date start = new Date();
        log.info("Initiating check of Vaults that need to produce a Pure record at " + start);

        List<Vault> vaults = vaultsService.getVaults();

        for (Vault vault : vaults) {

            // have we already produced the Record or not
            if (! vaultsPureService.hasProducedPureRecord(vault)) {

                log.info("Vault " + vault.getName() + " has not produced a Pure Record");

                // Build the Pure XML record for the Vault

                // deliver to the Pure mob

                // Now bash on with the emailing (if required actually I suppose we could email straight to Damon / Nik)

                /*HashMap<String, Object> model = new HashMap<String, Object>();
                model.put("vault-name", vault.getName());
                model.put("vault-id", vault.getID());
                model.put("vault-review-date", dateToString(vault.getReviewDate()));
                model.put("retention-policy-expiry-date", dateToString(vault.getRetentionPolicyExpiry()));
                model.put("group-name", vault.getGroup().getName());
                model.put("home-page", homeUrl);
                model.put("help-page", helpUrl);
                model.put("help-mail", helpMail);
                model.put("vault-review-link", homeUrl+ "/admin/vaults/" + vault.getID() + "/reviews");

                // Email the support team
                emailService.sendTemplateMail(helpMail, "DataVault Vault needing reviewed", "review-due-support.vm", model);*/
            }


        }

        Date end = new Date();
        log.info("Finished check of Vaults for review at " + start);
        log.info("Check took " + TimeUnit.MILLISECONDS.toSeconds(end.getTime() - start.getTime()) + " seconds");
    }
}
