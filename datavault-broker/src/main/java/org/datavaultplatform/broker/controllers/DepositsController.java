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
import org.datavaultplatform.broker.queue.Sender;

import org.jsondoc.core.annotation.*;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@Api(name="Deposits", description = "Interact with DataVault Deposits")
public class DepositsController {

    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RetrievesService restoresService;
    private MetadataService metadataService;
    private FilesService filesService;
    private UsersService usersService;
    private ArchiveStoreService archiveStoreService;
    private JobsService jobsService;
    private Sender sender;

    private static final Logger logger = LoggerFactory.getLogger(VaultsController.class);
    
    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }

    public void setRestoresService(RetrievesService restoresService) {
        this.restoresService = restoresService;
    }

    public void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    public void setFilesService(FilesService filesService) {
        this.filesService = filesService;
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

    @RequestMapping(value = "/deposits/{depositid}", method = RequestMethod.GET)
    public DepositInfo getDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        return depositsService.getUserDeposit(user, depositID).convertToResponse();
    }

    @RequestMapping(value = "/deposits", method = RequestMethod.POST)
    public DepositInfo addDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @RequestBody CreateDeposit createDeposit) throws Exception {

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, createDeposit.getVaultID());
        
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
            depositProperties.put("userId", user.getID());

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
    
    @RequestMapping(value = "/deposits/{depositid}/manifest", method = RequestMethod.GET)
    public List<FileFixity> getDepositManifest(@RequestHeader(value = "X-UserID", required = true) String userID,
                                               @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        List<FileFixity> manifest = metadataService.getManifest(deposit.getBagId());
        return manifest;
    }

    @RequestMapping(value = "/deposits/{depositid}/events", method = RequestMethod.GET)
    public List<EventInfo> getDepositEvents(@RequestHeader(value = "X-UserID", required = true) String userID,
                                            @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        List<EventInfo> events = new ArrayList<>();
        
        for (Event event : deposit.getEvents()) {
            events.add(event.convertToResponse());
        }
        
        return events;
    }

    @RequestMapping(value = "/deposits/{depositid}/restores", method = RequestMethod.GET)
    public List<Restore> getDepositRestores(@RequestHeader(value = "X-UserID", required = true) String userID,
                                            @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        List<Restore> restores = deposit.getRestores();
        return restores;
    }

    @RequestMapping(value = "/deposits/{depositid}/jobs", method = RequestMethod.GET)
    public List<Job> getDepositJobs(@RequestHeader(value = "X-UserID", required = true) String userID,
                                    @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        List<Job> jobs = deposit.getJobs();
        return jobs;
    }

    @RequestMapping(value = "/deposits/{depositid}/restore", method = RequestMethod.POST)
    public Boolean restoreDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @PathVariable("depositid") String depositID,
                                  @RequestBody Restore restore) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

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
            restoreProperties.put("userId", user.getID());

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
