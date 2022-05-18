package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import org.apache.commons.collections4.CollectionUtils;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.roles.CreateRoleAssignment;
import org.datavaultplatform.common.event.roles.OrphanVault;
import org.datavaultplatform.common.event.roles.TransferVaultOwnership;
import org.datavaultplatform.common.event.vault.UpdatedDescription;
import org.datavaultplatform.common.event.vault.UpdatedName;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.request.TransferVault;
import org.datavaultplatform.common.response.*;
import org.datavaultplatform.common.util.RoleUtils;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@Api(name="Vaults", description = "Interact with DataVault Vaults")
public class VaultsController {

    private static final Logger logger = LoggerFactory.getLogger(VaultsController.class);

    private static final String ORPHANED_ID = "Orphaned";

    private final EmailService emailService;
    private final VaultsService vaultsService;
    private final PendingVaultsService pendingVaultsService;
    private final PendingDataCreatorsService pendingDataCreatorsService;
    private final DepositsService depositsService;
    private final ExternalMetadataService externalMetadataService;
    private final RetentionPoliciesService retentionPoliciesService;
    private final GroupsService groupsService;
    private final UsersService usersService;
    private final EventService eventService;
    private final ClientsService clientsService;
    private final DataManagersService dataManagersService;
    private final RolesAndPermissionsService permissionsService;

    // NOTE: this a placeholder and will eventually be handled by per-user config
    private final String activeDir;

    // NOTE: this a placeholder and will eventually be handled by system config
    private final String archiveDir;

    private final String homePage;
    private final String helpPage;

    @Autowired
    public VaultsController(EmailService emailService, VaultsService vaultsService,
        PendingVaultsService pendingVaultsService,
        PendingDataCreatorsService pendingDataCreatorsService, DepositsService depositsService,
        ExternalMetadataService externalMetadataService,
        RetentionPoliciesService retentionPoliciesService, GroupsService groupsService,
        UsersService usersService, EventService eventService, ClientsService clientsService,
        DataManagersService dataManagersService, RolesAndPermissionsService permissionsService,
        @Value("${activeDir}") String activeDir,
        @Value("${archiveDir}") String archiveDir,
        @Value("${home.page}") String homePage,
        @Value("${help.page}") String helpPage) {
        this.emailService = emailService;
        this.vaultsService = vaultsService;
        this.pendingVaultsService = pendingVaultsService;
        this.pendingDataCreatorsService = pendingDataCreatorsService;
        this.depositsService = depositsService;
        this.externalMetadataService = externalMetadataService;
        this.retentionPoliciesService = retentionPoliciesService;
        this.groupsService = groupsService;
        this.usersService = usersService;
        this.eventService = eventService;
        this.clientsService = clientsService;
        this.dataManagersService = dataManagersService;
        this.permissionsService = permissionsService;
        this.activeDir = activeDir;
        this.archiveDir = archiveDir;
        this.homePage = homePage;
        this.helpPage = helpPage;
    }


    @ApiMethod(
            path = "/vaults",
            verb = ApiVerb.GET,
            description = "Gets a list of all Vaults for the specified User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/vaults")
    public List<VaultInfo> getVaults(@RequestHeader(HEADER_USER_ID) String userID) {

        List<VaultInfo> vaultResponses = permissionsService.getRoleAssignmentsForUser(userID).stream()
                .filter(roleAssignment -> (RoleType.VAULT == roleAssignment.getRole().getType() ||
                        RoleUtils.isDataOwner(roleAssignment))  && (roleAssignment.getVaultId() != null))
                .map(roleAssignment -> vaultsService.getVault(roleAssignment.getVaultId()).convertToResponse())
                .sorted(Comparator.comparing(VaultInfo::getCreationTime))
                .collect(Collectors.toList());
        Collections.reverse(vaultResponses);
        if(CollectionUtils.isNotEmpty(vaultResponses)) {
            for (VaultInfo vault : vaultResponses) {
                User owner = permissionsService.getVaultOwner(vault.getID());
                if(owner != null) {
                    vault.setOwnerId(owner.getID());
                }
            }
        }
        return vaultResponses;
    }

