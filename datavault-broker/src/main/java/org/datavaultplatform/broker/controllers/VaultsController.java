package org.datavaultplatform.broker.controllers;

import org.apache.commons.collections.CollectionUtils;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.roles.CreateRoleAssignment;
import org.datavaultplatform.common.event.roles.OrphanVault;
import org.datavaultplatform.common.event.roles.TransferVaultOwnership;
import org.datavaultplatform.common.event.vault.Create;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.request.TransferVault;
import org.datavaultplatform.common.response.*;
import org.datavaultplatform.common.util.RoleUtils;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private EmailService emailService;
    private VaultsService vaultsService;
    private DepositsService depositsService;
    private ExternalMetadataService externalMetadataService;
    private RetentionPoliciesService retentionPoliciesService;
    private GroupsService groupsService;
    private UsersService usersService;
    private EventService eventService;
    private ClientsService clientsService;
    private DataManagersService dataManagersService;
    private RolesAndPermissionsService permissionsService;
    private String activeDir;
    private String archiveDir;

    private String homePage;
    private String helpPage;

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }
    public void setHelpPage(String helpPage) {
        this.helpPage = helpPage;
    }

    private static final Logger logger = LoggerFactory.getLogger(VaultsController.class);

    public void setPermissionsService(RolesAndPermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    public void setEmailService(EmailService emailService) { this.emailService = emailService; }

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }

    public void setExternalMetadataService(ExternalMetadataService externalMetadataService) {
        this.externalMetadataService = externalMetadataService;
    }

    public void setRetentionPoliciesService(RetentionPoliciesService retentionPoliciesService) {
        this.retentionPoliciesService = retentionPoliciesService;
    }

    public void setGroupsService(GroupsService groupsService) {
        this.groupsService = groupsService;
    }

    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setClientsService(ClientsService clientsService) {
        this.clientsService = clientsService;
    }

    public void setDataManagersService(DataManagersService dataManagersService) {
        this.dataManagersService = dataManagersService;
    }

    // NOTE: this a placeholder and will eventually be handled by per-user config
    public void setActiveDir(String activeDir) {
        this.activeDir = activeDir;
    }

    // NOTE: this a placeholder and will eventually be handled by system config
    public void setArchiveDir(String archiveDir) {
        this.archiveDir = archiveDir;
    }

    @ApiMethod(
            path = "/vaults",
            verb = ApiVerb.GET,
            description = "Gets a list of all Vaults for the specified User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/vaults", method = RequestMethod.GET)
    public List<VaultInfo> getVaults(@RequestHeader(value = "X-UserID", required = true) String userID) {

        List<VaultInfo> vaultResponses = permissionsService.getRoleAssignmentsForUser(userID).stream()
                .filter(roleAssignment -> RoleType.VAULT == roleAssignment.getRole().getType() || RoleUtils.isDataOwner(roleAssignment))
                .map(roleAssignment -> vaultsService.getVault(roleAssignment.getVaultId()).convertToResponse())
                .sorted(Comparator.comparing(VaultInfo::getCreationTime))
                .collect(Collectors.toList());
        Collections.reverse(vaultResponses);
        return vaultResponses;
    }

    @RequestMapping(value = "/vaults/user", method = RequestMethod.GET)
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


    @RequestMapping(value = "/vaults/{vaultId}/transfer", method = RequestMethod.POST)
    public ResponseEntity transferVault(@RequestHeader(value = "X-UserID", required = true) String userID,
                                        @RequestHeader(value = "X-Client-Key", required = true) String clientKey,
                                        @PathVariable("vaultId") String vaultId,
                                        @RequestBody TransferVault transfer) throws Exception {

        Vault vault = vaultsService.getVault(vaultId);
        User currentOwner = vault.getUser();


        if (transfer.isOrphaning()) {
            vaultsService.orphanVault(vault);

            OrphanVault orphanVaultEvent = new OrphanVault(vault, userID);
            orphanVaultEvent.setVault(vault);
            orphanVaultEvent.setUser(usersService.getUser(userID));
            orphanVaultEvent.setAgentType(Agent.AgentType.BROKER);
            orphanVaultEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());

            eventService.addEvent(orphanVaultEvent);
        } else {
            vaultsService.transferVault(vault, usersService.getUser(transfer.getUserId()), transfer.getReason());

            sendEmails("transfer-vault-ownership.vm", vault, userID, transfer.getUserId());

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

            sendEmails("transfer-vault-ownership.vm", vault, userID, transfer.getUserId());

            CreateRoleAssignment roleAssignmentEvent = new CreateRoleAssignment(assignment, userID);
            roleAssignmentEvent.setVault(vaultsService.getVault(assignment.getVaultId()));
            roleAssignmentEvent.setUser(usersService.getUser(userID));
            roleAssignmentEvent.setAgentType(Agent.AgentType.BROKER);
            roleAssignmentEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
            roleAssignmentEvent.setAssignee(usersService.getUser(assignment.getUserId()));
            roleAssignmentEvent.setRole(assignment.getRole());;

            eventService.addEvent(roleAssignmentEvent);
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/vaults/search", method = RequestMethod.GET)
    public VaultsData searchAllVaults(@RequestHeader(value = "X-UserID", required = true) String userID,
                                      @RequestParam String query,
                                      @RequestParam(value = "sort", required = false) String sort,
                                      @RequestParam(value = "order", required = false)
                                      @ApiQueryParam(name = "order", description = "Vault sort order", allowedvalues = {"asc", "dec"}, defaultvalue = "asc", required = false) String order,
                                      @RequestParam(value = "offset", required = false)
                                      @ApiQueryParam(name = "offset", description = "Vault row id ", defaultvalue = "0", required = false) String offset,
                                      @RequestParam(value = "maxResult", required = false)
                                      @ApiQueryParam(name = "maxResult", description = "Number of records", required = false) String maxResult) {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        Long recordsTotal = 0L;
        Long recordsFiltered = 0L;
        List<Vault> vaults = vaultsService.search(userID, query, sort, order, offset, maxResult);
        if(CollectionUtils.isNotEmpty(vaults)) {
            for (Vault vault : vaults) {
                vaultResponses.add(vault.convertToResponse());
            }
            //Map of project with its size
            Map<String, Long> projectSizeMap = vaultsService.getAllProjectsSize();
            //update project Size in the response
            for(VaultInfo vault: vaultResponses) {
                if(vault.getProjectId() != null) {
                    vault.setProjectSize(projectSizeMap.get(vault.getProjectId()));
                }
            }
            recordsTotal = vaultsService.getTotalNumberOfVaults(userID);
            recordsFiltered = vaultsService.getTotalNumberOfVaults(query);
        }

        VaultsData data = new VaultsData();
        data.setRecordsTotal(recordsTotal);
        data.setRecordsFiltered(recordsFiltered);
        data.setData(vaultResponses);
        return data;
    }



    @RequestMapping(value = "/vaults/deposits/search", method = RequestMethod.GET)
    public List<DepositInfo> searchAllDeposits(@RequestHeader(value = "X-UserID", required = true) String userID,
                                               @RequestParam("query") String query,
                                               @RequestParam(value = "sort", required = false) String sort) {

        List<DepositInfo> depositResponses = new ArrayList<>();
        for (Deposit deposit : depositsService.search(query, sort, userID)) {
            //deposit.convertToResponse();
            DepositInfo depositInfo = deposit.convertToResponse();
            User depositor = usersService.getUser(depositInfo.getUserID());
            depositInfo.setUserName(depositor.getFirstname() + " " + depositor.getLastname());
            Vault vault = vaultsService.getVault(depositInfo.getVaultID());
            depositInfo.setVaultName(vault.getName());
            User vaultOwner = vault.getUser();
            depositInfo.setVaultOwnerID(vaultOwner.getID());
            depositInfo.setVaultOwnerName(vaultOwner.getFirstname() + " " + vaultOwner.getLastname());
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


    @RequestMapping(value = "/vaults/deposits/data/search", method = RequestMethod.GET)
    public DepositsData searchAllDepositsData(@RequestHeader(value = "X-UserID", required = true) String userID,
                                              @RequestParam("query") String query,
                                              @RequestParam(value = "sort", required = false) String sort) throws Exception {


        List<DepositInfo> depositResponses = new ArrayList<>();

        List<Deposit> deposits = depositsService.search(query, sort, userID);
        if(CollectionUtils.isNotEmpty(deposits)) {
            for (Deposit deposit : deposits) {
                DepositInfo depositInfo = deposit.convertToResponse();
                User depositor = usersService.getUser(depositInfo.getUserID());
                depositInfo.setUserName(depositor.getFirstname() + " " + depositor.getLastname());
                Vault vault = vaultsService.getVault(depositInfo.getVaultID());
                depositInfo.setVaultName(vault.getName());
                User vaultOwner = vault.getUser();
                depositInfo.setVaultOwnerID(vaultOwner.getID());
                depositInfo.setVaultOwnerName(vaultOwner.getFirstname() + " " + vaultOwner.getLastname());
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



    @RequestMapping(value = "/vaults", method = RequestMethod.POST)
    public VaultInfo addVault(@RequestHeader(value = "X-UserID", required = true) String userID,
                              @RequestHeader(value = "X-Client-Key", required = true) String clientKey,
                              @RequestBody CreateVault createVault) throws Exception {

        Vault vault = new Vault();
        vault.setName(createVault.getName());
        vault.setDescription(createVault.getDescription());

        RetentionPolicy retentionPolicy = retentionPoliciesService.getPolicy(createVault.getPolicyID());
        if (retentionPolicy == null) {
            logger.error("RetentionPolicy '" + createVault.getPolicyID() + "' does not exist");
            throw new Exception("RetentionPolicy '" + createVault.getPolicyID() + "' does not exist");
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
        String datasetId = createVault.getDatasetID();
        Dataset dataset = externalMetadataService.getCachedDataset(datasetId);

        if (dataset == null) {
            dataset = externalMetadataService.getDataset(datasetId);
            if (dataset == null) {
                logger.error("Dataset metadata record '" + datasetId + "' does not exist");
                throw new Exception("Dataset metadata record '" + datasetId + "' does not exist");
            }

            externalMetadataService.addCachedDataset(dataset);
        }
        vault.setDataset(dataset);
        vault.setSnapshot(externalMetadataService.getDatasetContent(datasetId));
        vault.setProjectId(externalMetadataService.getPureProjectId(dataset.getID()));

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

        RoleAssignment dataOwnerRoleAssignment = new RoleAssignment();
        dataOwnerRoleAssignment.setUserId(userID);
        dataOwnerRoleAssignment.setVaultId(vault.getID());
        dataOwnerRoleAssignment.setRole(permissionsService.getDataOwner());
        permissionsService.createRoleAssignment(dataOwnerRoleAssignment);

        Create vaultEvent = new Create(vault.getID());
        vaultEvent.setVault(vault);
        vaultEvent.setUser(usersService.getUser(userID));
        vaultEvent.setAgentType(Agent.AgentType.BROKER);
        vaultEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());

        eventService.addEvent(vaultEvent);

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

    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public VaultInfo getVault(@RequestHeader(value = "X-UserID", required = true) String userID,
                              @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);
        if (vault != null) {
            return vault.convertToResponse();
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/vaults/{vaultid}/checkretentionpolicy", method = RequestMethod.GET)
    public Vault checkVaultRetentionPolicy(@RequestHeader(value = "X-UserID", required = true) String userID,
                                           @PathVariable("vaultid") String vaultID) throws Exception {

        return vaultsService.checkRetentionPolicy(vaultID);
    }

    @RequestMapping(value = "/vaults/{vaultid}/record", method = RequestMethod.GET)
    public Vault getVaultRecord(@RequestHeader(value = "X-UserID", required = true) String userID,
                                @PathVariable("vaultid") String vaultID) {

        return vaultsService.getVault(vaultID);
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits", method = RequestMethod.GET)
    public List<DepositInfo> getDeposits(@RequestHeader(value = "X-UserID", required = true) String userID,
                                         @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        List<DepositInfo> depositResponses = new ArrayList<>();
        for (Deposit deposit : vault.getDeposits()) {
            depositResponses.add(deposit.convertToResponse());
        }
        return depositResponses;
    }

    @RequestMapping(value = "/vaults/{vaultid}/roleEvents", method = RequestMethod.GET)
    public List<EventInfo> getRoleEvents(@RequestHeader(value = "X-UserID", required = true) String userID,
                                         @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        List<EventInfo> events = new ArrayList<>();
        for (Event event : eventService.findVaultEvents(vault)) {
            events.add(event.convertToResponse());
        }

        return events;
    }

    @RequestMapping(value = "/vaults/{vaultid}/addDataManager", method = RequestMethod.POST)
    public VaultInfo addDataManager(@RequestHeader(value = "X-UserID", required = true) String userID,
                                    @PathVariable("vaultid") String vaultID,
                                    @RequestBody() String unn) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        DataManager dataManager = new DataManager(unn);
        dataManager.setVault(vault);
        dataManagersService.addDataManager(dataManager);

        return vault.convertToResponse();
    }

    @RequestMapping(value = "/vaults/{vaultid}/dataManagers", method = RequestMethod.GET)
    public List<DataManager> getDataManagers(@RequestHeader(value = "X-UserID", required = true) String userID,
                                             @PathVariable("vaultid") String vaultID) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        List<DataManager> dataManagersList = new ArrayList<>(vault.getDataManagers());
        return dataManagersList;
    }

    @RequestMapping(value = "/vaults/{vaultid}/dataManager/{uun}", method = RequestMethod.GET)
    public DataManager getDataManager(@RequestHeader(value = "X-UserID", required = true) String userID,
                                      @PathVariable("vaultid") String vaultID,
                                      @PathVariable("uun") String uun) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        return vault.getDataManager(uun);
    }

    @RequestMapping(value = "/vaults/{vaultid}/deleteDataManager/{dataManagerID}", method = RequestMethod.DELETE)
    public VaultInfo deleteDataManager(@RequestHeader(value = "X-UserID", required = true) String userID,
                                       @PathVariable("vaultid") String vaultID,
                                       @PathVariable("dataManagerID") String dataManagerID) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        dataManagersService.deleteDataManager(dataManagerID);

        return vault.convertToResponse();
    }

    @RequestMapping(value = "/vaults/{vaultid}/updateVaultDescription", method = RequestMethod.POST)
    public VaultInfo updateVaultDescription(@RequestHeader(value = "X-UserID", required = true) String userID,
                                            @PathVariable("vaultid") String vaultID,
                                            @RequestBody() String description) throws Exception {
        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, vaultID);

        vault.setDescription(description);
        vaultsService.updateVault(vault);

        return vault.convertToResponse();
    }

    private void sendEmails(String template, Vault vault, String userId, String newOwnerId) throws Exception {

        //sendEmails("transfer-vault-ownership.vm", vault, userID, transfer.getUserId());
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("homepage", this.homePage);
        model.put("helppage", this.helpPage);
        User assignee = this.usersService.getUser(userId);
        model.put("assignee", assignee.getFirstname() + " " + assignee.getLastname());
        model.put("vault", vault.getName());
        User vaultOwner = vault.getUser();
        model.put("previousowner", vaultOwner.getFirstname() + " " + vaultOwner.getLastname());
        User newOwner = this.usersService.getUser(newOwnerId);
        if(newOwner != null) {
            model.put("newowner", newOwner.getFirstname() + " " + newOwner.getLastname());
            // Send email to the deposit user
            emailService.sendTemplateMailToUser(newOwner,
                    "Datavault - Role Assignment",
                    template,
                    model);
        }else{
            model.put("newowner", "n/a");
        }
    }
}