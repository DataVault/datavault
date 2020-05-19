package org.datavaultplatform.broker.scheduled;

import org.apache.commons.collections.CollectionUtils;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.common.util.RoleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CheckForReview {

    private static final Logger log = LoggerFactory.getLogger(CheckForReview.class);

    private VaultsService vaultsService;
    private VaultsReviewService vaultsReviewService;
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

    public void setLDAPService(org.datavaultplatform.common.services.LDAPService LDAPService) {
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

                HashMap<String, Object> model = new HashMap<String, Object>();
                model.put("vault-name", vault.getName());
                model.put("vault-id", vault.getID());
                model.put("vault-review-date", DateToString(vault.getReviewDate()));
                model.put("group-name", vault.getGroup().getName());
                model.put("home-page", homeUrl);
                model.put("help-page", helpUrl);
                model.put("help-mail", helpMail);

                // Email the support team
                emailService.sendTemplateMail(helpMail, "DataVault Vault needing reviewed", "user-review-due.vm", model);

                List<RoleAssignment> roleAssignments = rolesAndPermissionsService.getRoleAssignmentsForVault(vault.getID());

                for (RoleAssignment roleAssignment : roleAssignments) {
                    if (RoleUtils.isDataOwner(roleAssignment)) {

                        log.info("Email Data Owner " + roleAssignment.getUserId() + " as Review is due" );
                        User user = usersService.getUser(roleAssignment.getUserId());
                        if (!LDAPService.getLDAPAttributes(user.getID()).isEmpty()) {
                            // User still appears to be at the Uni
                            emailService.sendTemplateMail(user.getEmail(), "DataVault Vault needing reviewed", "user-review-due.vm", model);
                        }
                    } else {
                        if (roleAssignment.getRole().getName().equals("Data Manager")) {
                            log.info("Email Data Manager " + roleAssignment.getUserId() + " as Review is due" );
                            User user = usersService.getUser(roleAssignment.getUserId());
                            if (!LDAPService.getLDAPAttributes(user.getID()).isEmpty()) {
                                // User still appears to be at the Uni
                                emailService.sendTemplateMail(user.getEmail(), "DataVault Vault needing reviewed", "user-review-due.vm", model);
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


    private String DateToString(Date date) {
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(date);
        } else {
            return "";
        }
    }

}
