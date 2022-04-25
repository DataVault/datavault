package org.datavaultplatform.broker.scheduled;

import org.apache.commons.collections4.CollectionUtils;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.common.util.RoleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CheckForReview {

    private static final Logger log = LoggerFactory.getLogger(CheckForReview.class);

    private VaultsService vaultsService;
    private VaultsReviewService vaultsReviewService;
    private DepositsReviewService depositsReviewService;
    private LDAPService LDAPService;
    private EmailService emailService;
    private RolesAndPermissionsService rolesAndPermissionsService;
    private UsersService usersService;

    private String homeUrl;
    private String helpUrl;
    private String helpMail;

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void setVaultsReviewService(VaultsReviewService vaultsReviewService) {
        this.vaultsReviewService = vaultsReviewService;
    }

    public void setDepositsReviewService(DepositsReviewService depositsReviewService) {
        this.depositsReviewService = depositsReviewService;
    }

    public void setLDAPService(LDAPService LDAPService) {
        this.LDAPService = LDAPService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setRolesAndPermissionsService(RolesAndPermissionsService rolesAndPermissionsService) {
        this.rolesAndPermissionsService = rolesAndPermissionsService;
    }

    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }

    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }

    public void setHelpMail(String helpMail) {
        this.helpMail = helpMail;
    }


    @Scheduled(cron = "${review.schedule}")
    public void checkAll() throws Exception {

        Date start = new Date();
        log.info("Initiating check of Vaults for review at " + start);

        List<Vault> vaults = vaultsService.getVaults();

        for (Vault vault : vaults) {

            if (vaultsReviewService.dueForReviewEmail(vault)) {

                log.info("Vault " + vault.getName() + " is due for review");

                // Start the review process by creating the VaultReview and DepositReview objects. TBH I am not sure
                // this should be done here. The review process should possibly only be started by a user. The reason
                // it is done here is to prevent emails being sent over and over. Time will tell if this was a bad
                // decision.

                VaultReview vaultReview = vaultsReviewService.createVaultReview(vault);
                List <DepositReview> depositReviews = depositsReviewService.addDepositReviews(vault, vaultReview);

                // Now bash on with the emailing.

                HashMap<String, Object> model = new HashMap<String, Object>();
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
                emailService.sendTemplateMail(helpMail, "DataVault Vault needing reviewed", "review-due-support.vm", model);
                
                /*
                The following code is really old school and should be replaced with Streams and Lambdas,
                I just don't have time to do it, sorry!
                */

                List<RoleAssignment> roleAssignments = rolesAndPermissionsService.getRoleAssignmentsForVault(vault.getID());

                // Loop round once to see if we have a valid owner
                boolean ownerFound = false;
                for (RoleAssignment roleAssignment : roleAssignments) {
                    if (RoleUtils.isDataOwner(roleAssignment)) {
                        log.info("Email Data Owner " + roleAssignment.getUserId() + " as Review is due" );
                        User user = usersService.getUser(roleAssignment.getUserId());
                        if (!LDAPService.getLDAPAttributes(user.getID()).isEmpty()) {
                            // User still appears to be at the Uni
                            emailService.sendTemplateMail(user.getEmail(), "[Edinburgh DataVault] Your vault’s review date is approaching (action required) ", "review-due-owner.vm", model);
                            ownerFound = true;
                        }
                    }
                }

                // If we didn't find an owner then email any data managers
                if (!ownerFound) {
                    for (RoleAssignment roleAssignment : roleAssignments) {
                        if (roleAssignment.getRole().getName().equals("Nominated Data Manager")) {
                            log.info("Email Data Manager " + roleAssignment.getUserId() + " as Review is due");
                            User user = usersService.getUser(roleAssignment.getUserId());
                            if (!LDAPService.getLDAPAttributes(user.getID()).isEmpty()) {
                                // User still appears to be at the Uni
                                emailService.sendTemplateMail(user.getEmail(), "[Edinburgh DataVault] Your vault’s review date is approaching ", "review-due-data-manager.vm", model);
                            }
                        }
                    }
                }
            }
        }

        Date end = new Date();
        log.info("Finished check of Vaults for review at " + start);
        log.info("Check took " + TimeUnit.MILLISECONDS.toSeconds(end.getTime() - start.getTime()) + " seconds");
    }


    private String dateToString(Date date) {
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(date);
        } else {
            return "";
        }
    }

}
