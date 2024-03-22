package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.EventInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.jsondoc.core.annotation.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
//@CrossOrigin
@Api(name="Deposits", description = "Interact with DataVault Deposits")
public class DepositsController {

    private final VaultsService vaultsService;
    private final DepositsService depositsService;
    private final RetrievesService retrievesService;
    private final MetadataService metadataService;
    private final ExternalMetadataService externalMetadataService;
    private final FilesService filesService;
    private final UsersService usersService;
    private final ArchiveStoreService archiveStoreService;
    private final JobsService jobsService;
    private final AdminService adminService;
    private final Sender sender;
    private final String optionsDir;
    private final String tempDir;
    private final String bucketName;
    private final String region;
    private final String awsAccessKey;
    private final String awsSecretKey;
    private final String tsmRetryTime;
    private final String occRetryTime;
    private final String tsmMaxRetries;
    private final String occMaxRetries;
    private final String ociNameSpace;
    private final String ociBucketName;

    private final String tsmReverse;
    private final ObjectMapper mapper;

    private static final Logger logger = LoggerFactory.getLogger(DepositsController.class);

    @Autowired
    public DepositsController(VaultsService vaultsService, DepositsService depositsService,
        RetrievesService retrievesService, MetadataService metadataService,
        ExternalMetadataService externalMetadataService, FilesService filesService,
        UsersService usersService, ArchiveStoreService archiveStoreService, JobsService jobsService,
        AdminService adminService, Sender sender,
        @Value("${optionsDir:#{null}}") String optionsDir,
        @Value("${tempDir:#{null}}") String tempDir,
        @Value("${s3.bucketName:#{null}}") String bucketName,
        @Value("${s3.region:#{null}}") String region,
        @Value("${s3.awsAccessKey:#{null}}") String awsAccessKey,
        @Value("${s3.awsSecretKey:#{null}}") String awsSecretKey,
        @Value("${tsmRetryTime:#{null}}") String tsmRetryTime,
        @Value("${occRetryTime:#{null}}") String occRetryTime,
        @Value("${tsmMaxRetries:#{null}}") String tsmMaxRetries,
        @Value("${occMaxRetries:#{null}}") String occMaxRetries,
        @Value("${ociNameSpace:#{null}}") String ociNameSpace,
        @Value("${tsmReverse:#{false}}") String tsmReverse,
        @Value("${ociBucketName:#{null}}") String ociBucketName,
        ObjectMapper mapper) {
        this.vaultsService = vaultsService;
        this.depositsService = depositsService;
        this.retrievesService = retrievesService;
        this.metadataService = metadataService;
        this.externalMetadataService = externalMetadataService;
        this.filesService = filesService;
        this.usersService = usersService;
        this.archiveStoreService = archiveStoreService;
        this.jobsService = jobsService;
        this.adminService = adminService;
        this.sender = sender;
        this.optionsDir = optionsDir;
        this.tempDir = tempDir;
        this.bucketName = bucketName;
        this.region = region;
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        this.tsmRetryTime = tsmRetryTime;
        this.occRetryTime = occRetryTime;
        this.tsmMaxRetries = tsmMaxRetries;
        this.occMaxRetries = occMaxRetries;
        this.ociNameSpace = ociNameSpace;
        this.ociBucketName = ociBucketName;
        this.tsmReverse = tsmReverse;
        this.mapper = mapper;
    }



    @GetMapping("/deposits/{depositid}")
    public DepositInfo getDeposit(@RequestHeader(HEADER_USER_ID) String userID,
                                  @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);

