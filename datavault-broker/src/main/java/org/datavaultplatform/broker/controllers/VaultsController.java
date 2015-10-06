package org.datavaultplatform.broker.controllers;

import java.util.List;
import java.util.HashMap;

import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.queue.Sender;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class VaultsController {
    
    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RestoresService restoresService;
    private MetadataService metadataService;
    private FilesService filesService;
    private PoliciesService policiesService;
    private UsersService usersService;
    private FileStoreService fileStoreService;
    private JobsService jobsService;
    private Sender sender;
    
    private String activeDir;
    
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

    public void setFileStoreService(FileStoreService fileStoreService) {
        this.fileStoreService = fileStoreService;
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

    @RequestMapping(value = "/vaults", method = RequestMethod.GET)
    public List<Vault> getVaults(@RequestHeader(value = "X-UserID", required = true) String userID) {

        User user = usersService.getUser(userID);
        return user.getVaults();
    }

    @RequestMapping(value = "/vaults/all", method = RequestMethod.GET)
    public List<Vault> getVaultsAll(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return vaultsService.getVaults();
    }

    @RequestMapping(value = "/vaults/search/{query}", method = RequestMethod.GET)
    public List<Vault> searchAllVaults(@RequestHeader(value = "X-UserID", required = true) String userID,
                                       @PathVariable("query") String query) throws Exception {

        return vaultsService.search(query);
    }

    @RequestMapping(value = "/vaults/deposits/search/{query}", method = RequestMethod.GET)
    public List<Deposit> searchAllDeposits(@RequestHeader(value = "X-UserID", required = true) String userID,
                                           @PathVariable("query") String query) throws Exception {

        return depositsService.search(query);
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

    @RequestMapping(value = "/vaults/restorecount", method = RequestMethod.GET)
    public int getRestoresCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return restoresService.count();
    }

    @RequestMapping(value = "/vaults/deposits", method = RequestMethod.GET)
    public List<Deposit> getDepositsAll(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return depositsService.getDeposits();
    }

    @RequestMapping(value = "/vaults/restores", method = RequestMethod.GET)
    public List<Restore> getRestoresAll(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return restoresService.getRestores();
    }

    @RequestMapping(value = "/vaults", method = RequestMethod.POST)
    public Vault addVault(@RequestHeader(value = "X-UserID", required = true) String userID,
                          @RequestBody Vault vault) throws Exception {
        
        String policyID = vault.getPolicyID();
        Policy policy = policiesService.getPolicy(policyID);
        if (policy == null) {
            throw new Exception("Policy '" + policyID + "' does not exist");
        }
        vault.setPolicy(policy);
        
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
        
        vaultsService.addVault(vault);
        return vault;
    }

    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public Vault getVault(@RequestHeader(value = "X-UserID", required = true) String userID,
                          @PathVariable("vaultid") String vaultID) throws Exception {

        User user = usersService.getUser(userID);
        return getUserVault(user, vaultID);
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits", method = RequestMethod.GET)
    public List<Deposit> getDeposits(@RequestHeader(value = "X-UserID", required = true) String userID,
                                     @PathVariable("vaultid") String vaultID) throws Exception {
        
        User user = usersService.getUser(userID);
        Vault vault = getUserVault(user, vaultID);
        
        return vault.getDeposits();
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits", method = RequestMethod.POST)
    public Deposit addDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                              @PathVariable("vaultid") String vaultID,
                              @RequestBody Deposit deposit) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = getUserVault(user, vaultID);
        
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
        
        FileStore store = null;
        List<FileStore> userStores = user.getFileStores();
        for (FileStore userStore : userStores) {
            if (userStore.getID().equals(storageID)) {
                store = userStore;
            }
        }
        
        if (store == null) {
            throw new IllegalArgumentException("Storage ID '" + storageID + "' is invalid");
        }
        
        // Check the source file path is valid
        if (!filesService.validPath(storagePath, store)) {
            throw new IllegalArgumentException("Path '" + storagePath + "' is invalid");
        }
        
        // Add the deposit object
        depositsService.addDeposit(vault, deposit, storagePath, store.getLabel());
        
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
            
            Task depositTask = new Task(job, depositProperties, store);
            String jsonDeposit = mapper.writeValueAsString(depositTask);
            sender.send(jsonDeposit);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return deposit;
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}", method = RequestMethod.GET)
    public Deposit getDeposit(@RequestHeader(value = "X-UserID", required = true) String userID, 
                              @PathVariable("vaultid") String vaultID,
                              @PathVariable("depositid") String depositID) throws Exception {
        
        User user = usersService.getUser(userID);
        return getUserDeposit(user, vaultID, depositID);
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
    public Deposit setDepositState(@RequestHeader(value = "X-UserID", required = true) String userID, 
                                   @PathVariable("vaultid") String vaultID,
                                   @PathVariable("depositid") String depositID,
                                   @RequestBody Deposit.Status status) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = getUserDeposit(user, vaultID, depositID);
        
        deposit.setStatus(status);
        depositsService.updateDeposit(deposit);
        return deposit;
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/restore", method = RequestMethod.POST)
    public Boolean restoreDeposit(@RequestHeader(value = "X-UserID", required = true) String userID, 
                                  @PathVariable("vaultid") String vaultID,
                                  @PathVariable("depositid") String depositID,
                                  @RequestBody Restore restore) throws Exception {
        
        User user = usersService.getUser(userID);
        Deposit deposit = getUserDeposit(user, vaultID, depositID);
        
        String fullPath = restore.getRestorePath();
        if (!fullPath.contains("/")) {
            throw new IllegalArgumentException("Path '" + fullPath + "' does not contain a storage ID");
        }
        
        String storageID = fullPath.substring(0, fullPath.indexOf("/"));
        String restorePath = fullPath.replaceFirst(storageID + "/", "");
        
        FileStore store = null;
        List<FileStore> userStores = user.getFileStores();
        for (FileStore userStore : userStores) {
            if (userStore.getID().equals(storageID)) {
                store = userStore;
            }
        }
        
        if (store == null) {
            throw new IllegalArgumentException("Storage ID '" + storageID + "' is invalid");
        }
        
        // Validate the path
        if (restorePath == null) {
            throw new IllegalArgumentException("Path was null");
        }
        
        // Check the source file path is valid
        if (!filesService.validPath(restorePath, store)) {
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
            restoreProperties.put("bagId", deposit.getBagId());
            restoreProperties.put("restorePath", restorePath); // No longer the absolute path
            
            Task restoreTask = new Task(job, restoreProperties, store);
            ObjectMapper mapper = new ObjectMapper();
            String jsonRestore = mapper.writeValueAsString(restoreTask);
            sender.send(jsonRestore);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return true;
    }
}