    @ApiMethod(
            path = "/pendingVaults",
            verb = ApiVerb.GET,
            description = "Gets a list of all Pending Vaults for the specified User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/pendingVaults")
    public List<VaultInfo> getPendingVaults(@RequestHeader(HEADER_USER_ID) String userID) {

        List<VaultInfo> vaultResponses = permissionsService.getRoleAssignmentsForUser(userID).stream()
                .filter(roleAssignment -> (RoleUtils.isVaultCreator(roleAssignment)) && (roleAssignment.getPendingVaultId() != null))
                .map(roleAssignment -> pendingVaultsService.getPendingVault(roleAssignment.getPendingVaultId()).convertToResponse())
                .sorted(Comparator.comparing(VaultInfo::getCreationTime))
                .collect(Collectors.toList());
        Collections.reverse(vaultResponses);
        if(CollectionUtils.isNotEmpty(vaultResponses)) {
            for (VaultInfo vault : vaultResponses) {
                User owner = permissionsService.getPendingVaultOwner(vault.getID());
                if(owner != null) {
                    vault.setOwnerId(owner.getID());
                }

                User vaultCreator = permissionsService.getPendingVaultCreator(vault.getID());
                if(vaultCreator != null) {
                    vault.setVaultCreatorId(vaultCreator.getID());
                }
            }
        }
        return vaultResponses;
        //return null;
    }

    @GetMapping("/vaults/user")
    public List<VaultInfo> getVaultsForUser(@RequestParam(value = "userID", required = true)String userID) {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        User user = usersService.getUser(userID);
        for (Vault vault : user.getVaults()) {
            vaultResponses.add(vault.convertToResponse());
        }
        vaultResponses.sort(Comparator.comparing(VaultInfo::getCreationTime));
        Collections.reverse(vaultResponses);
        return vaultResponses;
    }


    @PostMapping("/vaults/{vaultId}/transfer")
    public ResponseEntity<Void> transferVault(@RequestHeader(HEADER_USER_ID) String userID,
                                        @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                        @PathVariable("vaultId") String vaultId,
                                        @RequestBody TransferVault transfer) {

        Vault vault = vaultsService.getVault(vaultId);

        User currentOwner = null;
        List<RoleAssignment> roleAssignmentsForVault = permissionsService.getRoleAssignmentsForVault(vault.getID());

        // Note - I think this could be better coded as a Lambda
        for (RoleAssignment roleAssignment : roleAssignmentsForVault) {
            if (RoleUtils.isDataOwner(roleAssignment)) {
                currentOwner = usersService.getUser(roleAssignment.getUserId());
            }
        }

        //User currentOwner = vault.getUser();

        if (transfer.isOrphaning()) {
            vaultsService.orphanVault(vault);

            OrphanVault orphanVaultEvent = new OrphanVault(vault, userID);
            orphanVaultEvent.setVault(vault);
            orphanVaultEvent.setUser(usersService.getUser(userID));
            orphanVaultEvent.setAgentType(Agent.AgentType.BROKER);
            orphanVaultEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());

            eventService.addEvent(orphanVaultEvent);
        } else {
            String previousUserID = VaultsController.ORPHANED_ID;
            if (currentOwner != null) {
                previousUserID = currentOwner.getID();
            }

            vaultsService.transferVault(vault, usersService.getUser(transfer.getUserId()), transfer.getReason());

            logger.debug("send email for transfer ownership from: "+previousUserID+" to "+transfer.getUserId());
            sendEmails(EmailTemplate.TRANSFER_VAULT_OWNERSHIP, vault, userID, previousUserID, transfer.getUserId());

            TransferVaultOwnership transferEvent = new TransferVaultOwnership(transfer, vault, userID);
            transferEvent.setVault(vault);
            transferEvent.setUser(usersService.getUser(userID));
            transferEvent.setAgentType(Agent.AgentType.BROKER);
            transferEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
            transferEvent.setAssignee(usersService.getUser(transfer.getUserId()));

            eventService.addEvent(transferEvent);
        }

