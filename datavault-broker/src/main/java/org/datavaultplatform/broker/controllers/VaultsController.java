package org.datavaultplatform.broker.controllers;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.*;
import org.datavaultplatform.common.response.*;

import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@Api(name="Vaults", description = "Interact with DataVault Vaults")
public class VaultsController {

    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RestoresService restoresService;
    private ExternalMetadataService externalMetadataService;
    private PoliciesService policiesService;
    private GroupsService groupsService;
    private UsersService usersService;
    private FileStoreService fileStoreService;
    private ArchiveStoreService archiveStoreService;

    private String activeDir;
    private String archiveDir;

    private static final Logger logger = LoggerFactory.getLogger(VaultsController.class);
    
    // Get the specified Vault object and validate it against the current User
    private Vault getUserVault(User user, String vaultID) throws Exception {

        Vault vault = vaultsService.getVault(vaultID);

        if (vault == null) {
            throw new Exception("Vault '" + vaultID + "' does not exist");
        }

        if (!vault.getUser().equals(user)) {
            throw new Exception("Access denied");
        }

        return vault;
    }

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }

    public void setRestoresService(RestoresService restoresService) {
        this.restoresService = restoresService;
    }
    
    public void setExternalMetadataService(ExternalMetadataService externalMetadataService) {
        this.externalMetadataService = externalMetadataService;
    }

    public void setPoliciesService(PoliciesService policiesService) {
        this.policiesService = policiesService;
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

        // todo : remove this bit of logging. It was put here as an example of how to do logging.
        logger.info("Hello World");

        List<VaultInfo> vaultResponses = new ArrayList<>();
        User user = usersService.getUser(userID);
        for (Vault vault : user.getVaults()) {
            vaultResponses.add(vault.convertToResponse());
        }
        return vaultResponses;
    }

    @ApiMethod(
            path = "/vaults/all",
            verb = ApiVerb.GET,
            description = "Gets a list of all Vaults",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/vaults/all", method = RequestMethod.GET)
    public List<VaultInfo> getVaultsAll(@RequestHeader(value = "X-UserID", required = true) String userID,
                                        @RequestParam(value = "sort", required = false)
                                        @ApiQueryParam(name = "sort", description = "Vault sort field", allowedvalues = {"id", "name", "description", "vaultSize", "user", "policy", "creationTime"}, defaultvalue = "creationTime", required = false) String sort,
                                        @RequestParam(value = "order", required = false)
                                        @ApiQueryParam(name = "order", description = "Vault sort order", allowedvalues = {"asc", "dec"}, defaultvalue = "asc", required = false) String order) throws Exception {

        if (sort == null) sort = "";
        if (order == null) order = "asc";
        
        List<VaultInfo> vaultResponses = new ArrayList<>();
        for (Vault vault : vaultsService.getVaults(sort, order)) {
            vaultResponses.add(vault.convertToResponse());
        }
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





    @RequestMapping(value = "/vaults/deposits", method = RequestMethod.GET)
    public List<DepositInfo> getDepositsAll(@RequestHeader(value = "X-UserID", required = true) String userID,
                                            @RequestParam(value = "sort", required = false) String sort) throws Exception {
        
        List<DepositInfo> depositResponses = new ArrayList<>();
        for (Deposit deposit : depositsService.getDeposits(sort)) {
            depositResponses.add(deposit.convertToResponse());
        }
        return depositResponses;
    }

    @RequestMapping(value = "/vaults/restores", method = RequestMethod.GET)
    public List<Restore> getRestoresAll(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return restoresService.getRestores();
    }

    @RequestMapping(value = "/vaults", method = RequestMethod.POST)
    public VaultInfo addVault(@RequestHeader(value = "X-UserID", required = true) String userID,
                              @RequestBody CreateVault createVault) throws Exception {
        
        Vault vault = new Vault();
        vault.setName(createVault.getName());
        vault.setDescription(createVault.getDescription());
        
        Policy policy = policiesService.getPolicy(createVault.getPolicyID());
        if (policy == null) {
            throw new Exception("Policy '" + createVault.getPolicyID() + "' does not exist");
        }
        vault.setPolicy(policy);
        
        Group group = groupsService.getGroup(createVault.getGroupID());
        if (group == null) {
            throw new Exception("Group '" + createVault.getGroupID() + "' does not exist");
        }
        vault.setGroup(group);
        
        User user = usersService.getUser(userID);
        if (user == null) {
            throw new Exception("User '" + userID + "' does not exist");
        }
        vault.setUser(user);
        
        Dataset dataset = externalMetadataService.getCachedDataset(createVault.getDatasetID());
        if (dataset == null) {
            dataset = externalMetadataService.getDataset(createVault.getDatasetID());
            if (dataset == null) {
                throw new Exception("Dataset metadata record '" + createVault.getDatasetID() + "' does not exist");
            }
            
            externalMetadataService.addCachedDataset(dataset);
        }
        vault.setDataset(dataset);
        
        // For testing purposes add a default file store for the user if none exists.
        // This is an action that will ideally take place at user creation instead.
        // A template could be used to construct file paths (if configured).
        List<FileStore> userStores = user.getFileStores();
        if (userStores.isEmpty()) {
            HashMap<String,String> storeProperties = new HashMap<String,String>();
            storeProperties.put("rootPath", activeDir);
            FileStore store = new FileStore("org.datavaultplatform.common.storage.impl.LocalFileSystem", storeProperties, "Default filesystem (local)");
            store.setUser(user);
            fileStoreService.addFileStore(store);
        }

        // Also configure a default system-level archive store if none exists.
        // This would normally be part of system configuration.
        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        if (archiveStores.isEmpty()) {
            HashMap<String,String> storeProperties = new HashMap<String,String>();
            storeProperties.put("rootPath", archiveDir);
            ArchiveStore store = new ArchiveStore("org.datavaultplatform.common.storage.impl.LocalFileSystem", storeProperties, "Default archive store (local)");
            archiveStoreService.addArchiveStore(store);
        }

        vaultsService.addVault(vault);
        return vault.convertToResponse();
    }

    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public VaultInfo getVault(@RequestHeader(value = "X-UserID", required = true) String userID,
                              @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = getUserVault(user, vaultID);
        if (vault != null) {
            return vault.convertToResponse();
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/vaults/{vaultid}/checkpolicy", method = RequestMethod.GET)
    public Vault checkVaultPolicy(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @PathVariable("vaultid") String vaultID) throws Exception {

        return vaultsService.checkPolicy(vaultID);
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits", method = RequestMethod.GET)
    public List<DepositInfo> getDeposits(@RequestHeader(value = "X-UserID", required = true) String userID,
                                         @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = getUserVault(user, vaultID);

        List<DepositInfo> depositResponses = new ArrayList<>();
        for (Deposit deposit : vault.getDeposits()) {
            depositResponses.add(deposit.convertToResponse());
        }
        return depositResponses;
    }
}
