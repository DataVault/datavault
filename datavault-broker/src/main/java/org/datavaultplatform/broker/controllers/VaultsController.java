package org.datavaultplatform.broker.controllers;

import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.vault.Create;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@RestController
@Api(name="Vaults", description = "Interact with DataVault Vaults")
public class VaultsController {

    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RetrievesService retrievesService;
    private ExternalMetadataService externalMetadataService;
    private RetentionPoliciesService retentionPoliciesService;
    private GroupsService groupsService;
    private UsersService usersService;
    private FileStoreService fileStoreService;
    private ArchiveStoreService archiveStoreService;
    private EventService eventService;
    private ClientsService clientsService;
    private DataManagersService dataManagersService;
    
    private String activeDir;
    private String archiveDir;

    private static final Logger logger = LoggerFactory.getLogger(VaultsController.class);
    
    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }

    public void setRetrievesService(RetrievesService retrievesService) {
        this.retrievesService = retrievesService;
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

    public void setFileStoreService(FileStoreService fileStoreService) {
        this.fileStoreService = fileStoreService;
    }

    public void setArchiveStoreService(ArchiveStoreService archiveStoreService) {
        this.archiveStoreService = archiveStoreService;
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

        List<VaultInfo> vaultResponses = new ArrayList<>();
        User user = usersService.getUser(userID);
        for (Vault vault : user.getVaults()) {
            vaultResponses.add(vault.convertToResponse());
        }
        vaultResponses.sort(Comparator.comparing(VaultInfo::getCreationTime));
        Collections.reverse(vaultResponses);
        return vaultResponses;
    }



    @RequestMapping(value = "/vaults/search", method = RequestMethod.GET)
    public List<VaultInfo> searchAllVaults(@RequestHeader(value = "X-UserID", required = true) String userID,
                                                  @RequestParam String query,
                                                  @RequestParam(value = "sort", required = false) String sort,
                                                  @RequestParam(value = "order", required = false)
                                                  @ApiQueryParam(name = "order", description = "Vault sort order", allowedvalues = {"asc", "dec"}, defaultvalue = "asc", required = false) String order) throws Exception {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        for (Vault vault : vaultsService.search(query, sort, order)) {
            vaultResponses.add(vault.convertToResponse());
        }
        return vaultResponses;
    }

    @RequestMapping(value = "/vaults/deposits/search", method = RequestMethod.GET)
    public List<DepositInfo> searchAllDeposits(@RequestHeader(value = "X-UserID", required = true) String userID,
                                           @RequestParam("query") String query,
                                           @RequestParam(value = "sort", required = false) String sort) throws Exception {

        List<DepositInfo> depositResponses = new ArrayList<>();
        for (Deposit deposit : depositsService.search(query, sort)) {
            depositResponses.add(deposit.convertToResponse());
        }
        return depositResponses;
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
        
        Dataset dataset = externalMetadataService.getCachedDataset(createVault.getDatasetID());
        if (dataset == null) {
            dataset = externalMetadataService.getDataset(createVault.getDatasetID());
            if (dataset == null) {
                logger.error("Dataset metadata record '" + createVault.getDatasetID() + "' does not exist");
                throw new Exception("Dataset metadata record '" + createVault.getDatasetID() + "' does not exist");
            }
            
            externalMetadataService.addCachedDataset(dataset);
        }
        vault.setDataset(dataset);

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

        List<DataManager> dataManagersList = new ArrayList<>();
        for (DataManager dataManager : vault.getDataManagers()) {
            dataManagersList.add(dataManager);
        }
        return dataManagersList;
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
}
