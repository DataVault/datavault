package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.datavaultplatform.common.util.DateTimeUtils;
import org.datavaultplatform.common.util.RoleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@Tag(name="vaults-controller", description = "Interact with DataVault Vaults")
public class VaultsController {

    private static final Logger logger = LoggerFactory.getLogger(VaultsController.class);

    private static final String ORPHANED_ID = "Orphaned";
    private static final String ROLE_DATA_OWNER = "Data Owner";
    private static final String ROLE_DEPOSITOR = "Depositor";
    private static final String ROLE_NDM = "Nominated Data Manager";
    public static final String EMAIL_HOMEPAGE = "homepage";
    public static final String EMAIL_HELPPAGE = "helppage";
    public static final String EMAIL_VAULT = "vault";
    public static final String EMAIL_PREVIOUS_OWNER = "previousowner";
    public static final String EMAIL_ASSIGNEE = "assignee";
    public static final String EMAIL_NEW_OWNER = "newowner";

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


    @Operation(
            summary = "Gets a list of all Vaults for the specified User",
            description = "Retrieves a list of all Vaults that the specified user has a role assignment for."
    )
    @GetMapping(value = "/vaults", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VaultInfo> getVaults( @RequestHeader(HEADER_USER_ID) String userId) {

        List<VaultInfo> vaultResponses = permissionsService.getRoleAssignmentsForUser(userId).stream()
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

    @Operation(
            summary = "Gets a list of all Pending Vaults for the specified User",
            description = "Retrieves a list of all Pending Vaults that the specified user has a role assignment for."    
    )
    @GetMapping(value = "/pendingVaults", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VaultInfo> getPendingVaults( @RequestHeader(HEADER_USER_ID) String userId) {

        List<VaultInfo> vaultResponses = permissionsService.getRoleAssignmentsForUser(userId).stream()
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
    }

    @Operation(
            summary = "Get Vaults for a specific user",
            description = "Retrieves a list of all Vaults associated with a specific user."
    )
    @GetMapping(value = "/vaults/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VaultInfo> getVaultsForUser(@RequestParam(value = "userID") String userID) {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        User user = usersService.getUser(userID);
        for (Vault vault : user.getVaults()) {
            vaultResponses.add(vault.convertToResponse());
        }
        vaultResponses.sort(Comparator.comparing(VaultInfo::getCreationTime));
        Collections.reverse(vaultResponses);
        return vaultResponses;
    }


    @Operation(
            summary = "Transfer a Vault's ownership",
            description = "Transfers ownership of a Vault to another user or orphans it."
    )
    @PostMapping(value = "/vaults/{vaultId}/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> transferVault(@RequestHeader(HEADER_USER_ID) String userId,
                                              @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                              @PathVariable String vaultId,
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

        if (transfer.isOrphaning()) {
            vaultsService.orphanVault(vault);

            OrphanVault orphanVaultEvent = new OrphanVault(vault, userId);
            orphanVaultEvent.setVault(vault);
            orphanVaultEvent.setUser(usersService.getUser(userId));
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
            sendEmails(EmailTemplate.TRANSFER_VAULT_OWNERSHIP, vault, userId, previousUserID, transfer.getUserId());

            TransferVaultOwnership transferEvent = new TransferVaultOwnership(transfer, vault, userId);
            transferEvent.setVault(vault);
            transferEvent.setUser(usersService.getUser(userId));
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

            CreateRoleAssignment roleAssignmentEvent = new CreateRoleAssignment(assignment, userId);
            roleAssignmentEvent.setVault(vaultsService.getVault(assignment.getVaultId()));
            roleAssignmentEvent.setUser(usersService.getUser(userId));
            roleAssignmentEvent.setAgentType(Agent.AgentType.BROKER);
            roleAssignmentEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
            roleAssignmentEvent.setAssignee(usersService.getUser(assignment.getUserId()));
            roleAssignmentEvent.setRole(assignment.getRole());

            eventService.addEvent(roleAssignmentEvent);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Search all Vaults",
            description = "Searches for Vaults based on a query string and other parameters."
    )
    @GetMapping(value = "/vaults/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultsData searchAllVaults( @RequestHeader(HEADER_USER_ID) String userId,
                                      @RequestParam String query,
                                      @RequestParam(value = "sort", required = false) String sort,
                                      @RequestParam(value = "order", required = false) String order,
                                      @RequestParam(value = "offset", required = false) String offset,
                                      @RequestParam(value = "maxResult", required = false) String maxResult) {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        int recordsTotal = 0;
        int recordsFiltered = 0;
        List<Vault> vaults = vaultsService.search(userId, query, sort, order, offset, maxResult);
        if(CollectionUtils.isNotEmpty(vaults)) {
            for (Vault vault : vaults) {
                vaultResponses.add(vault.convertToResponse());
            }
            Map<String, Long> projectSizeMap = vaultsService.getAllProjectsSize();
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
            recordsTotal = vaultsService.getTotalNumberOfVaults(userId);
            recordsFiltered = vaultsService.getTotalNumberOfVaults(userId, query);
        }

        VaultsData data = new VaultsData();
        data.setRecordsTotal(recordsTotal);
        data.setRecordsFiltered(recordsFiltered);
        data.setData(vaultResponses);
        return data;
    }

    @Operation(
            summary = "Search all Pending Vaults",
            description = "Searches for Pending Vaults based on a query string and other parameters."
    )
    @GetMapping(value = "/pendingVaults/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultsData searchAllPendingVaults(@RequestHeader(HEADER_USER_ID) String userId,
                                             @RequestParam String query,
                                             @RequestParam(value = "sort", required = false) String sort,
                                             @RequestParam(value = "order", required = false) String order,
                                             @RequestParam(value = "offset", required = false) String offset,
                                             @RequestParam(value = "confirmed", required = false) String confirmed,
                                             @RequestParam(value = "maxResult", required = false) String maxResult) {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        int recordsTotal = 0;
        int recordsFiltered = 0;
        List<PendingVault> vaults = pendingVaultsService.search(userId, query, sort, order, offset, maxResult, confirmed);
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

            recordsTotal = pendingVaultsService.getTotalNumberOfPendingVaults(userId, confirmed);
            recordsFiltered = pendingVaultsService.getTotalNumberOfPendingVaults(userId, query, confirmed);
        }

        VaultsData data = new VaultsData();
        data.setRecordsTotal(recordsTotal);
        data.setRecordsFiltered(recordsFiltered);
        data.setData(vaultResponses);
        return data;
    }

    @Operation(
            summary = "Search all Deposits",
            description = "Searches for Deposits based on a query string and other parameters."
    )
    @GetMapping(value = "/vaults/deposits/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DepositInfo> searchAllDeposits( @RequestHeader(HEADER_USER_ID) String userId,
                                               @RequestParam(value = "query", required = false, defaultValue = "") String query,
                                               @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                                               @RequestParam(value = "order", required = false, defaultValue = "desc") String order) {

        List<DepositInfo> depositResponses = new ArrayList<>();
        for (Deposit deposit : depositsService.search(query, sort, order, userId)) {
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


    @Operation(
            summary = "Search all Deposits with data",
            description = "Searches for Deposits with data based on a query string and other parameters."
    )
    @GetMapping(value = "/vaults/deposits/data/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public DepositsData searchAllDepositsData( @RequestHeader(HEADER_USER_ID) String userId,
                                              @RequestParam(value = "query", required = false, defaultValue = "") String query,
                                              @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                                              @RequestParam(value = "order", required = false, defaultValue = "desc") String order) {


        List<DepositInfo> depositResponses = new ArrayList<>();

        List<Deposit> deposits = depositsService.search(query, sort, order, userId);
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
                if(vault.getDataset() != null) {
                	depositInfo.setDatasetID(vault.getDataset().getID());
                }
                if(vault.getGroup() != null) {
                	depositInfo.setGroupName(vault.getGroup().getName());
                	depositInfo.setGroupID(vault.getGroup().getID());
                }
                if(vault.getReviewDate() != null) {
                	depositInfo.setVaultReviewDate(vault.getReviewDate().toString());
                }
                depositResponses.add(depositInfo);
            }
        }

        DepositsData data = new DepositsData();
        // data.setRecordsTotal(recordsTotal);
        // data.setRecordsFiltered(recordsFiltered);
        data.setData(depositResponses);
        return data;

    }

    @Operation(
            summary = "Limited search for Deposits with data",
            description = "Searches for Deposits with data based on a query string and other parameters, with a limit on the number of results."
    )
    @GetMapping(value = "/vaults/deposits/data/limited/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public DepositsData limitedSearchDepositsData( @RequestHeader(HEADER_USER_ID) String userId,
                                                     @RequestParam(value = "query", required = false, defaultValue = "") String query,
                                                     @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                                                     @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                                                     @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                                                     @RequestParam(value = "maxResult", required = false, defaultValue = "300") int maxResult) {


        List<DepositInfo> depositResponses = new ArrayList<>();

        List<Deposit> deposits = depositsService.getDeposits( query, userId, sort, order, offset, maxResult);
        
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
                if(vault.getDataset() != null) {
                	depositInfo.setDatasetID(vault.getDataset().getID());
                }
                if(vault.getGroup() != null) {
                	depositInfo.setGroupName(vault.getGroup().getName());
                	depositInfo.setGroupID(vault.getGroup().getID());
                }
                if(vault.getReviewDate() != null) {
                	depositInfo.setVaultReviewDate(vault.getReviewDate().toString());
                }
                depositResponses.add(depositInfo);
            }
        }

        DepositsData data = new DepositsData();
        data.setData(depositResponses);
        return data;

    }

    @Operation(
            summary = "Update a Pending Vault",
            description = "Updates an existing Pending Vault."
    )
    @PostMapping(value = "/pendingVaults/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultInfo updatePendingVault(@RequestHeader(HEADER_USER_ID) String userId,
                                        @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                        @RequestBody CreateVault createVault) throws Exception {
        PendingVault vault = pendingVaultsService.getPendingVault(createVault.getPendingID());
        vault = pendingVaultsService.processVaultParams(vault, createVault, userId);

        pendingVaultsService.addOrUpdatePendingVault(vault);

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
        pendingVaultsService.addOwnerRole(createVault, vault.getId(), userId);
        vault = pendingVaultsService.processDataCreatorParams(createVault, vault);
        pendingVaultsService.addNDMRoles(createVault, vault.getId());
        pendingVaultsService.addCreator(createVault, userId, vault.getId());

        if (createVault.getConfirmed()) {
            logger.info("Calling sendNewPendingVaultEmail().");
            pendingVaultsService.sendNewPendingVaultEmail(vault);
            logger.info("Called sendNewPendingVaultEmail().");
        }

        return vault.convertToResponse();
    }

    @Operation(
            summary = "Edit a Pending Vault (Admin)",
            description = "Updates an existing Pending Vault as an administrator."
    )
    @PostMapping(value = "/admin/pendingVaults/edit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultInfo editPendingVault(@RequestHeader(HEADER_USER_ID) String userId,
                                      @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                      @RequestBody CreateVault createVault) throws Exception {
        PendingVault vault = pendingVaultsService.getPendingVault(createVault.getPendingID());

        User owner = vault.getOwner();
        logger.info("owner: " + owner);
        if(owner != null) {
        	logger.info("owner id:" + owner.getID());
        }
       
        createVault.setAffirmed(true);
        createVault.setPureLink(true);
        
        vault = pendingVaultsService.processVaultParams(vault, createVault, userId);
        
        pendingVaultsService.addOrUpdatePendingVault(vault);
        
        List<RoleAssignment> previousRoles = permissionsService.getRoleAssignmentsForPendingVault(vault.getId());
        if (previousRoles != null && !previousRoles.isEmpty()) {
            for (RoleAssignment pr : previousRoles) {
            	if(pr.getRole().getName().equals(ROLE_DATA_OWNER)) {
            		permissionsService.deleteRoleAssignment(pr.getId());
            	}
            }
        }
        
        if(owner != null) {
        	pendingVaultsService.addOwnerRole(createVault, vault.getId(), owner.getID());
        } else {
        	pendingVaultsService.addOwnerRole(createVault, vault.getId(), null);
        }
        
        List<PendingDataCreator> previousCreators = vault.getDataCreators();
        if (previousCreators != null && ! previousCreators.isEmpty()) {
            for (PendingDataCreator pdc : previousCreators) {
                pendingDataCreatorsService.deletePendingDataCreator(pdc.getId());
            }
        }
        
        logger.info("pendingVaultsService.processDataCreatorParams");
        logger.info("createVault.getDataCreatorsAsString(): " + createVault.getDataCreatorsAsString());
        vault = pendingVaultsService.processDataCreatorParams(createVault, vault);

        previousRoles = permissionsService.getRoleAssignmentsForPendingVault(vault.getId());
        if (previousRoles != null && !previousRoles.isEmpty()) {
            for (RoleAssignment pr : previousRoles) {
            	if(pr.getRole().getName().equals(ROLE_DEPOSITOR)) {
            		permissionsService.deleteRoleAssignment(pr.getId());
            	}
            }
        }
        
        logger.info("pendingVaultsService.addDepositorRoles");
        logger.info("createVault.getDepositorsAsString(): " + createVault.getDepositorsAsString());
        pendingVaultsService.addDepositorRoles(createVault, vault.getId());
         
        previousRoles = permissionsService.getRoleAssignmentsForPendingVault(vault.getId());
        if (previousRoles != null && !previousRoles.isEmpty()) {
            for (RoleAssignment pr : previousRoles) {
            	if(pr.getRole().getName().equals(ROLE_NDM)) {
            		permissionsService.deleteRoleAssignment(pr.getId());
            	}
            }
        }
        
        logger.info("pendingVaultsService.addNDMRoles");
        logger.info("createVault.getNDMsAsString(): " + createVault.getNDMsAsString());
        pendingVaultsService.addNDMRoles(createVault, vault.getId());

        return vault.convertToResponse();
    }

    @Operation(
            summary = "Add a new Pending Vault",
            description = "Adds a new Pending Vault to the system."
    )
    @PostMapping(value = "/pendingVaults", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultInfo addPendingVault(@RequestHeader(HEADER_USER_ID) String userId,
                                     @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                     @RequestBody CreateVault createVault) throws Exception {
        PendingVault vault = new PendingVault();
        vault = pendingVaultsService.processVaultParams(vault, createVault, userId);

        pendingVaultsService.addOrUpdatePendingVault(vault);

        pendingVaultsService.addDepositorRoles(createVault, vault.getId());
        pendingVaultsService.addOwnerRole(createVault, vault.getId(), userId);
        vault = pendingVaultsService.processDataCreatorParams(createVault, vault);
        pendingVaultsService.addNDMRoles(createVault, vault.getId());
        pendingVaultsService.addCreator(createVault, userId, vault.getId());

        if (createVault.getConfirmed()) {
            logger.info("Calling sendNewPendingVaultEmail().");
            pendingVaultsService.sendNewPendingVaultEmail(vault);
            logger.info("Called sendNewPendingVaultEmail().");
        }

        return vault.convertToResponse();
    }

    @Operation(
            summary = "Add a new Vault",
            description = "Adds a new Vault to the system."
    )
    @PostMapping(value = "/vaults", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultInfo addVault(@RequestHeader(HEADER_USER_ID) String userId,
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

        User user = usersService.getUser(userId);
        if (user == null) {
            logger.error("User '" + userId + "' does not exist");
            throw new Exception("User '" + userId + "' does not exist");
        }
        vault.setUser(user);

        try {
            vault.setGrantEndDate(createVault.getGrantEndDate());
        } catch (NullPointerException ex) {
            logger.error("Grant date is not in the right format: "+createVault.getGrantEndDate());
            vault.setGrantEndDate(null);
        }

        try {
            vault.setReviewDate(createVault.getReviewDate());
        } catch (NullPointerException ex) {
            logger.error("Review date is not in the right format: "+createVault.getReviewDate());
            vault.setReviewDate(null);
        }


        vaultsService.addVault(vault);
        vaultsService.addVaultEvent(vault, clientKey, userId);
        vaultsService.addOwnerRole(createVault, vault, clientKey);
        vaultsService.sendVaultOwnerEmail(vault, homePage, helpPage, user);
        vaultsService.addDepositorRoles(createVault, vault, clientKey, homePage, helpPage);
        vault = vaultsService.processDataCreatorParams(createVault, vault);
        vaultsService.addNDMRoles(createVault, vault, clientKey, homePage, helpPage);
        vaultsService.addBillingInfo(createVault, vault);

        try {
            vaultsService.checkRetentionPolicy(vault.getID());
        } catch (Exception e) {
            logger.error("Fail to check retention policy: ",e);
            throw e;
        }


        return vault.convertToResponse();
    }

    @Operation(
            summary = "Get a specific Vault",
            description = "Retrieves details for a specific Vault by its ID."
    )
    @GetMapping(value = "/vaults/{vaultId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultInfo getVault( @RequestHeader(HEADER_USER_ID) String userId,
                              @PathVariable String vaultId) throws Exception {

        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);
        if (vault != null) {
            logger.debug("getVault: " + vault);
            return vault.convertToResponse();
        } else {
            return null;
        }
    }

    @Operation(
            summary = "Get a specific Pending Vault",
            description = "Retrieves details for a specific Pending Vault by its ID."
    )
    @GetMapping(value = "/pendingVaults/{vaultId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultInfo getPendingVault( @RequestHeader(HEADER_USER_ID) String userId,
                                     @PathVariable String vaultId) throws Exception {

        User user = usersService.getUser(userId);
        PendingVault vault = pendingVaultsService.getUserPendingVault(user, vaultId);
        User owner = permissionsService.getPendingVaultOwner(vaultId);
        List<User> ndms = permissionsService.getPendingVaultNDMs(vaultId);
        List<User> deps = permissionsService.getPendingVaultDepositors(vaultId);
        User creator = permissionsService.getPendingVaultCreator(vaultId);
        
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
    

    @Operation(
            summary = "Check a Vault's retention policy",
            description = "Checks the retention policy of a specific Vault.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = true, description = "DataVault Broker User ID", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.PATH, name = "vaultId", required = true, description = "The ID of the Vault to check", schema = @Schema(type = "string"))
            }
    )
    @GetMapping(value = "/vaults/{vaultId}/checkretentionpolicy", produces = MediaType.APPLICATION_JSON_VALUE)
    public Vault checkVaultRetentionPolicy( @RequestHeader(HEADER_USER_ID) String userId,
                                           @PathVariable String vaultId) {

        return vaultsService.checkRetentionPolicy(vaultId);
    }

    @Operation(
            summary = "Get a Vault's record",
            description = "Retrieves the full record for a specific Vault."
    )
    @GetMapping(value = "/vaults/{vaultId}/record", produces = MediaType.APPLICATION_JSON_VALUE)
    public Vault getVaultRecord( @RequestHeader(HEADER_USER_ID) String userId,
                                @PathVariable String vaultId) {

        return vaultsService.getVault(vaultId);
    }
    
    @Operation(
            summary = "Get a Pending Vault's record",
            description = "Retrieves the full record for a specific Pending Vault."
    )
    @GetMapping(value = "/pendingVaults/{vaultId}/record", produces = MediaType.APPLICATION_JSON_VALUE)
    public PendingVault getPendingVaultRecord( @RequestHeader(HEADER_USER_ID) String userId,
                                              @PathVariable String vaultId) {

        return pendingVaultsService.getPendingVault(vaultId);
    }

    @Operation(
            summary = "Get a Vault's Deposits",
            description = "Retrieves a list of all Deposits for a specific Vault."
    )
    @GetMapping(value = "/vaults/{vaultId}/deposits", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DepositInfo> getDeposits( @RequestHeader(HEADER_USER_ID) String userId,
                                         @PathVariable String vaultId) throws Exception {

        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);

        List<DepositInfo> depositResponses = new ArrayList<>();
        for (Deposit deposit : vault.getDeposits()) {
            depositResponses.add(deposit.convertToResponse());
        }
        return depositResponses;
    }

    @Operation(
            summary = "Get a Vault's Role Events",
            description = "Retrieves a list of all Role Events for a specific Vault."
    )
    @GetMapping(value = "/vaults/{vaultId}/roleEvents", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EventInfo> getRoleEvents( @RequestHeader(HEADER_USER_ID) String userId,
                                         @PathVariable String vaultId) throws Exception {

        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);

        List<EventInfo> events = new ArrayList<>();
        for (Event event : eventService.findVaultEvents(vault)) {
            events.add(event.convertToResponse());
        }

        return events;
    }

    @Operation(
            summary = "Add a Data Manager to a Vault",
            description = "Adds a Data Manager to a specific Vault.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The user uun",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", description = "the user uun")
                    )
            )
    )
    @PostMapping(value = "/vaults/{vaultId}/addDataManager", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultInfo addDataManager(@RequestHeader(HEADER_USER_ID) String userId,
                                    @PathVariable String vaultId,
                                    @RequestBody String unn) throws Exception {
        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);

        DataManager dataManager = new DataManager(unn);
        dataManager.setVault(vault);
        dataManagersService.addDataManager(dataManager);

        return vault.convertToResponse();
    }

