package org.datavaultplatform.broker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.EventInfo;
import org.datavaultplatform.common.task.Task;
import org.jsondoc.core.annotation.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private AdminService adminService;
    private Sender sender;
    private String optionsDir;
	private String tempDir;
    private String bucketName;
    private String region;
    private String awsAccessKey;
    private String awsSecretKey;

    private static final Logger logger = LoggerFactory.getLogger(DepositsController.class);
    
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

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }
    
    public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}
    
    public void setOptionsDir(String optionsDir) {
    	this.optionsDir = optionsDir;
    }
    
    public void setBucketName(String bucketName) {
    	this.bucketName = bucketName;
    }

    public void setRegion(String region) {
    	this.region = region;
    }

    public void setAwsAccessKey(String awsAccessKey) {
    	this.awsAccessKey = awsAccessKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
    	this.awsSecretKey = awsSecretKey;
    }

    @RequestMapping(value = "/deposits/{depositid}", method = RequestMethod.GET)
    public DepositInfo getDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);

        return depositsService.getUserDeposit(user, depositID).convertToResponse();
    }

    @RequestMapping(value = "/deposits", method = RequestMethod.POST)
    public ResponseEntity<Object> addDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                             @RequestBody CreateDeposit createDeposit) throws Exception {

        Deposit deposit = new Deposit();

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, createDeposit.getVaultID());

        deposit.setName(createDeposit.getName());
        deposit.setDescription(createDeposit.getDescription());
        if (createDeposit.getHasPersonalData().toLowerCase().equals("yes")) {
            deposit.setHasPersonalData(true);
        } else {
            deposit.setHasPersonalData(false);
        }
        deposit.setPersonalDataStatement(createDeposit.getPersonalDataStatement());
        deposit.setDepositPaths(new ArrayList<>());

        System.out.println("Deposit File Path: ");
        for (DepositPath dPath : deposit.getDepositPaths()){
            System.out.println("\t- " + dPath.getFilePath());
        }

        if (user == null) {
            throw new Exception("User '" + userID + "' does not exist");
        }
        deposit.setUser(user);

        // Add the file upload path
        DepositPath fileUploadPath = new DepositPath(deposit, createDeposit.getFileUploadHandle(), Path.PathType.USER_UPLOAD);
        deposit.getDepositPaths().add(fileUploadPath);

        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        if (archiveStores.size() == 0) {
            throw new Exception("No configured archive storage");
        }

        archiveStores = this.addArchiveSpecificOptions(archiveStores);

        System.out.println("Deposit File Path: ");
        for (DepositPath dPath : deposit.getDepositPaths()){
            System.out.println("\t- " + dPath.getFilePath());
        }

        // Add the deposit object
        depositsService.addDeposit(vault, deposit, "", "");

        this.runDeposit(archiveStores, deposit, createDeposit.getDepositPaths(), null);

        // Check the retention policy of the newly created vault
        vaultsService.checkRetentionPolicy(vault.getID());

        return new ResponseEntity<>(deposit.convertToResponse(), HttpStatus.OK);
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

        return deposit.getRetrieves();
    }

    @RequestMapping(value = "/deposits/{depositid}/jobs", method = RequestMethod.GET)
    public List<Job> getDepositJobs(@RequestHeader(value = "X-UserID", required = true) String userID,
                                    @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        return deposit.getJobs();
    }

    @RequestMapping(value = "/deposits/{depositid}/retrieve", method = RequestMethod.POST)
    public Boolean retrieveDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @PathVariable("depositid") String depositID,
                                  @RequestBody Retrieve retrieve) throws Exception {
        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);
        
        if (user == null) {
            throw new Exception("User '" + userID + "' does not exist");
        }
        retrieve.setUser(user);
        
        List<Job> jobs = deposit.getJobs();
        for (Job job : jobs) {
            if (!job.isError() && job.getState() != job.getStates().size() - 1) {
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

        // Fetch the ArchiveStore that is flagged for retrieval. We store it in a list as the Task parameters require a list.
        ArchiveStore archiveStore = archiveStoreService.getForRetrieval();
        List<ArchiveStore> archiveStores = new ArrayList<>();
        archiveStores.add(archiveStore);
        archiveStores = this.addArchiveSpecificOptions(archiveStores);

        // Find the Archive that matches the ArchiveStore.
        String archiveID = null;
        for (Archive archive : deposit.getArchives()) {
            if (archive.getArchiveStore().getID().equals(archiveStore.getID())) {
                archiveID = archive.getArchiveId();
            }
        }

        // Worth checking that we found a matching Archive for the ArchiveStore.
        if (archiveID == null) {
            throw new Exception("No valid archive for retrieval");
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
            retrieveProperties.put("archiveId", archiveID);
            retrieveProperties.put("archiveSize", Long.toString(deposit.getArchiveSize()));
            retrieveProperties.put("userId", user.getID());
            retrieveProperties.put("archiveDigest", deposit.getArchiveDigest());
            retrieveProperties.put("archiveDigestAlgorithm", deposit.getArchiveDigestAlgorithm());
            retrieveProperties.put("numOfChunks", Integer.toString(deposit.getNumOfChunks()));
            
            // Add a single entry for the user file storage
            Map<String, String> userFileStoreClasses = new HashMap<>();
            Map<String, Map<String, String>> userFileStoreProperties = new HashMap<>();
            userFileStoreClasses.put(storageID, userStore.getStorageClass());
            userFileStoreProperties.put(storageID, userStore.getProperties());
            
            // get chunks checksums
            HashMap<Integer, String> chunksDigest = new HashMap<>();
            List<DepositChunk> depositChunks = deposit.getDepositChunks();
            for (DepositChunk depositChunk : depositChunks) {
                chunksDigest.put(depositChunk.getChunkNum(), depositChunk.getArchiveDigest());
            }
            
            // Get encryption IVs
            byte[] tarIVs = deposit.getEncIV();
            HashMap<Integer, byte[]> chunksIVs = new HashMap<>();
            for( DepositChunk chunks : deposit.getDepositChunks() ) {
                chunksIVs.put(chunks.getChunkNum(), chunks.getEncIV());
            }
            
            // Get encrypted digests
            String encTarDigest = deposit.getEncArchiveDigest();
            HashMap<Integer, String> encChunksDigests = new HashMap<>();
            for( DepositChunk chunks : deposit.getDepositChunks() ) {
                encChunksDigests.put(chunks.getChunkNum(), chunks.getEcnArchiveDigest());
            }
            
            Task retrieveTask = new Task(
                    job, retrieveProperties, archiveStores, 
                    userFileStoreProperties, userFileStoreClasses, 
                    null, null, 
                    chunksDigest,
                    tarIVs, chunksIVs,
                    encTarDigest, encChunksDigests, null);
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
    
    private List<ArchiveStore> addArchiveSpecificOptions(List<ArchiveStore> archiveStores) {
    	if (archiveStores != null && ! archiveStores.isEmpty()) { 
	    	for (ArchiveStore archiveStore : archiveStores) {
		        if (archiveStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.TivoliStorageManager")) {
		        	HashMap<String, String> asProps = archiveStore.getProperties();
		        	if (this.optionsDir != null && ! this.optionsDir.equals("")) {
		        		asProps.put("optionsDir", this.optionsDir);
		        	}
		        	if (this.tempDir != null && ! this.tempDir.equals("")) {
		        		asProps.put("tempDir", this.tempDir);
		        	}
		        	archiveStore.setProperties(asProps);
		        }
		        
		        if (archiveStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.S3Cloud")) {
		        	HashMap<String, String> asProps = archiveStore.getProperties();
		        	if (this.bucketName != null && ! this.bucketName.equals("")) {  
		        		asProps.put("s3.bucketName", this.bucketName);
		        	}
		        	if (this.region != null && ! this.region.equals("")) {  
		        		asProps.put("s3.region", this.region);
		        	}
		        	if (this.awsAccessKey != null && ! this.awsAccessKey.equals("")) {  
		        		asProps.put("s3.awsAccessKey", this.awsAccessKey);
		        	}
		        	if (this.awsSecretKey != null && ! this.awsSecretKey.equals("")) {  
		        		asProps.put("s3.awsSecretKey", this.awsSecretKey);
		        	}

		        	//if (this.authDir != null && ! this.authDir.equals("")) {
		        	//	asProps.put("authDir", this.authDir);
		        	//}
		        	archiveStore.setProperties(asProps);
		        }
	        }
    	}
    	
    	return archiveStores;
    }

    @RequestMapping(value = "/deposits/{depositid}/restart", method = RequestMethod.POST)
    public Deposit restartDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                   @PathVariable("depositid") String depositID) throws Exception{

        User user = adminService.ensureAdminUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);


        if (deposit == null) {
            throw new Exception("Deposit '" + depositID + "' does not exist");
        }

        List<FileStore> userStores = user.getFileStores();
        System.out.println("There is " + userStores.size() + "user stores.");

        ArrayList<String> paths = new ArrayList<>();
        for(DepositPath dPath : deposit.getDepositPaths()){
            if(dPath.getPathType() == Path.PathType.FILESTORE) {
                paths.add(dPath.getFilePath());
            }
        }
        System.out.println("There is " + paths.size() + " deposit path");

        // Get last Deposit Event
        Event lastEvent = deposit.getLastNotFailedEvent();
        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        archiveStores = this.addArchiveSpecificOptions(archiveStores);
        runDeposit(archiveStores, deposit, paths, lastEvent);

        return deposit;
    }

    private Job runDeposit(List<ArchiveStore> archiveStores, Deposit deposit, List<String> paths, Event lastEvent) throws Exception{
        User user = deposit.getUser();
        Vault vault = deposit.getVault();

        String externalMetadata = externalMetadataService.getDatasetContent(vault.getDataset().getID());

        List<FileStore> userStores = user.getFileStores();

        Map<String, String> userFileStoreClasses = new HashMap<>();
        Map<String, Map<String, String>> userFileStoreProperties = new HashMap<>();

        // Add any server-side filestore paths
        if (paths != null) {
            for (String path : paths) {

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
                        throw (e);
                    }
                }

                System.out.println("Add deposit path: " + path);
                DepositPath depositPath = new DepositPath(deposit, path, Path.PathType.FILESTORE);
                deposit.getDepositPaths().add(depositPath);
            }
        }

        // Create a job to track this deposit
        Job job = new Job("org.datavaultplatform.worker.tasks.Deposit");
        jobsService.addJob(deposit, job);

        // Ask the worker to process the deposit
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

        for (DepositPath path : deposit.getDepositPaths()) {
            if (path.getPathType() == Path.PathType.FILESTORE) {
                filestorePaths.add(path.getFilePath());
            } else if (path.getPathType() == Path.PathType.USER_UPLOAD) {
                userUploadPaths.add(path.getFilePath());
            }
        }

        Task depositTask = new Task(
                job, depositProperties, archiveStores,
                userFileStoreProperties, userFileStoreClasses,
                filestorePaths, userUploadPaths,
                null, null, null, null, null,
                lastEvent);
        String jsonDeposit = mapper.writeValueAsString(depositTask);
        sender.send(jsonDeposit);

        return job;
    }
}
