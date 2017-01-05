package org.datavaultplatform.broker.controllers;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
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
    private RetrievesService retrievesService;
    private MetadataService metadataService;
    private ExternalMetadataService externalMetadataService;
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

    public void setRetrievesService(RetrievesService retrievesService) {
        this.retrievesService = retrievesService;
    }

    public void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }
    
    public void setExternalMetadataService(ExternalMetadataService externalMetadataService) {
        this.externalMetadataService = externalMetadataService;
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
        deposit.setDepositPaths(new ArrayList<DepositPath>());
        
        List<FileStore> userStores = user.getFileStores();
        
        Map<String, String> userFileStoreClasses = new HashMap<>();
        Map<String, Map<String, String>> userFileStoreProperties = new HashMap<>();
        
        // Add any server-side filestore paths
        if (createDeposit.getDepositPaths() != null) {
            for (String path : createDeposit.getDepositPaths()) {

                String storageID, storagePath;
                if (!path.contains("/")) {
                    // A request to archive the whole share/device
                    storageID = path;
                    storagePath = "/";
                } else {
                    // A request to archive a sub-directory
                    storageID = path.substring(0, path.indexOf("/"));
                    storagePath = path.replaceFirst(storageID + "/", "");
                }

                if (!userFileStoreClasses.containsKey(storageID)) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();

                        FileStore userStore = null;
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

                        userFileStoreClasses.put(storageID, userStore.getStorageClass());
                        userFileStoreProperties.put(storageID, userStore.getProperties());

                    } catch (Exception e) {
                        e.printStackTrace();
                        throw(e);
                    }
                }

                DepositPath depositPath = new DepositPath(deposit, path, Path.PathType.FILESTORE);
                deposit.getDepositPaths().add(depositPath);
            }
        }
        
        // Add the file upload path
        DepositPath fileUploadPath = new DepositPath(deposit, createDeposit.getFileUploadHandle(), Path.PathType.USER_UPLOAD);
        deposit.getDepositPaths().add(fileUploadPath);
        
        ArchiveStore archiveStore = null;
        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        if (archiveStores.size() > 0) {
            // For now, just use the first configured archive store
            archiveStore = archiveStores.get(0);
        } else {
            throw new Exception("No configured archive storage");
        }

        /*
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
        */
        
        // Get metadata content from the external provider (bypass database cache)
        String externalMetadata = externalMetadataService.getDatasetContent(vault.getDataset().getID());
        
        // Add the deposit object
        depositsService.addDeposit(vault, deposit, archiveStore.getID());
        
        // Create a job to track this deposit
        Job job = new Job("org.datavaultplatform.worker.tasks.Deposit");
        jobsService.addJob(deposit, job);

        // Ask the worker to process the deposit
        try {
            ObjectMapper mapper = new ObjectMapper();

            HashMap<String, String> depositProperties = new HashMap<>();
            depositProperties.put("depositId", deposit.getID());
            depositProperties.put("bagId", deposit.getBagId());
            depositProperties.put("userId", user.getID());
            
            // Deposit and Vault metadata
            // TODO: at the moment we're just serialising the objects to JSON.
            // In future we'll need a more formal schema/representation (e.g. RDF or JSON-LD).
            depositProperties.put("depositMetadata", mapper.writeValueAsString(deposit));
            depositProperties.put("vaultMetadata", mapper.writeValueAsString(vault));
            
            // External metadata is text from an external system - e.g. XML or JSON
            depositProperties.put("externalMetadata", externalMetadata);
            
            ArrayList<String> filestorePaths = new ArrayList<>();
            ArrayList<String> userUploadPaths = new ArrayList<>();
            
            for (DepositPath path: deposit.getDepositPaths()) {
                if (path.getPathType() == Path.PathType.FILESTORE) {
                    filestorePaths.add(path.getFilePath());
                } else if (path.getPathType() == Path.PathType.USER_UPLOAD) {
                    userUploadPaths.add(path.getFilePath());
                }
            }
            
            Task depositTask = new Task(job, depositProperties, archiveStore, userFileStoreProperties, userFileStoreClasses, filestorePaths, userUploadPaths);
            String jsonDeposit = mapper.writeValueAsString(depositTask);
            sender.send(jsonDeposit);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check the retention policy of the newly created vault
        vaultsService.checkRetentionPolicy(vault.getID());

        return deposit.convertToResponse();
    }
    
    @RequestMapping(value = "/deposits/{depositid}/manifest", method = RequestMethod.GET)
    public List<FileFixity> getDepositManifest(@RequestHeader(value = "X-UserID", required = true) String userID,
                                               @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        List<FileFixity> manifest = new ArrayList<>();
        
        if (deposit.getStatus() == Deposit.Status.COMPLETE) {
            manifest = metadataService.getManifest(deposit.getBagId());
        }
        
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

    @RequestMapping(value = "/deposits/{depositid}/retrieves", method = RequestMethod.GET)
    public List<Retrieve> getDepositRetrieves(@RequestHeader(value = "X-UserID", required = true) String userID,
                                            @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        List<Retrieve> retrieves = deposit.getRetrieves();
        return retrieves;
    }

    @RequestMapping(value = "/deposits/{depositid}/jobs", method = RequestMethod.GET)
    public List<Job> getDepositJobs(@RequestHeader(value = "X-UserID", required = true) String userID,
                                    @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        List<Job> jobs = deposit.getJobs();
        return jobs;
    }

    @RequestMapping(value = "/deposits/{depositid}/retrieve", method = RequestMethod.POST)
    public Boolean retrieveDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @PathVariable("depositid") String depositID,
                                  @RequestBody Retrieve retrieve) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        List<Job> jobs = deposit.getJobs();
        for (Job job : jobs) {
            if (job.isError() == false && job.getState() != job.getStates().size() - 1) {
                // There's an in-progress job for this deposit
                throw new IllegalArgumentException("Job in-progress for this Deposit");
            }
        }
        
        String fullPath = retrieve.getRetrievePath();
        String storageID, retrievePath;
        if (!fullPath.contains("/")) {
            // A request to retrieve the whole share/device
            storageID = fullPath;
            retrievePath = "/";
        } else {
            // A request to retrieve a sub-directory
            storageID = fullPath.substring(0, fullPath.indexOf("/"));
            retrievePath = fullPath.replaceFirst(storageID + "/", "");
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
        if (retrievePath == null) {
            throw new IllegalArgumentException("Path was null");
        }

        // Check the source file path is valid
        if (!filesService.validPath(retrievePath, userStore)) {
            throw new IllegalArgumentException("Path '" + retrievePath + "' is invalid");
        }

        // Create a job to track this retrieve
        Job job = new Job("org.datavaultplatform.worker.tasks.Retrieve");
        jobsService.addJob(deposit, job);

        // Add the retrieve object
        retrievesService.addRetrieve(retrieve, deposit, retrievePath);

        // Ask the worker to process the data retrieve
        try {
            HashMap<String, String> retrieveProperties = new HashMap<>();
            retrieveProperties.put("depositId", deposit.getID());
            retrieveProperties.put("retrieveId", retrieve.getID());
            retrieveProperties.put("bagId", deposit.getBagId());
            retrieveProperties.put("retrievePath", retrievePath); // No longer the absolute path
            retrieveProperties.put("archiveId", deposit.getArchiveId());
            retrieveProperties.put("archiveSize", Long.toString(deposit.getArchiveSize()));
            retrieveProperties.put("userId", user.getID());
            retrieveProperties.put("archiveDigest", deposit.getArchiveDigest());
            retrieveProperties.put("archiveDigestAlgorithm", deposit.getArchiveDigestAlgorithm());
            
            // Add a single entry for the user file storage
            Map<String, String> userFileStoreClasses = new HashMap<>();
            Map<String, Map<String, String>> userFileStoreProperties = new HashMap<>();
            userFileStoreClasses.put(storageID, userStore.getStorageClass());
            userFileStoreProperties.put(storageID, userStore.getProperties());
            
            Task retrieveTask = new Task(job, retrieveProperties, archiveStore, userFileStoreProperties, userFileStoreClasses, null, null);
            ObjectMapper mapper = new ObjectMapper();
            String jsonRetrieve = mapper.writeValueAsString(retrieveTask);
            sender.send(jsonRetrieve);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check the retention policy of the newly created vault
        vaultsService.checkRetentionPolicy(deposit.getVault().getID());

        return true;
    }
}
