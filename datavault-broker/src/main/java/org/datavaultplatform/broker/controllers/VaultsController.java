package org.datavaultplatform.broker.controllers;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.*;
import org.datavaultplatform.common.response.*;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.queue.Sender;

import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@Api(name="Vaults", description = "Interact with DataVault Vaults")
public class VaultsController {

    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RestoresService restoresService;
    private MetadataService metadataService;
    private FilesService filesService;
    private PoliciesService policiesService;
    private GroupsService groupsService;
    private UsersService usersService;
    private FileStoreService fileStoreService;
    private ArchiveStoreService archiveStoreService;
    private JobsService jobsService;
    private Sender sender;

    private String activeDir;
    private String archiveDir;
    
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

    // Get the specified Deposit object and validate it against the current User and Vault
    private Deposit getUserDeposit(User user, String vaultID, String depositID) throws Exception {

        Vault vault = getUserVault(user, vaultID);
        Deposit deposit = depositsService.getDeposit(depositID);

        if (!vault.equals(deposit.getVault())) {
            throw new Exception("Invalid Vault ID");
        }

        return deposit;
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

    public void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    public void setFilesService(FilesService filesService) {
        this.filesService = filesService;
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

    public void setJobsService(JobsService jobsService) {
        this.jobsService = jobsService;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
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

    @RequestMapping(value = "/vaults/count", method = RequestMethod.GET)
    public int getVaultsCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return vaultsService.count();
    }

    @RequestMapping(value = "/vaults/size", method = RequestMethod.GET)
    public Long getVaultsSize(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return depositsService.size();
    }

    @RequestMapping(value = "/vaults/depositcount", method = RequestMethod.GET)
    public int getDepositsCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return depositsService.count();
    }

    @RequestMapping(value = "/vaults/depositqueuecount", method = RequestMethod.GET)
    public int getDepositsQueueCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return depositsService.queueCount();
    }