        if (transfer.isChangingRoles()) {
            long roleId = transfer.getRoleId();

            RoleModel role = permissionsService.getRole(roleId);
            RoleAssignment assignment = new RoleAssignment();
            assignment.setRole(role);
            assignment.setUserId(currentOwner.getID());
            assignment.setVaultId(vaultId);

            permissionsService.createRoleAssignment(assignment);

            // Jira RSS212-099 - 'Don't email the old owners under any circumstances', so commenting out this email.
            //sendEmails(EmailTemplate.TRANSFER_VAULT_OWNERSHIP, vault, userID, transfer.getUserId());

            CreateRoleAssignment roleAssignmentEvent = new CreateRoleAssignment(assignment, userID);
            roleAssignmentEvent.setVault(vaultsService.getVault(assignment.getVaultId()));
            roleAssignmentEvent.setUser(usersService.getUser(userID));
            roleAssignmentEvent.setAgentType(Agent.AgentType.BROKER);
            roleAssignmentEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
            roleAssignmentEvent.setAssignee(usersService.getUser(assignment.getUserId()));
            roleAssignmentEvent.setRole(assignment.getRole());

            eventService.addEvent(roleAssignmentEvent);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/vaults/search")
    public VaultsData searchAllVaults(@RequestHeader(HEADER_USER_ID) String userID,
                                      @RequestParam String query,
                                      @RequestParam(value = "sort", required = false) String sort,
                                      @RequestParam(value = "order", required = false)
                                      @ApiQueryParam(name = "order", description = "Vault sort order", allowedvalues = {"asc", "desc"}, defaultvalue = "asc", required = false) String order,
                                      @RequestParam(value = "offset", required = false)
                                      @ApiQueryParam(name = "offset", description = "Vault row id ", defaultvalue = "0", required = false) String offset,
                                      @RequestParam(value = "maxResult", required = false)
                                      @ApiQueryParam(name = "maxResult", description = "Number of records", required = false) String maxResult) {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        int recordsTotal = 0;
        int recordsFiltered = 0;
        List<Vault> vaults = vaultsService.search(userID, query, sort, order, offset, maxResult);
        if(CollectionUtils.isNotEmpty(vaults)) {
            for (Vault vault : vaults) {
                vaultResponses.add(vault.convertToResponse());
            }
            //Map of project with its size
            Map<String, Long> projectSizeMap = vaultsService.getAllProjectsSize();
            //update project Size in the response
            for(VaultInfo vault: vaultResponses) {
                User owner = permissionsService.getVaultOwner(vault.getID());
                if(owner != null) {
                    vault.setOwnerId(owner.getID());
                    vault.setOwnerName(owner.getFirstname() + " " + owner.getLastname());
                }

                if(vault.getProjectId() != null) {
                    vault.setProjectSize(projectSizeMap.get(vault.getProjectId()));
                }
            }
            recordsTotal = vaultsService.getTotalNumberOfVaults(userID);
            recordsFiltered = vaultsService.getTotalNumberOfVaults(userID, query);
        }

        VaultsData data = new VaultsData();
        data.setRecordsTotal(recordsTotal);
        data.setRecordsFiltered(recordsFiltered);
        data.setData(vaultResponses);
        return data;
    }

    @GetMapping("/pendingVaults/search")
    public VaultsData searchAllPendingVaults(@RequestHeader(HEADER_USER_ID) String userID,
                                      @RequestParam String query,
                                      @RequestParam(value = "sort", required = false) String sort,
                                      @RequestParam(value = "order", required = false)
                                      @ApiQueryParam(name = "order", description = "Vault sort order", allowedvalues = {"asc", "desc"}, defaultvalue = "asc", required = false) String order,
                                      @RequestParam(value = "offset", required = false)
                                      @ApiQueryParam(name = "offset", description = "Vault row id ", defaultvalue = "0", required = false) String offset,
                                      @RequestParam(value = "confirmed", required = false)
                                      @ApiQueryParam(name = "confirmed", description = "True = confirmed records only, false saved ones and null all", required = false) String confirmed,
                                      @RequestParam(value = "maxResult", required = false)
                                      @ApiQueryParam(name = "maxResult", description = "Number of records", required = false) String maxResult) {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        int recordsTotal = 0;
        int recordsFiltered = 0;
        List<PendingVault> vaults = pendingVaultsService.search(userID, query, sort, order, offset, maxResult, confirmed);
        if(CollectionUtils.isNotEmpty(vaults)) {
            for (PendingVault vault : vaults) {
            	User owner = permissionsService.getPendingVaultOwner(vault.getId());
            	if(owner != null) {
            		vault.setOwner(owner);
            	}
            	
            	VaultInfo vaultInfo = vault.convertToResponse();
            	User vaultCreator = permissionsService.getPendingVaultCreator(vault.getId());
            	if(vaultCreator != null) {
            		vaultInfo.setVaultCreatorId(vaultCreator.getID());
            	}
            	
                vaultResponses.add(vaultInfo);
            }

            recordsTotal = pendingVaultsService.getTotalNumberOfPendingVaults(userID, confirmed);
            recordsFiltered = pendingVaultsService.getTotalNumberOfPendingVaults(userID, query, confirmed);
        }

        VaultsData data = new VaultsData();
        data.setRecordsTotal(recordsTotal);
        data.setRecordsFiltered(recordsFiltered);
        data.setData(vaultResponses);
        return data;
    }

    @GetMapping(value = "/vaults/deposits/search")
    public List<DepositInfo> searchAllDeposits(@RequestHeader(HEADER_USER_ID) String userID,
                                               @RequestParam(value = "query", required = false, defaultValue = "") String query,
                                               @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                                               @RequestParam(value = "order", required = false, defaultValue = "desc") String order) {

        List<DepositInfo> depositResponses = new ArrayList<>();
        for (Deposit deposit : depositsService.search(query, sort, order, userID)) {
            //deposit.convertToResponse();
            DepositInfo depositInfo = deposit.convertToResponse();
            User depositor = usersService.getUser(depositInfo.getUserID());
            depositInfo.setUserName(depositor.getFirstname() + " " + depositor.getLastname());
            Vault vault = vaultsService.getVault(depositInfo.getVaultID());
            depositInfo.setVaultName(vault.getName());
            User vaultOwner = permissionsService.getVaultOwner(vault.getID());
            if(vaultOwner != null) {
                depositInfo.setVaultOwnerID(vaultOwner.getID());
                depositInfo.setVaultOwnerName(vaultOwner.getFirstname() + " " + vaultOwner.getLastname());
            }
            depositInfo.setDatasetID(vault.getDataset().getID());
            depositInfo.setGroupName(vault.getGroup().getName());
            depositInfo.setGroupID(vault.getGroup().getID());
            depositInfo.setVaultReviewDate(vault.getReviewDate().toString());
            depositResponses.add(depositInfo);
            //for (Deposit deposit : depositsService.search(query, sort, userID)) {
            //    depositResponses.add(deposit.convertToResponse());
        }
        return depositResponses;
    }


    @GetMapping("/vaults/deposits/data/search")
    public DepositsData searchAllDepositsData(@RequestHeader(HEADER_USER_ID) String userID,
                                              @RequestParam(value = "query", required = false, defaultValue = "") String query,
                                              @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                                              @RequestParam(value = "order", required = false, defaultValue = "desc") String order) {


        List<DepositInfo> depositResponses = new ArrayList<>();

        List<Deposit> deposits = depositsService.search(query, sort, order, userID);
        if(CollectionUtils.isNotEmpty(deposits)) {
            for (Deposit deposit : deposits) {
                DepositInfo depositInfo = deposit.convertToResponse();
                User depositor = usersService.getUser(depositInfo.getUserID());
                depositInfo.setUserName(depositor.getFirstname() + " " + depositor.getLastname());
                Vault vault = vaultsService.getVault(depositInfo.getVaultID());
                depositInfo.setVaultName(vault.getName());
                User vaultOwner = permissionsService.getVaultOwner(vault.getID());
                if(vaultOwner != null) {
                    depositInfo.setVaultOwnerID(vaultOwner.getID());
                    depositInfo.setVaultOwnerName(vaultOwner.getFirstname() + " " + vaultOwner.getLastname());
                }
                depositInfo.setDatasetID(vault.getDataset().getID());
                depositInfo.setGroupName(vault.getGroup().getName());
                depositInfo.setGroupID(vault.getGroup().getID());
                depositInfo.setVaultReviewDate(vault.getReviewDate().toString());
                depositResponses.add(depositInfo);
            }
        }

        DepositsData data = new DepositsData();
        // data.setRecordsTotal(recordsTotal);
        // data.setRecordsFiltered(recordsFiltered);
        data.setData(depositResponses);
        return data;

    }

    @PostMapping("/pendingVaults/update")
    public VaultInfo updatePendingVault(@RequestHeader(HEADER_USER_ID) String userID,
                                        @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                        @RequestBody CreateVault createVault) throws Exception {
        PendingVault vault = pendingVaultsService.getPendingVault(createVault.getPendingID());
        vault = pendingVaultsService.processVaultParams(vault, createVault, userID);

        pendingVaultsService.addOrUpdatePendingVault(vault);

        // delete all the previously assigned roles / creatores etc. and re-add (whether they have changed or not easier than working out what has changed)
        List<RoleAssignment> previousRoles = permissionsService.getRoleAssignmentsForPendingVault(vault.getId());
        if (previousRoles != null && ! previousRoles.isEmpty()) {
            for (RoleAssignment pr : previousRoles) {
                permissionsService.deleteRoleAssignment(pr.getId());
            }
        }

        List<PendingDataCreator> previousCreators = vault.getDataCreators();
        if (previousCreators != null && ! previousCreators.isEmpty()) {
            for (PendingDataCreator pdc : previousCreators) {
                pendingDataCreatorsService.deletePendingDataCreator(pdc.getId());
            }
        }

        pendingVaultsService.addDepositorRoles(createVault, vault.getId());

        pendingVaultsService.addOwnerRole(createVault, vault.getId(), userID);

        vault = pendingVaultsService.processDataCreatorParams(createVault, vault);

        pendingVaultsService.addNDMRoles(createVault, vault.getId());

        pendingVaultsService.addCreator(createVault, userID, vault.getId());

        //Create vaultEvent = new Create(vault.getId());
        //vaultEvent.setVault(vault);
        //vaultEvent.setUser(usersService.getUser(userID));
        //vaultEvent.setAgentType(Agent.AgentType.BROKER);
        //vaultEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());

        //eventService.addEvent(vaultEvent);

        // Check the retention policy of the newly created vault
        //try {
        //    vaultsService.checkRetentionPolicy(vault.getId());
        //} catch (Exception e) {
        //    logger.error("Fail to check retention policy: "+e);
        //    e.printStackTrace();
        //    throw e;
        //}
        return vault.convertToResponse();
    }
    @PostMapping(value = "/pendingVaults")
    public VaultInfo addPendingVault(@RequestHeader(HEADER_USER_ID) String userID,
                              @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                              @RequestBody CreateVault createVault) throws Exception {
        PendingVault vault = new PendingVault();
        vault = pendingVaultsService.processVaultParams(vault, createVault, userID);

        pendingVaultsService.addOrUpdatePendingVault(vault);

        pendingVaultsService.addDepositorRoles(createVault, vault.getId());

        pendingVaultsService.addOwnerRole(createVault, vault.getId(), userID);

        vault = pendingVaultsService.processDataCreatorParams(createVault, vault);

        pendingVaultsService.addNDMRoles(createVault, vault.getId());

        pendingVaultsService.addCreator(createVault, userID, vault.getId());

        // Check the retention policy of the newly created vault
        //try {
        //    vaultsService.checkRetentionPolicy(vault.getId());
        //} catch (Exception e) {
        //    logger.error("Fail to check retention policy: "+e);
        //    e.printStackTrace();
        //    throw e;
        //}
        return vault.convertToResponse();
    }

    @PostMapping("/vaults")
    public VaultInfo addVault(@RequestHeader(HEADER_USER_ID) String userID,
                              @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                              @RequestBody CreateVault createVault) throws Exception {

        Vault vault = new Vault();
        vault.setName(createVault.getName());
        vault.setDescription(createVault.getDescription());
        vault.setAffirmed(createVault.getAffirmed());
        vault.setNotes(createVault.getNotes());
        if (createVault.getEstimate() != null  && ! createVault.getEstimate().isEmpty()) {
            vault.setEstimate(PendingVault.Estimate.valueOf(createVault.getEstimate()));
        }
        vault.setContact(createVault.getContactPerson());
        vault.setPureLink(createVault.getPureLink());

        String policyID = createVault.getPolicyInfo().split("-")[0];
        RetentionPolicy retentionPolicy = retentionPoliciesService.getPolicy(policyID);
        if (retentionPolicy == null) {
            logger.error("RetentionPolicy '" + policyID + "' does not exist");
            throw new Exception("RetentionPolicy '" + policyID + "' does not exist");
        }
        vault.setRetentionPolicy(retentionPolicy);

        Group group = groupsService.getGroup(createVault.getGroupID());
        if (group == null) {
            logger.error("Group '" + createVault.getGroupID() + "' does not exist");
            throw new Exception("Group '" + createVault.getGroupID() + "' does not exist");
        }
        vault.setGroup(group);

        User user = usersService.getUser(userID);
        if (user == null) {
            logger.error("User '" + userID + "' does not exist");
            throw new Exception("User '" + userID + "' does not exist");
        }
        vault.setUser(user);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            vault.setGrantEndDate(formatter.parse(createVault.getGrantEndDate()));
        } catch (ParseException|NullPointerException ex) {
            logger.error("Grant date is not in the right format: "+createVault.getGrantEndDate());
            vault.setGrantEndDate(null);
        }

        try {
            vault.setReviewDate(formatter.parse(createVault.getReviewDate()));
        } catch (ParseException|NullPointerException ex) {
            logger.error("Review date is not in the right format: "+createVault.getReviewDate());
            vault.setGrantEndDate(null);
        }


        vaultsService.addVault(vault);
        // TODO: Add new Pending Vault created event that includes the creators name as that will be lost when the vault is upgraded
        //Pending pendingEvent = new Pending(createVault.getPendingID());
        //pendingEvent.setVault(vault);
        //permissionsService.get
        //pendingEvent.setUser(usersService.getUser(createVault.get);
        //pendingEvent.setAgentType(Agent.AgentType.BROKER);
        //pendingEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
        vaultsService.addVaultEvent(vault, clientKey, userID);
        vaultsService.addOwnerRole(createVault, vault, clientKey);
        // send mail to owner
        vaultsService.sendVaultOwnerEmail(vault, homePage, helpPage, user);
        vaultsService.addDepositorRoles(createVault, vault, clientKey, homePage, helpPage);
        // send mail to depositors
        vault = vaultsService.processDataCreatorParams(createVault, vault);
        vaultsService.addNDMRoles(createVault, vault, clientKey, homePage, helpPage);
        // send mail to ndms
        vaultsService.addBillingInfo(createVault, vault);

        // Check the retention policy of the newly created vault
        try {
            vaultsService.checkRetentionPolicy(vault.getID());
        } catch (Exception e) {
            logger.error("Fail to check retention policy: "+e);
            e.printStackTrace();
            throw e;
        }
        return vault.convertToResponse();
    }