        return depositsService.getUserDeposit(user, depositID).convertToResponse();
    }

    @PostMapping("/deposits")
    public ResponseEntity<DepositInfo> addDeposit(@RequestHeader(HEADER_USER_ID) String userID,
                                             @RequestBody CreateDeposit createDeposit) throws Exception {

        Deposit deposit = new Deposit();

        User user = usersService.getUser(userID);
        Vault vault = vaultsService.getUserVault(user, createDeposit.getVaultID());

        deposit.setName(createDeposit.getName());
        deposit.setDescription(createDeposit.getDescription());
        deposit.setHasPersonalData("yes".equalsIgnoreCase(createDeposit.getHasPersonalData()));
        deposit.setPersonalDataStatement(createDeposit.getPersonalDataStatement());
        deposit.setDepositPaths(new ArrayList<>());

        if (user == null) {
            throw new Exception("User '" + userID + "' does not exist");
        }
        deposit.setUser(user);

        // Add the file upload path
        DepositPath fileUploadPath = new DepositPath(deposit, createDeposit.getFileUploadHandle(), Path.PathType.USER_UPLOAD);
        deposit.getDepositPaths().add(fileUploadPath);

        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        if (archiveStores.isEmpty()) {
            throw new Exception("No configured archive storage");
        }

        archiveStores = this.addArchiveSpecificOptions(archiveStores);

        logger.info("Deposit File Path: ");
        for (DepositPath dPath : deposit.getDepositPaths()){
            logger.info("\t- " + dPath.getFilePath());
        }

        // Add the deposit object
        depositsService.addDeposit(vault, deposit, "", "");

        this.runDeposit(archiveStores, deposit, createDeposit.getDepositPaths(), null);

        // Check the retention policy of the newly created vault
        vaultsService.checkRetentionPolicy(vault.getID());

        return new ResponseEntity<>(deposit.convertToResponse(), HttpStatus.OK);
    }
    
    @GetMapping("/deposits/{depositid}/manifest")
    public List<FileFixity> getDepositManifest(@RequestHeader(HEADER_USER_ID) String userID,
                                               @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        List<FileFixity> manifest = new ArrayList<>();
        
        if (deposit.getStatus() == Deposit.Status.COMPLETE) {
            manifest = metadataService.getManifest(deposit.getBagId());
        }
        
        return manifest;
    }

    @GetMapping("/deposits/{depositid}/events")
    public List<EventInfo> getDepositEvents(@RequestHeader(HEADER_USER_ID) String userID,
                                            @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        List<EventInfo> events = new ArrayList<>();
        
        for (Event event : deposit.getEvents()) {
            events.add(event.convertToResponse());
        }
        
        return events;
    }

    @GetMapping("/deposits/{depositid}/retrieves")
    public List<Retrieve> getDepositRetrieves(@RequestHeader(HEADER_USER_ID) String userID,
                                            @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        return deposit.getRetrieves();
    }

    @GetMapping("/deposits/{depositid}/jobs")
    public List<Job> getDepositJobs(@RequestHeader(HEADER_USER_ID) String userID,
                                    @PathVariable("depositid") String depositID) throws Exception {

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        return deposit.getJobs();
    }

    @PostMapping( "/deposits/{depositid}/retrieve")
    public Boolean retrieveDeposit(@RequestHeader(HEADER_USER_ID) String userID,
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
            retrieveProperties.put(PropNames.DEPOSIT_ID, deposit.getID());
            retrieveProperties.put(PropNames.DEPOSIT_CREATION_DATE, DateTimeUtils.formatDateBasicISO(deposit.getCreationTime()));
            retrieveProperties.put(PropNames.RETRIEVE_ID, retrieve.getID());
            retrieveProperties.put(PropNames.BAG_ID, deposit.getBagId());
            retrieveProperties.put(PropNames.RETRIEVE_PATH, retrievePath); // No longer the absolute path
            retrieveProperties.put(PropNames.ARCHIVE_ID, archiveID);
            retrieveProperties.put(PropNames.ARCHIVE_SIZE, Long.toString(deposit.getArchiveSize()));
            retrieveProperties.put(PropNames.USER_ID, user.getID());
            retrieveProperties.put(PropNames.ARCHIVE_DIGEST, deposit.getArchiveDigest());
            retrieveProperties.put(PropNames.ARCHIVE_DIGEST_ALGORITHM, deposit.getArchiveDigestAlgorithm());
            retrieveProperties.put(PropNames.NUM_OF_CHUNKS, Integer.toString(deposit.getNumOfChunks()));
            
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
            String jsonRetrieve = this.mapper.writeValueAsString(retrieveTask);
            sender.send(jsonRetrieve);
        } catch (Exception e) {
            logger.error("unexpected exception", e);
        }

        // Check the retention policy of the newly created vault
        vaultsService.checkRetentionPolicy(deposit.getVault().getID());

        return true;
    }
    
    private List<ArchiveStore> addArchiveSpecificOptions(List<ArchiveStore> archiveStores) {
    	if (archiveStores != null && ! archiveStores.isEmpty()) {
	    	for (ArchiveStore archiveStore : archiveStores) {
		        if (archiveStore.isTivoliStorageManager()) {
		        	HashMap<String, String> asProps = archiveStore.getProperties();
		        	if (this.optionsDir != null && !this.optionsDir.isEmpty()) {
		        		asProps.put(PropNames.OPTIONS_DIR, this.optionsDir);
		        	}
		        	if (this.tempDir != null && !this.tempDir.isEmpty()) {
		        		asProps.put(PropNames.TEMP_DIR, this.tempDir);
		        	}
		        	if (this.tsmRetryTime != null && !this.tsmRetryTime.isEmpty()) {
		        	    asProps.put(PropNames.TSM_RETRY_TIME, this.tsmRetryTime);
                    }
                    if (this.tsmMaxRetries != null && !this.tsmMaxRetries.isEmpty()) {
                        asProps.put(PropNames.TSM_MAX_RETRIES, this.tsmMaxRetries);
                    }
                    if (this.tsmReverse != null && !this.tsmReverse.isEmpty()) {
                        asProps.put(PropNames.TSM_REVERSE, this.tsmReverse);
                    }

		        	archiveStore.setProperties(asProps);
		        }

		        if (archiveStore.isOracle()) {
                    HashMap<String, String> asProps = archiveStore.getProperties();
                    if (this.occRetryTime != null && !this.occRetryTime.isEmpty()) {
                        asProps.put(PropNames.OCC_RETRY_TIME, this.occRetryTime);
                    }
                    if (this.occMaxRetries != null && !this.occMaxRetries.isEmpty()) {
                        asProps.put(PropNames.OCC_MAX_RETRIES, this.occMaxRetries);
                    }
                    if (this.ociBucketName != null && !this.ociBucketName.isEmpty()) {
                        asProps.put(PropNames.OCI_BUCKET_NAME, this.ociBucketName);
                    }
                    if (this.ociNameSpace != null && !this.ociNameSpace.isEmpty()) {
                        asProps.put(PropNames.OCI_NAME_SPACE, this.ociNameSpace);
                    }
                    archiveStore.setProperties(asProps);
                }

		        if (archiveStore.isAmazonS3()) {
		        	HashMap<String, String> asProps = archiveStore.getProperties();
		        	if (this.bucketName != null && !this.bucketName.isEmpty()) {
		        		asProps.put(PropNames.AWS_S3_BUCKET_NAME, this.bucketName);
		        	}
		        	if (this.region != null && !this.region.isEmpty()) {
		        		asProps.put(PropNames.AWS_S3_REGION, this.region);
		        	}
		        	if (this.awsAccessKey != null && !this.awsAccessKey.isEmpty()) {
		        		asProps.put(PropNames.AWS_ACCESS_KEY, this.awsAccessKey);
		        	}
		        	if (this.awsSecretKey != null && !this.awsSecretKey.isEmpty()) {
		        		asProps.put(PropNames.AWS_SECRET_KEY, this.awsSecretKey);
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

    @PostMapping("/deposits/{depositid}/restart")
    public Deposit restartDeposit(@RequestHeader(HEADER_USER_ID) String userID,
                                   @PathVariable("depositid") String depositID) throws Exception{

        User user = adminService.ensureAdminUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);


        if (deposit == null) {
            throw new Exception("Deposit '" + depositID + "' does not exist");
        }

        List<FileStore> userStores = user.getFileStores();
        logger.info("There is " + userStores.size() + "user stores.");

        ArrayList<String> paths = new ArrayList<>();
        for(DepositPath dPath : deposit.getDepositPaths()){
            if(dPath.getPathType() == Path.PathType.FILESTORE) {
                paths.add(dPath.getFilePath());
            }
        }
        logger.info("There is " + paths.size() + " deposit path");
        if (paths.isEmpty()) {
            throw new Exception("There are no file paths for restarted deposit - Exiting");
        }
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

        //String externalMetadata = externalMetadataService.getDatasetContent(vault.getDataset().getID());

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
                        logger.error("unexpected exception", e);
                        throw (e);
                    }
                }

                if (lastEvent == null) {
                    logger.info("Add deposit path: " + path);
                    DepositPath depositPath = new DepositPath(deposit, path, Path.PathType.FILESTORE);
                    deposit.getDepositPaths().add(depositPath);
                    logger.debug("Prior to creating the jobs we have the following depositPaths:");
                    for (DepositPath dp : deposit.getDepositPaths()) {
                        logger.debug(dp.getFilePath());
                    }
                    depositsService.updateDeposit(deposit);
                }
            }
        }

        // Create a job to track this deposit
        Job job = new Job("org.datavaultplatform.worker.tasks.Deposit");
        jobsService.addJob(deposit, job);

        HashMap<String, String> depositProperties = new HashMap<>();
        depositProperties.put(PropNames.DEPOSIT_ID, deposit.getID());
        depositProperties.put(PropNames.BAG_ID, deposit.getBagId());
        depositProperties.put(PropNames.USER_ID, user.getID());
        if (deposit.getNumOfChunks() != 0) {
            logger.debug("Restart num of chunks: " + deposit.getNumOfChunks());
            depositProperties.put(PropNames.NUM_OF_CHUNKS, Integer.toString(deposit.getNumOfChunks()));
        }
        if (deposit.getArchiveDigest() != null) {
            depositProperties.put(PropNames.ARCHIVE_DIGEST, deposit.getArchiveDigest());
        }

        // Deposit and Vault metadata
        // TODO: at the moment we're just serialising the objects to JSON.
        // In future we'll need a more formal schema/representation (e.g. RDF or JSON-LD).

        DepositInfo depositInfo = deposit.convertToResponse();
        depositProperties.put(PropNames.DEPOSIT_METADATA, this.mapper.writeValueAsString(depositInfo));

        VaultInfo vaultInfo = vault.convertToResponse();
        depositProperties.put(PropNames.VAULT_METADATA, this.mapper.writeValueAsString(vaultInfo));

        // External metadata is text from an external system - e.g. XML or JSON
        //depositProperties.put("externalMetadata", externalMetadata);

        ArrayList<String> filestorePaths = new ArrayList<>();
        ArrayList<String> userUploadPaths = new ArrayList<>();

        for (DepositPath path : deposit.getDepositPaths()) {
            if (path.getPathType() == Path.PathType.FILESTORE) {
                logger.debug("Adding Filestore path " + path.getFilePath());
                filestorePaths.add(path.getFilePath());
            } else if (path.getPathType() == Path.PathType.USER_UPLOAD) {
                logger.debug("Adding User upload path " + path.getFilePath());
                userUploadPaths.add(path.getFilePath());
            }
        }

        // extra info from previous attempt for restart
        // get chunks checksums
        // Get encryption IVs
        byte[] tarIVs = (deposit.getEncIV() != null) ? deposit.getEncIV(): null;
        String encTarDigest = (deposit.getEncArchiveDigest() != null) ? deposit.getEncArchiveDigest() : null;
        HashMap<Integer, String> chunksDigest = null;
        HashMap<Integer, byte[]> chunksIVs = null;
        HashMap<Integer, String> encChunksDigests = null;
        if (deposit.getDepositChunks() != null) {
            chunksDigest = new HashMap<>();
            chunksIVs = new HashMap<>();
            encChunksDigests = new HashMap<>();
            List<DepositChunk> depositChunks = deposit.getDepositChunks();
            for (DepositChunk depositChunk : depositChunks) {
                int num = depositChunk.getChunkNum();
                chunksDigest.put(num, depositChunk.getArchiveDigest());
                chunksIVs.put(num, depositChunk.getEncIV());
                encChunksDigests.put(num, depositChunk.getEcnArchiveDigest());
            }
        }
        HashMap<String, String> archiveIDs = null;
        if (deposit.getArchives() != null) {
            archiveIDs = new HashMap<>();
            for (Archive archive : deposit.getArchives()) {
                for (ArchiveStore archiveStore : archiveStores) {
                    if (archive.getArchiveStore().getID().equals(archiveStore.getID())) {
                        archiveIDs.put(archiveStore.getID(), archive.getArchiveId());
                    }
                }
            }
        }

        Task depositTask = new Task(
                job, depositProperties, archiveStores,
                userFileStoreProperties, userFileStoreClasses,
                filestorePaths, userUploadPaths,
                chunksDigest, tarIVs, chunksIVs, encTarDigest, encChunksDigests,
                lastEvent);
        if (archiveIDs != null) {
            depositTask.setRestartArchiveIds(archiveIDs);
        }
        String jsonDeposit = this.mapper.writeValueAsString(depositTask);
        sender.send(jsonDeposit);

        return job;
    }
}