    @Operation(
            summary = "Get a Vault's Data Managers",
            description = "Retrieves a list of all Data Managers for a specific Vault."
    )
    @GetMapping(value = "/vaults/{vaultId}/dataManagers", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DataManager> getDataManagers( @RequestHeader(HEADER_USER_ID) String userId,
                                             @PathVariable String vaultId) throws Exception {
        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);
        List<DataManager> dataManagersList = dataManagersService.findByVaultId(vault.getID());
        return dataManagersList;
    }

    @Operation(
            summary = "Get a specific Data Manager",
            description = "Retrieves a specific Data Manager by their UUN."
    )
    @GetMapping(value = "/vaults/{vaultId}/dataManager/{uun}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DataManager getDataManager( @RequestHeader(HEADER_USER_ID) String userId,
                                      @PathVariable String vaultId,
                                      @PathVariable String uun) throws Exception {
        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);

        return vault.getDataManager(uun);
    }

    @Operation(
            summary = "Delete a Data Manager",
            description = "Deletes a Data Manager from a Vault."
    )
    @DeleteMapping(value = "/vaults/{vaultId}/deleteDataManager/{dataManagerID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultInfo deleteDataManager( @RequestHeader(HEADER_USER_ID) String userId,
                                       @PathVariable String vaultId,
                                       @PathVariable String dataManagerID) throws Exception {
        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);

        dataManagersService.deleteDataManager(dataManagerID);

        return vault.convertToResponse();
    }

    @Operation(
            summary = "Update a Vault's description",
            description = "Updates the description of a specific Vault.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The new vault description",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", description = "the new vault description")
                    )
            )
    )
    @PostMapping(value = "/vaults/{vaultId}/updateVaultDescription", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultInfo updateVaultDescription(@RequestHeader(HEADER_USER_ID) String userId,
                                            @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                            @PathVariable String vaultId,
                                            @RequestBody String description) throws Exception {
        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);
        String oldDesc = vault.getDescription();
        vault.setDescription(description);
        vaultsService.updateVault(vault);

        UpdatedDescription descEvent = new UpdatedDescription(oldDesc, description);
        descEvent.setVault(vault);
        descEvent.setUser(usersService.getUser(userId));
        descEvent.setAgentType(Agent.AgentType.BROKER);
        descEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());

        eventService.addEvent(descEvent);

        return vault.convertToResponse();
    }

    @Operation(
            summary = "Update a Vault's name",
            description = "Updates the name of a specific Vault.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The new vault name",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", description = "the new vault name")
                    )
            )
    )
    @PostMapping(value = "/vaults/{vaultId}/updateVaultName",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultInfo updateVaultName(@RequestHeader(HEADER_USER_ID) String userId,
                                     @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                     @PathVariable String vaultId,
                                     @RequestBody String name) throws Exception {
        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);
        String oldName = vault.getName();
        vault.setName(name);
        vaultsService.updateVault(vault);

        UpdatedName nameEvent = new UpdatedName(oldName, name);
        nameEvent.setVault(vault);
        nameEvent.setUser(usersService.getUser(userId));
        nameEvent.setAgentType(Agent.AgentType.BROKER);
        nameEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());

        eventService.addEvent(nameEvent);

        return vault.convertToResponse();
    }

    @Operation(
            summary = "Update a Vault's review date",
            description = "Updates the review date of a specific Vault.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Review date in ISO 8601 format",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    format = "date",
                                    examples = "2026-12-31"
                            )
                    )
            )
    )
    @PostMapping(value = "/vaults/{vaultId}/updatereviewdate", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultInfo updateVaultReviewDate(@RequestHeader(HEADER_USER_ID) String userId,
                                           @PathVariable String vaultId,
                                           @RequestBody String reviewDate) throws Exception {
        User user = usersService.getUser(userId);
        Vault vault = vaultsService.getUserVault(user, vaultId);

        vault.setReviewDate(DateTimeUtils.parseLocalDate(reviewDate));

        logger.info("Updating Review Date for Vault Id {}", vaultId);
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
        model.put(EMAIL_HOMEPAGE, this.homePage);
        model.put(EMAIL_HELPPAGE, this.helpPage);
        model.put(EMAIL_VAULT, vault.getName());
        model.put(EMAIL_ASSIGNEE, user.getFirstname() + " " + user.getLastname());
        if (previousOwner != null) {
            model.put(EMAIL_PREVIOUS_OWNER, previousOwner.getFirstname() + " " + previousOwner.getLastname());
        } else {
            model.put(EMAIL_PREVIOUS_OWNER, VaultsController.ORPHANED_ID);
        }

        if(newOwner != null) {
            model.put(EMAIL_NEW_OWNER, newOwner.getFirstname() + " " + newOwner.getLastname());

            emailService.sendTemplateMailToUser(newOwner, "Datavault - Role Assignment", template, model);
        }
    }
}
