package org.datavaultplatform.broker.services;

import java.math.BigDecimal;
import java.util.*;

import org.datavaultplatform.common.event.roles.CreateRoleAssignment;
import org.datavaultplatform.common.event.vault.Create;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.dao.VaultDAO;
import org.datavaultplatform.common.request.CreateVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.datavaultplatform.common.email.EmailTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VaultsService {

    private final Logger logger = LoggerFactory.getLogger(VaultsService.class);
    private final VaultDAO vaultDAO;

    private final RolesAndPermissionsService rolesAndPermissionsService;

    private final RetentionPoliciesService retentionPoliciesService;

    private final DataCreatorsService dataCreatorsService;

    private final BillingService billingService;
    private final UsersService usersService;
    private final EventService eventService;
    private final ClientsService clientsService;
    private final EmailService emailService;

    @Autowired
    public VaultsService(VaultDAO vaultDAO, RolesAndPermissionsService rolesAndPermissionsService,
        RetentionPoliciesService retentionPoliciesService, DataCreatorsService dataCreatorsService,
        BillingService billingService, UsersService usersService, EventService eventService,
        ClientsService clientsService, EmailService emailService) {
        this.vaultDAO = vaultDAO;
        this.rolesAndPermissionsService = rolesAndPermissionsService;
        this.retentionPoliciesService = retentionPoliciesService;
        this.dataCreatorsService = dataCreatorsService;
        this.billingService = billingService;
        this.usersService = usersService;
        this.eventService = eventService;
        this.clientsService = clientsService;
        this.emailService = emailService;
    }


    public RetentionPoliciesService getRetentionPoliciesService() {
        return retentionPoliciesService;
    }

    public List<Vault> getVaults() {
        return vaultDAO.list();
    }

    public List<Vault> getVaults(String userId, String sort, String order, String offset, String maxResult) {
        return vaultDAO.list(userId, sort, order, offset, maxResult);
    }

    public void addVault(Vault vault) {
        Date d = new Date();
        vault.setCreationTime(d);
        vaultDAO.save(vault);
    }

    public void addVaultEvent(Vault vault, String clientKey, String userID) {
        Create vaultEvent = new Create(vault.getID());
        vaultEvent.setVault(vault);
        vaultEvent.setUser(usersService.getUser(userID));
        vaultEvent.setAgentType(Agent.AgentType.BROKER);
        vaultEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
        eventService.addEvent(vaultEvent);
    }

    private void addRoleEvent(RoleAssignment ra, String assigneeId, String creatorId, String clientKey) {
        CreateRoleAssignment roleAssignmentEvent = new CreateRoleAssignment(ra, creatorId);
        roleAssignmentEvent.setVault(this.getVault(ra.getVaultId()));
        roleAssignmentEvent.setUser(usersService.getUser(creatorId));
        roleAssignmentEvent.setAgentType(Agent.AgentType.BROKER);
        roleAssignmentEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
        roleAssignmentEvent.setAssignee(usersService.getUser(assigneeId));
        roleAssignmentEvent.setRole(ra.getRole());

        eventService.addEvent(roleAssignmentEvent);
    }

    public void sendVaultOwnerEmail(Vault vault, String homePage, String helpPage, User user) {
        // send mail to owner
        this.sendEmail(vault, user.getEmail(), "A new vault you own has been created", "Owner",
                EmailTemplate.USER_VAULT_CREATE, homePage, helpPage);
    }

    public void sendVaultDepositorsEmail(Vault vault, String homePage, String helpPage, User user) {
        // send mail to depositor
        this.sendEmail(vault, user.getEmail(), "A new vault you have a role on has been created", "Depositor",
                EmailTemplate.USER_VAULT_CREATE, homePage, helpPage);
    }

    public void sendVaultNDMsEmail(Vault vault, String homePage, String helpPage, User user) {
        // send mail to ndm
        this.sendEmail(vault, user.getEmail(), "A new vault you have a role on has been created", "Nominated Data Manager",
                EmailTemplate.USER_VAULT_CREATE, homePage, helpPage);
    }

    private void sendEmail(Vault vault, String email, String subject, String role, String template, String homePage, String helpPage) {
        HashMap<String, Object> model = new HashMap<>();
        model.put("home-page", homePage);
        model.put("help-page", helpPage);
        model.put("vault-name", vault.getName());
        model.put("group-name", vault.getGroup().getName());
        model.put("vault-id", vault.getID());
        model.put("vault-review-date", vault.getReviewDate());
        model.put("role-name", role);
        emailService.sendTemplateMail(email, subject, template, model);
    }

    public void orphanVault(Vault vault) {
        vault.setUser(null);
        vaultDAO.update(vault);

        RoleModel dataOwnerRole = rolesAndPermissionsService.getDataOwner();
        rolesAndPermissionsService.getRoleAssignmentsForRole(dataOwnerRole.getId()).stream()
                .filter(roleAssignment -> vault.getID().equals(roleAssignment.getVaultId()))
                .findFirst()
                .ifPresent(roleAssignment -> rolesAndPermissionsService.deleteRoleAssignment(roleAssignment.getId()));
    }

    public void updateVault(Vault vault) {
        vaultDAO.update(vault);
    }

    public void saveOrUpdateVault(Vault vault) {
        vaultDAO.saveOrUpdateVault(vault);
    }

    public Vault getVault(String vaultID) {
        return vaultDAO.findById(vaultID);
    }

    public List<Vault> search(String userId, String query, String sort, String order, String offset, String maxResult) {
        return this.vaultDAO.search(userId, query, sort, order, offset, maxResult);
    }

    public int count(String userId) {
        return vaultDAO.count(userId);
    }

    public int getRetentionPolicyCount(int status) {
        return vaultDAO.getRetentionPolicyCount(status);
    }

    public Vault checkRetentionPolicy(String vaultID) {
        // Get the vault
        Vault vault = vaultDAO.findById(vaultID);

        retentionPoliciesService.setRetention(vault);

        // Check the policy
        //retentionPoliciesService.run(vault);

        // Set the expiry date
        //vault.setRetentionPolicyExpiry(retentionPoliciesService.getReviewDate(vault));

        // Record when we checked it
        //vault.setRetentionPolicyLastChecked(new Date());

        // Update and return the policy
        vaultDAO.update(vault);
        return vault;
    }

    // Get the specified Vault object and validate it against the current User
    public Vault getUserVault(User user, String vaultID) throws Exception {
        Vault vault = getVault(vaultID);

        if (vault == null) {
            throw new Exception("Vault '" + vaultID + "' does not exist");
        }

        return vault;
    }

    public int getTotalNumberOfVaults(String userId) {
        return vaultDAO.getTotalNumberOfVaults(userId);
    }

    /**
     * Total number of records after applying search filter
     *
     * @param query
     * @return
     */
    public int getTotalNumberOfVaults(String userId, String query) {
        return vaultDAO.getTotalNumberOfVaults(userId, query);
    }

    public Map<String, Long> getAllProjectsSize() {
        Map<String, Long> projectSizeMap = new HashMap<>();
        List<Object[]> allProjectsSize = vaultDAO.getAllProjectsSize();
        if (allProjectsSize != null) {
            for (Object[] projectsizeArray : allProjectsSize) {
                projectSizeMap.put((String) projectsizeArray[0], (Long) projectsizeArray[1]);
            }
        }
        return projectSizeMap;
    }

    public void transferVault(Vault vault, User newOwner, String reason) {
        vault.setUser(newOwner);
        vaultDAO.update(vault);

        RoleModel dataOwnerRole = rolesAndPermissionsService.getDataOwner();
        rolesAndPermissionsService.getRoleAssignmentsForRole(dataOwnerRole.getId()).stream()
                .filter(roleAssignment -> vault.getID().equals(roleAssignment.getVaultId()))
                .findFirst()
                .ifPresent(roleAssignment -> rolesAndPermissionsService.deleteRoleAssignment(roleAssignment.getId()));

        RoleAssignment newDataOwnerAssignment = new RoleAssignment();
        newDataOwnerAssignment.setUserId(newOwner.getID());
        newDataOwnerAssignment.setVaultId(vault.getID());
        newDataOwnerAssignment.setRole(dataOwnerRole);
        rolesAndPermissionsService.createRoleAssignment(newDataOwnerAssignment);
    }

    public void addDepositorRoles(CreateVault createVault, Vault vault, String clientKey, String homePage, String helpPage) {

        // if vault already has depositors delete them and readd
        List<String> depositors = createVault.getDepositors();
        if (depositors != null) {
            for (String dep: depositors) {
                if (dep != null && ! dep.isEmpty()) {
                    RoleAssignment ra = new RoleAssignment();
                    ra.setVaultId(vault.getID());
                    ra.setRole(rolesAndPermissionsService.getDepositor());
                    ra.setUserId(dep);
                    rolesAndPermissionsService.createRoleAssignment(ra);

                    this.addRoleEvent(ra, createVault.getVaultOwner(), createVault.getVaultCreator(), clientKey);
                    this.sendVaultDepositorsEmail(vault, homePage, helpPage, usersService.getUser(dep));
                }
            }
        }
    }

    public void addOwnerRole(CreateVault createVault, Vault vault, String clientKey) {
        String ownerId = createVault.getVaultOwner();
        String creatorId = createVault.getVaultCreator();

        if (ownerId != null) {
            RoleAssignment ownerRoleAssignment = new RoleAssignment();
            ownerRoleAssignment.setUserId(ownerId);
            ownerRoleAssignment.setVaultId(vault.getID());
            ownerRoleAssignment.setRole(rolesAndPermissionsService.getDataOwner());
            rolesAndPermissionsService.createRoleAssignment(ownerRoleAssignment);

            this.addRoleEvent(ownerRoleAssignment, ownerId, creatorId, clientKey);
        } else {
            // error!
        }
    }

    public Vault processDataCreatorParams(CreateVault createVault, Vault vault) {
        List<String> dcs = createVault.getDataCreators();
        if (dcs != null) {
            logger.debug("Data creator list is :'" + dcs + "'");
            List<DataCreator> creators = new ArrayList<>();
            for (String creator : dcs) {
                if (creator != null && !creator.isEmpty()) {
                    DataCreator dc = new DataCreator();
                    dc.setName(creator);
                    dc.setVault(vault);
                    creators.add(dc);
                }
            }
            vault.setDataCreator(creators);
        } else {
            logger.debug("Data creator list is :null");
        }

        List<DataCreator> creators = vault.getDataCreators();
        if (creators != null && ! creators.isEmpty()) {
            dataCreatorsService.addCreators(creators);
        }

        return vault;
    }

    public void addNDMRoles(CreateVault createVault, Vault vault, String clientKey, String homePage, String helpPage) {
        List<String> ndms = createVault.getNominatedDataManagers();
        if (ndms != null) {
            for (String ndm: ndms) {
                if (ndm != null && !ndm.isEmpty()) {
                    RoleAssignment ra = new RoleAssignment();
                    ra.setVaultId(vault.getID());
                    ra.setRole(rolesAndPermissionsService.getNominatedDataManager());
                    ra.setUserId(ndm);
                    rolesAndPermissionsService.createRoleAssignment(ra);
                    this.addRoleEvent(ra, createVault.getVaultOwner(), createVault.getVaultCreator(), clientKey);
                    this.sendVaultNDMsEmail(vault, homePage, helpPage, usersService.getUser(ndm));
                }
            }
        }
    }

    public void addBillingInfo(CreateVault createVault, Vault vault) {
        BillingInfo billinginfo =  new BillingInfo();
        billinginfo.setAmountBilled(new BigDecimal(0));
        billinginfo.setAmountToBeBilled(new BigDecimal(0));
        billinginfo.setVault(vault);
        String billingType = createVault.getBillingType();

        PendingVault.Billing_Type enumBT = PendingVault.Billing_Type.valueOf(billingType);
        billinginfo.setBillingType(enumBT);

        if (enumBT.equals(PendingVault.Billing_Type.GRANT_FUNDING)) {
            billinginfo.setContactName(createVault.getGrantAuthoriser());
            billinginfo.setSchool(createVault.getGrantSchoolOrUnit());
            billinginfo.setSubUnit(createVault.getGrantSubunit());
            billinginfo.setProjectTitle(createVault.getProjectTitle());
        }

        if (enumBT.equals(PendingVault.Billing_Type.BUDGET_CODE)) {
            billinginfo.setContactName(createVault.getBudgetAuthoriser());
            billinginfo.setSchool(createVault.getBudgetSchoolOrUnit());
            billinginfo.setSubUnit(createVault.getBudgetSubunit());
        }

        if (enumBT.equals(PendingVault.Billing_Type.SLICE)) {
            billinginfo.setSliceID(createVault.getSliceID());
        }
        billingService.saveOrUpdateVault(billinginfo);
    }
}