    @RequestMapping(value = "/vaults/depositinprogresscount", method = RequestMethod.GET)
    public int getDepositsInProgressCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return depositsService.inProgressCount();
    }

    @RequestMapping(value = "/vaults/restorecount", method = RequestMethod.GET)
    public int getRestoresCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return restoresService.count();
    }

    @RequestMapping(value = "/vaults/restorequeuecount", method = RequestMethod.GET)
    public int getRestoresQueuedCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return restoresService.queueCount();
    }

    @RequestMapping(value = "/vaults/restoreinprogresscount", method = RequestMethod.GET)
    public int getRestoresInProgressCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return restoresService.inProgressCount();
    }

    @RequestMapping(value = "/vaults/policycount/{status}", method = RequestMethod.GET)
    public int getPolicyStatusCount(@RequestHeader(value = "X-UserID", required = true) String userID,
                                @PathVariable("status") int status) throws Exception {

        return vaultsService.getPolicyCount(status);
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

    @RequestMapping(value = "/vaults/{vaultid}/deposits", method = RequestMethod.POST)
    public DepositInfo addDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @PathVariable("vaultid") String vaultID,
                                  @RequestBody CreateDeposit createDeposit) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = getUserVault(user, vaultID);
        
        Deposit deposit = new Deposit();
        deposit.setNote(createDeposit.getNote());
        deposit.setFilePath(createDeposit.getFilePath());
        
        String fullPath = deposit.getFilePath();
        String storageID, storagePath;
        if (!fullPath.contains("/")) {
            // A request to archive the whole share/device
            storageID = fullPath;
            storagePath = "/";
        } else {
            // A request to archive a sub-directory
            storageID = fullPath.substring(0, fullPath.indexOf("/"));
            storagePath = fullPath.replaceFirst(storageID + "/", "");
        }

        ArchiveStore archiveStore = null;
        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        if (archiveStores.size() > 0) {
            // For now, just use the first configured archive store
            archiveStore = archiveStores.get(0);
        } else {
            throw new Exception("No configured archive storage");
        }

        FileStore userStore = null;
        List<FileStore> userStores = user.getFileStores();
        for (FileStore store : userStores) {
            if (store.getID().equals(storageID)) {
                userStore = store;
            }
        }

        if (userStore == null) {
            throw new IllegalArgumentException("Storage ID '" + storageID + "' is invalid");
        }

        // Check the source file path is valid
        if (!filesService.validPath(storagePath, userStore)) {
            throw new IllegalArgumentException("Path '" + storagePath + "' is invalid");
        }

        // Add the deposit object
        depositsService.addDeposit(vault, deposit, storagePath, userStore.getLabel(), archiveStore.getID());

        // Create a job to track this deposit
        Job job = new Job("org.datavaultplatform.worker.tasks.Deposit");
        jobsService.addJob(deposit, job);

        // Ask the worker to process the deposit
        try {
            ObjectMapper mapper = new ObjectMapper();

            HashMap<String, String> depositProperties = new HashMap<>();
            depositProperties.put("depositId", deposit.getID());
            depositProperties.put("bagId", deposit.getBagId());
            depositProperties.put("filePath", storagePath); // Path without storage ID

            // Deposit and Vault metadata
            // TODO: at the moment we're just serialising the objects to JSON.
            // In future we'll need a more formal schema/representation (e.g. RDF or JSON-LD).
            depositProperties.put("depositMetadata", mapper.writeValueAsString(deposit));
            depositProperties.put("vaultMetadata", mapper.writeValueAsString(vault));

            Task depositTask = new Task(job, depositProperties, userStore, archiveStore);
            String jsonDeposit = mapper.writeValueAsString(depositTask);
            sender.send(jsonDeposit);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return deposit.convertToResponse();
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}", method = RequestMethod.GET)
    public DepositInfo getDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @PathVariable("vaultid") String vaultID,
                                  @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        return getUserDeposit(user, vaultID, depositID).convertToResponse();
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/manifest", method = RequestMethod.GET)
    public List<FileFixity> getDepositManifest(@RequestHeader(value = "X-UserID", required = true) String userID,
                                               @PathVariable("vaultid") String vaultID,
                                               @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = getUserDeposit(user, vaultID, depositID);

        List<FileFixity> manifest = metadataService.getManifest(deposit.getBagId());
        return manifest;
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/events", method = RequestMethod.GET)
    public List<Event> getDepositEvents(@RequestHeader(value = "X-UserID", required = true) String userID,
                                        @PathVariable("vaultid") String vaultID,
                                        @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = getUserDeposit(user, vaultID, depositID);

        List<Event> events = deposit.getEvents();
        return events;
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/restores", method = RequestMethod.GET)
    public List<Restore> getDepositRestores(@RequestHeader(value = "X-UserID", required = true) String userID,
                                            @PathVariable("vaultid") String vaultID,
                                            @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = getUserDeposit(user, vaultID, depositID);

        List<Restore> restores = deposit.getRestores();
        return restores;
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/jobs", method = RequestMethod.GET)
    public List<Job> getDepositJobs(@RequestHeader(value = "X-UserID", required = true) String userID,
                                    @PathVariable("vaultid") String vaultID,
                                    @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = getUserDeposit(user, vaultID, depositID);

        List<Job> jobs = deposit.getJobs();
        return jobs;
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/status", method = RequestMethod.GET)
    public Deposit.Status getDepositState(@RequestHeader(value = "X-UserID", required = true) String userID,
                                          @PathVariable("vaultid") String vaultID,
                                          @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = getUserDeposit(user, vaultID, depositID);

        return deposit.getStatus();
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/status", method = RequestMethod.POST)
    public DepositInfo setDepositState(@RequestHeader(value = "X-UserID", required = true) String userID,
                                       @PathVariable("vaultid") String vaultID,
                                       @PathVariable("depositid") String depositID,
                                       @RequestBody Deposit.Status status) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = getUserDeposit(user, vaultID, depositID);

        deposit.setStatus(status);
        depositsService.updateDeposit(deposit);
        return deposit.convertToResponse();
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/restore", method = RequestMethod.POST)
    public Boolean restoreDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @PathVariable("vaultid") String vaultID,
                                  @PathVariable("depositid") String depositID,
                                  @RequestBody Restore restore) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = getUserDeposit(user, vaultID, depositID);

        String fullPath = restore.getRestorePath();
        String storageID, restorePath;
        if (!fullPath.contains("/")) {
            // A request to restore the whole share/device
            storageID = fullPath;
            restorePath = "/";
        } else {
            // A request to restore a sub-directory
            storageID = fullPath.substring(0, fullPath.indexOf("/"));
            restorePath = fullPath.replaceFirst(storageID + "/", "");
        }

        String archiveStoreID = deposit.getArchiveDevice();
        ArchiveStore archiveStore = archiveStoreService.getArchiveStore(archiveStoreID);

        if (archiveStore == null) {
            throw new IllegalArgumentException("Archive store ID '" + archiveStoreID + "' is invalid");
        }

        FileStore userStore = null;
        List<FileStore> userStores = user.getFileStores();
        for (FileStore store : userStores) {
            if (store.getID().equals(storageID)) {
                userStore = store;
            }
        }

        if (userStore == null) {
            throw new IllegalArgumentException("Storage ID '" + storageID + "' is invalid");
        }

        // Validate the path
        if (restorePath == null) {
            throw new IllegalArgumentException("Path was null");
        }

        // Check the source file path is valid
        if (!filesService.validPath(restorePath, userStore)) {
            throw new IllegalArgumentException("Path '" + restorePath + "' is invalid");
        }

        // Create a job to track this restore
        Job job = new Job("org.datavaultplatform.worker.tasks.Restore");
        jobsService.addJob(deposit, job);

        // Add the restore object
        restoresService.addRestore(restore, deposit, restorePath);

        // Ask the worker to process the data restore
        try {
            HashMap<String, String> restoreProperties = new HashMap<>();
            restoreProperties.put("depositId", deposit.getID());
            restoreProperties.put("restoreId", restore.getID());
            restoreProperties.put("bagId", deposit.getBagId());
            restoreProperties.put("restorePath", restorePath); // No longer the absolute path
            restoreProperties.put("archiveId", deposit.getArchiveId());
            restoreProperties.put("archiveSize", Long.toString(deposit.getArchiveSize()));

            Task restoreTask = new Task(job, restoreProperties, userStore, archiveStore);
            ObjectMapper mapper = new ObjectMapper();
            String jsonRestore = mapper.writeValueAsString(restoreTask);
            sender.send(jsonRestore);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
