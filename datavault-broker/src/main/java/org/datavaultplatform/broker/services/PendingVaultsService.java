package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.PendingVault.Feewaiver_Query_Choice;
import org.datavaultplatform.common.model.PendingVault.Funding_Query_Choice;
import org.datavaultplatform.common.model.PendingVault.Slice_Query_Choice;
import org.datavaultplatform.common.model.dao.PendingVaultDAO;
import org.datavaultplatform.common.request.CreateVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PendingVaultsService {

    public static final String EMAIL_VAULT_NAME = "vault-name";
    public static final String EMAIL_GROUP_NAME = "group-name";
    public static final String EMAIL_SUBMITTER_ID = "submitter-id";
    public static final String EMAIL_TIMESTAMP = "timestamp";

    private final Logger logger = LoggerFactory.getLogger(PendingVaultsService.class);

    private final PendingVaultDAO pendingVaultDAO;
    private final GroupsService groupsService;
    private final RetentionPoliciesService retentionPoliciesService;
    private final UsersService usersService;
    private final RolesAndPermissionsService permissionsService;
    private final PendingDataCreatorsService pendingDataCreatorsService;

    private final VaultsService vaultsService;
    private final EmailService emailService;
    private final String helpMail;

    @Autowired
    public PendingVaultsService(PendingVaultDAO pendingVaultDAO, GroupsService groupsService,
            RetentionPoliciesService retentionPoliciesService, UsersService usersService,
            RolesAndPermissionsService permissionsService,
            PendingDataCreatorsService pendingDataCreatorsService, VaultsService vaultsService,
            EmailService emailService, @Value("${help.mail}") String helpMail) {
        this.pendingVaultDAO = pendingVaultDAO;
        this.groupsService = groupsService;
        this.retentionPoliciesService = retentionPoliciesService;
        this.usersService = usersService;
        this.permissionsService = permissionsService;
        this.pendingDataCreatorsService = pendingDataCreatorsService;
        this.vaultsService = vaultsService;
        this.emailService = emailService;
        this.helpMail = helpMail;
    }

    public void addOrUpdatePendingVault(PendingVault vault) {
        Date d = new Date();
        vault.setCreationTime(d);
        if (vault != null) {
            if (vault.getId() == null || vault.getId().isEmpty()) {
                logger.info("Saving a new pending vault");
                pendingVaultDAO.save(vault);
            } else {
                logger.info("Updating an existing pending vault (" + vault.getId() + ")");
                pendingVaultDAO.update(vault);
            }
        }

    }

    public void delete(String id) {
        logger.info("Called delete for '" + id + "'");
        // delete role_assigmnets for id
        // for each role
        List<RoleAssignment> roles = permissionsService.getRoleAssignmentsForPendingVault(id);
        for (RoleAssignment role : roles) {
            permissionsService.deleteRoleAssignment(role.getId());
        }
        // delete pending data creators
        // deleted by cascade in PendingVaults hibernate config
        // for each creator
        // pendingDataCreatorsService.deletePendingDataCreator(creatorID);
        // delete pending vault
        pendingVaultDAO.deleteById(id);
    }

    public PendingVault getPendingVault(String vaultID) {
        return pendingVaultDAO.findById(vaultID).orElse(null);
    }

    public int getTotalNumberOfPendingVaults(String userId, String confirmed) {
        return pendingVaultDAO.getTotalNumberOfPendingVaults(userId, confirmed);
    }

    /**
     * Total number of records after applying search filter
     *
     * @param query
     * @return
     */
    public int getTotalNumberOfPendingVaults(String userId, String query, String confirmed) {
        return pendingVaultDAO.getTotalNumberOfPendingVaults(userId, query, confirmed);
    }

    public List<PendingVault> search(String userId, String query, String sort, String order, String offset,
            String maxResult, String confirmed) {
        return this.pendingVaultDAO.search(userId, query, sort, order, offset, maxResult, confirmed);
    }

    public int count(String userId) {
        return pendingVaultDAO.count(userId);
    }

    // Get the specified Vault object and validate it against the current User
    // DAS copied this from the Vaults service it doesn't appear to do any
    // validation
    // dunno if Digirati handed it in like that or we've changed it yet
    public PendingVault getUserPendingVault(User user, String vaultID) throws Exception {
        PendingVault vault = getPendingVault(vaultID);

        if (vault == null) {
            throw new Exception("Vault '" + vaultID + "' does not exist");
        }

        return vault;
    }

    public void addNDMRoles(CreateVault createVault, String pendingVaultId) {
        List<String> ndms = createVault.getNominatedDataManagers();
        if (ndms != null) {
            for (String ndm : ndms) {
                if (ndm != null && !ndm.isEmpty()) {
                    RoleAssignment ra = new RoleAssignment();
                    ra.setPendingVaultId(pendingVaultId);
                    ra.setRole(permissionsService.getNominatedDataManager());
                    ra.setUserId(ndm);
                    permissionsService.createRoleAssignment(ra);
                    logger.debug("NDMS + '" + ndm + "'");
                }
            }
        }
    }

    public void addDepositorRoles(CreateVault createVault, String pendingVaultId) {

        // if vault already has depositors delete them and readd
        List<String> depositors = createVault.getDepositors();
        if (depositors != null) {
            for (String dep : depositors) {
                if (dep != null && !dep.isEmpty()) {
                    RoleAssignment ra = new RoleAssignment();
                    ra.setPendingVaultId(pendingVaultId);
                    ra.setRole(permissionsService.getDepositor());
                    ra.setUserId(dep);
                    permissionsService.createRoleAssignment(ra);
                    logger.debug("Depositor + '" + dep + "'");
                }
            }
        }
    }

    public void addOwnerRole(CreateVault createVault, String pendingVaultId, String userID) {
        Boolean isOwner = createVault.getIsOwner();
        logger.debug("IsOwner is '" + isOwner + "'");
        String ownerId = userID;
        if ((isOwner != null && !isOwner) || userID == null) {
            ownerId = createVault.getVaultOwner();
        }
        logger.debug("VaultOwner is '" + ownerId + "'");
        if (ownerId != null) {
            RoleAssignment ownerRoleAssignment = new RoleAssignment();
            ownerRoleAssignment.setUserId(ownerId);
            ownerRoleAssignment.setPendingVaultId(pendingVaultId);
            ownerRoleAssignment.setRole(permissionsService.getDataOwner());
            permissionsService.createRoleAssignment(ownerRoleAssignment);
        } else {
            // error!
        }
    }

    public void addCreator(CreateVault createVault, String userID, String pendingVaultId) {
        // this adds the creator to the full role assignments table as owner (so it is
        // displayed only to the creator while still pending)
        RoleAssignment pendingVaultCreatorRoleAssignment = new RoleAssignment();
        pendingVaultCreatorRoleAssignment.setUserId(userID);
        pendingVaultCreatorRoleAssignment.setPendingVaultId(pendingVaultId);
        pendingVaultCreatorRoleAssignment.setRole(permissionsService.getVaultCreator());

        logger.debug("Role userID: '" + userID + "'");
        logger.debug("Role vaultID: '" + pendingVaultId + "'");
        logger.debug("Role type: '" + pendingVaultCreatorRoleAssignment.getRole().getType() + "'");
        // String pendingId = createVault.getPendingID();
        // if (pendingId == null || pendingId.isEmpty()) {
        permissionsService.createRoleAssignment(pendingVaultCreatorRoleAssignment);
        // } else {
        /*
         * TODO: once we add the ability to set a different owner this will be required
         * as it will be possible that the people who can see it will have changed
         */
        // permissionsService.updateRoleAssignment(dataOwnerRoleAssignment);
        // }
    }

    public PendingVault processDataCreatorParams(CreateVault createVault, PendingVault vault) {
        List<String> dcs = createVault.getDataCreators();
        if (dcs != null) {
            logger.debug("Data creator list is :'" + dcs + "'");
            List<PendingDataCreator> creators = new ArrayList<>();
            for (String creator : dcs) {
                if (creator != null && !creator.isEmpty()) {
                    PendingDataCreator dc = new PendingDataCreator();
                    dc.setName(creator);
                    dc.setPendingVault(vault);
                    creators.add(dc);
                }
            }
            vault.setDataCreator(creators);
        } else {
            logger.debug("Data creator list is :null");
        }

        List<PendingDataCreator> creators = vault.getDataCreators();
        if (creators != null && !creators.isEmpty()) {
            pendingDataCreatorsService.addPendingCreators(creators);
        }

        return vault;
    }

    public PendingVault processVaultParams(PendingVault vault, CreateVault createVault, String userID)
            throws Exception {
        // PendingVault vault = new PendingVault();

        String pendingId = createVault.getPendingID();
        logger.debug("Pending ID is: '" + pendingId + "'");
        if (pendingId != null && !pendingId.isEmpty()) {
            // this vault has previously been saved mid completion
            vault.setId(pendingId);
        }

        Boolean affirmed = createVault.getAffirmed();
        logger.debug("Affirmed is: '" + affirmed + "'");
        if (affirmed != null) {
            vault.setAffirmed(affirmed);
        }

        String name = createVault.getName();
        logger.debug("Name is: '" + name + "'");
        if (name != null) {
            vault.setName(name);
        }

        String desc = createVault.getDescription();
        logger.debug("Description is: '" + desc + "'");
        if (desc != null) {
            vault.setDescription(desc);
        }

        String notes = createVault.getNotes();
        logger.debug("Notes is: '" + notes + "'");
        if (notes != null) {
            vault.setNotes(notes);
        }

        String estimate = createVault.getEstimate();
        logger.debug("Estimate is: '" + estimate + "'");
        if (estimate != null) {
            PendingVault.Estimate enumEst = PendingVault.Estimate.valueOf(estimate);
            if (enumEst != null) {
                vault.setEstimate(enumEst);
            } else {
                vault.setEstimate(PendingVault.Estimate.UNKNOWN);
            }
        }

        String billingType = createVault.getBillingType();
        logger.debug("Billing Type is: '" + billingType + "'");
        if (billingType != null && !billingType.isEmpty()) {
            PendingVault.Billing_Type enumBT = PendingVault.Billing_Type.valueOf(billingType);
            if (enumBT != null) {
                vault.setBillingType(enumBT);
            }
        } else {
            vault.setBillingType(null);
        }

        String policyId = (createVault.getPolicyInfo() != null) ? createVault.getPolicyInfo().split("-")[0] : null;
        logger.debug("Retention policy id is: '" + policyId + "'");
        if (policyId != null) {
            RetentionPolicy retentionPolicy = retentionPoliciesService.getPolicy(policyId);

            if (retentionPolicy == null) {
                logger.error("RetentionPolicy '" + policyId + "' does not exist");
                throw new Exception("RetentionPolicy '" + policyId + "' does not exist");
            }
            vault.setRetentionPolicy(retentionPolicy);
        }

        String groupId = createVault.getGroupID();
        logger.debug("Group id is: '" + groupId + "'");
        if (groupId != null) {
            Group group = groupsService.getGroup(groupId);
            if (group == null) {
                logger.error("Group '" + groupId + "' does not exist");
                throw new Exception("Group '" + groupId + "' does not exist");
            }
            vault.setGroup(group);
        }

        String sliceID = createVault.getSliceID();
        logger.debug("Slice ID is: '" + sliceID + "'");
        if (sliceID != null) {
            vault.setSliceID(sliceID);
        }

        if (vault.getBillingType() != null) {

            if (vault.getBillingType().equals(PendingVault.Billing_Type.WILL_PAY)) {
                String authoriser = createVault.getBudgetAuthoriser();
                logger.debug("Authoriser is: '" + authoriser + "'");
                if (authoriser != null) {
                    vault.setAuthoriser(authoriser);
                }

                String schoolOrUnit = createVault.getBudgetSchoolOrUnit();
                logger.debug("schoolOrUnit is: '" + schoolOrUnit + "'");
                if (schoolOrUnit != null) {
                    vault.setSchoolOrUnit(schoolOrUnit);
                }

                String subunit = createVault.getBudgetSubunit();
                logger.debug("Subunit is: '" + subunit + "'");
                if (subunit != null) {
                    vault.setSubunit(subunit);
                }

                String projectTitle = createVault.getProjectTitle();
                logger.debug("ProjectTitle is: '" + projectTitle + "'");
                if (projectTitle != null) {
                    vault.setProjectTitle(projectTitle);
                }

                String paymentDetails = createVault.getPaymentDetails();
                logger.debug("paymentDetails: '" + paymentDetails + "'");
                if (paymentDetails != null) {
                    vault.setPaymentDetails(paymentDetails);
                }

            }

            if (vault.getBillingType().equals(PendingVault.Billing_Type.GRANT_FUNDING)) {
                String authoriser = createVault.getGrantAuthoriser();
                logger.debug("Authoriser is: '" + authoriser + "'");
                if (authoriser != null) {
                    vault.setAuthoriser(authoriser);
                }

                String schoolOrUnit = createVault.getGrantSchoolOrUnit();
                logger.debug("schoolOrUnit is: '" + schoolOrUnit + "'");
                if (schoolOrUnit != null) {
                    vault.setSchoolOrUnit(schoolOrUnit);
                }

                String subunit = createVault.getGrantSubunit();
                logger.debug("Subunit is: '" + subunit + "'");
                if (subunit != null) {
                    vault.setSubunit(subunit);
                }

                String projectTitle = createVault.getProjectTitle();
                logger.debug("ProjectTitle is: '" + projectTitle + "'");
                if (projectTitle != null) {
                    vault.setProjectTitle(projectTitle);
                }
            }

            if (vault.getBillingType().equals(PendingVault.Billing_Type.BUDGET_CODE)) {
                String authoriser = createVault.getBudgetAuthoriser();
                logger.debug("Authoriser is: '" + authoriser + "'");
                if (authoriser != null) {
                    vault.setAuthoriser(authoriser);
                }

                String schoolOrUnit = createVault.getBudgetSchoolOrUnit();
                logger.debug("schoolOrUnit is: '" + schoolOrUnit + "'");
                if (schoolOrUnit != null) {
                    vault.setSchoolOrUnit(schoolOrUnit);
                }

                String subunit = createVault.getBudgetSubunit();
                logger.debug("Subunit is: '" + subunit + "'");
                if (subunit != null) {
                    vault.setSubunit(subunit);
                }
            }
        }

        // this the creator of the pending vault not necessarily the prospective owner
        User user = usersService.getUser(userID);
        if (user == null) {
            logger.error("User '" + userID + "' does not exist");
            throw new Exception("User '" + userID + "' does not exist");
        }
        vault.setUser(user);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        String grantEndDate = (createVault.getBillingGrantEndDate() != null &&
                !createVault.getBillingGrantEndDate().isEmpty()) ? createVault.getBillingGrantEndDate() : null;
       
        if (grantEndDate == null) {
           grantEndDate = (createVault.getGrantEndDate() != null &&
                !createVault.getGrantEndDate().isEmpty()) ? createVault.getGrantEndDate() : null; 
        }

        try {
            vault.setGrantEndDate(formatter.parse(grantEndDate));
        } catch (ParseException | NullPointerException ex) {
            logger.error("Grant date is not in the right format: " + grantEndDate);
            vault.setGrantEndDate(null);
        }

        String reviewDate = createVault.getReviewDate();
        if (reviewDate != null) {
            try {
                vault.setReviewDate(formatter.parse(reviewDate));
            } catch (ParseException | NullPointerException ex) {
                logger.error("Review date is not in the right format: " + reviewDate);
                vault.setReviewDate(null);
            }
        }

        String contact = createVault.getContactPerson();
        if (contact != null) {
            logger.debug("Contact is '" + contact + "'");
            vault.setContact(contact);
        }

        Boolean pureLink = createVault.getPureLink();
        logger.debug("Pure Link is: '" + pureLink + "'");
        if (pureLink != null) {
            vault.setPureLink(pureLink);
        }

        Boolean confirmed = createVault.getConfirmed();
        logger.debug("Confirmed is: '" + confirmed + "'");
        if (confirmed != null) {
            vault.setConfirmed(confirmed);
        }

        String sliceQueryChoice = createVault.getSliceQueryChoice();
        logger.debug("sliceQueryChoice is: '" + sliceQueryChoice + "'");
        if (sliceQueryChoice != null && !sliceQueryChoice.isEmpty()) {
            vault.setSliceQueryChoice(Slice_Query_Choice.valueOf(sliceQueryChoice));
        } else {
            vault.setSliceQueryChoice(null);
        }

        String fundingQueryChoice = createVault.getFundingQueryChoice();
        logger.debug("fundingQueryChoice is: '" + fundingQueryChoice + "'");
        if (fundingQueryChoice != null && !fundingQueryChoice.isEmpty()) {
            vault.setFundingQueryChoice(Funding_Query_Choice.valueOf(fundingQueryChoice));
        } else {
            vault.setFundingQueryChoice(null);
        }

        String feewaiverQueryChoice = createVault.getFeewaiverQueryChoice();
        logger.debug("feewaiverQueryChoice is: '" + feewaiverQueryChoice + "'");
        if (feewaiverQueryChoice != null && !feewaiverQueryChoice.isEmpty()) {
            vault.setFeewaiverQueryChoice(Feewaiver_Query_Choice.valueOf(feewaiverQueryChoice));
        } else {
            vault.setFeewaiverQueryChoice(null);
        }

        return vault;
    }

    public List<PendingVault> getPendingVaults() {
        return pendingVaultDAO.list();
    }

    public int getTotalNumberOfPendingVaults() {
        return pendingVaultDAO.list() != null ? pendingVaultDAO.list().size() : 0;
    }

    public List<PendingVault> getPendingVaults(String userId, String sort, String order, String offset,
            String maxResult) {
        return pendingVaultDAO.list(userId, sort, order, offset, maxResult);
    }

    public void sendNewPendingVaultEmail(PendingVault vault) {
        // send mail to admin email which in turn sets up unidesk call

        logger.info("Sending pending vault submitted email");
        HashMap<String, Object> model = new HashMap<>();
        model.put(EMAIL_VAULT_NAME, vault.getName());
        model.put(EMAIL_GROUP_NAME, vault.getGroup().getName());
        model.put(EMAIL_SUBMITTER_ID, vault.getUser().getID());
        LocalDate today = LocalDate.now();
        model.put(EMAIL_TIMESTAMP, today);
        this.emailService.sendTemplateMail(this.helpMail, "New Pending Vault Submitted",
                EmailTemplate.GROUP_ADMIN_PV_SUBMITTED, model);
    }
}
