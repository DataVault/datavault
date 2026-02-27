package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.datavaultplatform.common.util.RoleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Component
public class CheckForReview implements ScheduledTask {

    public static final String EMAIL_VAULT_NAME = "vault-name";
    public static final String EMAIL_VAULT_ID = "vault-id";
    public static final String EMAIL_VAULT_REVIEW_DATE = "vault-review-date";
    public static final String EMAIL_RETENTION_POLICY_EXPIRY_DATE = "retention-policy-expiry-date";
    public static final String EMAIL_GROUP_NAME = "group-name";
    public static final String EMAIL_HOME_PAGE = "home-page";
    public static final String EMAIL_HELP_PAGE = "help-page";
    public static final String EMAIL_HELP_MAIL = "help-mail";
    public static final String EMAIL_VAULT_REVIEW_LINK = "vault-review-link";

    private static final Logger LOG = LoggerFactory.getLogger(CheckForReview.class);

    private final VaultsService vaultsService;
    private final VaultsReviewService vaultsReviewService;
    private final LDAPService ldapService;
    private final EmailService emailService;
    private final RolesAndPermissionsService rolesAndPermissionsService;
    private final UsersService usersService;

    private final String homeUrl;
    private final String helpUrl;
    private final String helpMail;
    private final Clock clock;

    @Autowired
    public CheckForReview(VaultsService vaultsService, VaultsReviewService vaultsReviewService,
                           LDAPService ldapService, EmailService emailService,
                           RolesAndPermissionsService rolesAndPermissionsService, UsersService usersService,
                           @Value("${home.page}") String homeUrl,
                           @Value("${help.page}") String helpUrl,
                           @Value("${help.mail}") String helpMail,
                           Clock clock) {
        this.vaultsService = vaultsService;
        this.vaultsReviewService = vaultsReviewService;
        this.ldapService = ldapService;
        this.emailService = emailService;
        this.rolesAndPermissionsService = rolesAndPermissionsService;
        this.usersService = usersService;
        this.homeUrl = homeUrl;
        this.helpUrl = helpUrl;
        this.helpMail = helpMail;
        this.clock = clock;
    }

    @Override
    @Scheduled(cron = ScheduledUtils.SCHEDULE_4_REVIEW)
    @Transactional
    public void execute() throws Exception {

        long start = clock.millis();
        LOG.info("Initiating check of Vaults for review");

        checkVaultsForReview();

        long end = clock.millis();
        long durationSecs = TimeUnit.MILLISECONDS.toSeconds(end - start);
        LOG.info("Finished check of Vaults for review. Took [{}] seconds", durationSecs);
    }

    private void checkVaultsForReview() {
        List<Vault> vaults = vaultsService.getVaults();
        for (Vault vault : vaults) {
            if (vault == null) {
                continue;
            }
            checkVaultForReview(vault);
        }
    }

    private void checkVaultForReview(Vault vault) {
        if (!vaultsReviewService.dueForReviewEmail(vault)) {
            return;
        }

        LOG.info("Vault [{}}/{}] is due for review", vault.getID(), vault.getName());

        // Start the review process by creating the VaultReview and DepositReview objects. TBH I am not sure
        // this should be done here. The review process should possibly only be started by a user. The reason
        // it is done here is to prevent emails being sent over and over. Time will tell if this was a bad
        // decision.

        // IMPORTANT - do not remove
        Assert.isTrue(!vault.isVaultReviewUnderway(), "We are about to create a new vault review - just double checking one doesn't exist");
        vaultsReviewService.createVaultReview(vault);
        // IMPORTANT - do not remove.

        // Now bash on with the emailing.

        Map<String, Object> model = getEmailTemplateModel(vault);
        // Email the support team
        emailService.sendTemplateMail(helpMail, "DataVault Vault needing reviewed", EmailTemplate.REVIEW_DUE_SUPPORT, model);

        List<RoleAssignment> roleAssignments = rolesAndPermissionsService.getRoleAssignmentsForVault(vault.getID());

        boolean emailedOwners = emailMatchingRoleAssignments(roleAssignments, RoleUtils::isDataOwner, "Data Owner", model, EmailTemplate.REVIEW_DUE_OWNER);
        if (!emailedOwners) {
            emailMatchingRoleAssignments(roleAssignments, RoleUtils::isNominatedDataManager, "Data Manager", model, EmailTemplate.REVIEW_DUE_DATA_MANAGER);
        }
    }

    private boolean emailMatchingRoleAssignments(List<RoleAssignment> roleAssignments, Predicate<RoleAssignment> roleAssignmentTest, String roleLabel, Map<String, Object> model, String emailTemplate) {
        boolean emailed = false;
        for (RoleAssignment roleAssignment : roleAssignments) {
            if (roleAssignment == null) {
                continue;
            }
            if (roleAssignmentTest.test(roleAssignment)) {
                String userId = roleAssignment.getUserId();
                LOG.info("Email {} ({}) as Review is due", roleLabel, userId);
                if (isUserInLdap(userId)) {
                    User user = usersService.getUser(userId);
                    // User still appears to be at the Uni
                    emailService.sendTemplateMail(user.getEmail(), "[Edinburgh DataVault] Your vault’s review date is approaching ", emailTemplate, model);
                    emailed = true;
                }
            }
        }
        return emailed;
    }

    private boolean isUserInLdap(String userID) {
        boolean result = false;
        if (userID == null) {
            return false;
        }
        try {
            var attrs = ldapService.getLDAPAttributes(userID);
            result = !attrs.isEmpty();
        } catch (Exception ex) {
            LOG.warn("problem looking up LDAP attributes for [{}]", userID);

        }
        return result;
    }

    private Map<String, Object> getEmailTemplateModel(Vault vault) {
        Assert.notNull(vault, "The vault cannot be null");
        HashMap<String, Object> model = new HashMap<>();
        model.put(EMAIL_VAULT_NAME, vault.getName());
        model.put(EMAIL_VAULT_ID, vault.getID());
        model.put(EMAIL_VAULT_REVIEW_DATE, DateTimeUtils.formatDate(vault.getReviewDate()));
        model.put(EMAIL_RETENTION_POLICY_EXPIRY_DATE, DateTimeUtils.formatDate(vault.getRetentionPolicyExpiry()));
        String groupName = vault.getGroup() == null ? "" : vault.getGroup().getName();
        model.put(EMAIL_GROUP_NAME, groupName);
        model.put(EMAIL_HOME_PAGE, homeUrl);
        model.put(EMAIL_HELP_PAGE, helpUrl);
        model.put(EMAIL_HELP_MAIL, helpMail);
        String vaultReviewLinkURL = "%s/admin/vaults/%s/reviews".formatted(homeUrl, vault.getID());
        model.put(EMAIL_VAULT_REVIEW_LINK, vaultReviewLinkURL);
        return model;
    }
}