    @GetMapping("/vaults/{vaultid}")
    public VaultInfo getVault(@RequestHeader(HEADER_USER_ID) String userID,
                              @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);
        if (vault != null) {
            return vault.convertToResponse();
        } else {
            return null;
        }
    }

    @GetMapping("/pendingVaults/{vaultid}")
    public VaultInfo getPendingVault(@RequestHeader(HEADER_USER_ID) String userID,
                              @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        PendingVault vault = pendingVaultsService.getUserPendingVault(user, vaultID);
        User owner = permissionsService.getPendingVaultOwner(vaultID);
        List<User> ndms = permissionsService.getPendingVaultNDMs(vaultID);
        List<User> deps = permissionsService.getPendingVaultDepositors(vaultID);
        User creator = permissionsService.getPendingVaultCreator(vaultID);
        
        vault.setOwner(owner);
        vault.setNominatedDataManagers(ndms);
        vault.setDepositors(deps);
        vault.setCreator(creator);
        if (vault.getRetentionPolicy() != null) {
            logger.debug("Vault Policy ID is '" + vault.getRetentionPolicy().getID());
            logger.debug("Vault Policy length is '" + vault.getRetentionPolicy().getMinRetentionPeriod());
        }

        if (vault != null) {
            VaultInfo retVal = vault.convertToResponse();
            logger.debug("VaultInfo policy ID is '" + retVal.getPolicyID());
            logger.debug("VaultInfo policy length is '" + retVal.getPolicyLength());
            return retVal;
        } else {
            return null;
        }
    }
    

    @GetMapping("/vaults/{vaultid}/checkretentionpolicy")
    public Vault checkVaultRetentionPolicy(@RequestHeader(HEADER_USER_ID) String userID,
                                           @PathVariable("vaultid") String vaultID) {

        return vaultsService.checkRetentionPolicy(vaultID);
    }

    @GetMapping("/vaults/{vaultid}/record")
    public Vault getVaultRecord(@RequestHeader(HEADER_USER_ID) String userID,
                                @PathVariable("vaultid") String vaultID) {

        return vaultsService.getVault(vaultID);
    }
    
    @GetMapping("/pendingVaults/{vaultid}/record")
    public PendingVault getPendingVaultRecord(@RequestHeader(HEADER_USER_ID) String userID,
                                @PathVariable("vaultid") String vaultID) {

        return pendingVaultsService.getPendingVault(vaultID);
    }

    @GetMapping("/vaults/{vaultid}/deposits")
    public List<DepositInfo> getDeposits(@RequestHeader(HEADER_USER_ID) String userID,
                                         @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        List<DepositInfo> depositResponses = new ArrayList<>();
        for (Deposit deposit : vault.getDeposits()) {
            depositResponses.add(deposit.convertToResponse());
        }
        return depositResponses;
    }

    @GetMapping("/vaults/{vaultid}/roleEvents")
    public List<EventInfo> getRoleEvents(@RequestHeader(HEADER_USER_ID) String userID,
                                         @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        List<EventInfo> events = new ArrayList<>();
        for (Event event : eventService.findVaultEvents(vault)) {
            events.add(event.convertToResponse());
        }

        return events;
    }

    @PostMapping("/vaults/{vaultid}/addDataManager")
    public VaultInfo addDataManager(@RequestHeader(HEADER_USER_ID) String userID,
                                    @PathVariable("vaultid") String vaultID,
                                    @RequestBody String unn) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        DataManager dataManager = new DataManager(unn);
        dataManager.setVault(vault);
        dataManagersService.addDataManager(dataManager);

        return vault.convertToResponse();
    }

    @GetMapping("/vaults/{vaultid}/dataManagers")
    public List<DataManager> getDataManagers(@RequestHeader(HEADER_USER_ID) String userID,
                                             @PathVariable("vaultid") String vaultID) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        List<DataManager> dataManagersList = new ArrayList<>(vault.getDataManagers());
        return dataManagersList;
    }

    @GetMapping("/vaults/{vaultid}/dataManager/{uun}")
    public DataManager getDataManager(@RequestHeader(HEADER_USER_ID) String userID,
                                      @PathVariable("vaultid") String vaultID,
                                      @PathVariable("uun") String uun) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        return vault.getDataManager(uun);
    }

    @DeleteMapping( "/vaults/{vaultid}/deleteDataManager/{dataManagerID}")
    public VaultInfo deleteDataManager(@RequestHeader(HEADER_USER_ID) String userID,
                                       @PathVariable("vaultid") String vaultID,
                                       @PathVariable("dataManagerID") String dataManagerID) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        dataManagersService.deleteDataManager(dataManagerID);

        return vault.convertToResponse();
    }

    @PostMapping("/vaults/{vaultid}/updateVaultDescription")
    public VaultInfo updateVaultDescription(@RequestHeader(HEADER_USER_ID) String userID,
                                            @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                            @PathVariable("vaultid") String vaultID,
                                            @RequestBody() String description) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);
        String oldDesc = vault.getDescription();
        vault.setDescription(description);
        vaultsService.updateVault(vault);

        UpdatedDescription descEvent = new UpdatedDescription(oldDesc, description);
        descEvent.setVault(vault);
        descEvent.setUser(usersService.getUser(userID));
        descEvent.setAgentType(Agent.AgentType.BROKER);
        descEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());

        eventService.addEvent(descEvent);

        return vault.convertToResponse();
    }

    @PostMapping(value = "/vaults/{vaultid}/updateVaultName")
    public VaultInfo updateVaultName(@RequestHeader(HEADER_USER_ID) String userID,
                                     @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                     @PathVariable("vaultid") String vaultID,
                                     @RequestBody String name) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);
        String oldName = vault.getName();
        vault.setName(name);
        vaultsService.updateVault(vault);

        UpdatedName nameEvent = new UpdatedName(oldName, name);
        nameEvent.setVault(vault);
        nameEvent.setUser(usersService.getUser(userID));
        nameEvent.setAgentType(Agent.AgentType.BROKER);
        nameEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());

        eventService.addEvent(nameEvent);

        return vault.convertToResponse();
    }

    @PostMapping("/vaults/{vaultid}/updatereviewdate")
    public VaultInfo updateVaultReviewDate(@RequestHeader(HEADER_USER_ID) String userID,
                                            @PathVariable("vaultid") String vaultID,
                                            @RequestBody String reviewDate) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        vault.setReviewDate(formatter.parse(reviewDate));

        logger.info("Updating Review Date for Vault Id " + vaultID);
        vaultsService.updateVault(vault);

        return vault.convertToResponse();
    }


    private void sendEmails(String template, Vault vault, String userId, String previousUserID, String newOwnerId) {

        User user = this.usersService.getUser(userId);
        User previousOwner = null;
        if (! previousUserID.equals(VaultsController.ORPHANED_ID)) {
            previousOwner = this.usersService.getUser(previousUserID);
        }
        User newOwner = this.usersService.getUser(newOwnerId);

        HashMap<String, Object> model = new HashMap<>();
        model.put("homepage", this.homePage);
        model.put("helppage", this.helpPage);
        model.put("vault", vault.getName());
        model.put("assignee", user.getFirstname() + " " + user.getLastname());
        if (previousOwner != null) {
            model.put("previousowner", previousOwner.getFirstname() + " " + previousOwner.getLastname());
        } else {
            model.put("previousowner", VaultsController.ORPHANED_ID);
        }

        if(newOwner != null) {
            model.put("newowner", newOwner.getFirstname() + " " + newOwner.getLastname());

            emailService.sendTemplateMailToUser(newOwner, "Datavault - Role Assignment", template, model);
        }
    }
}