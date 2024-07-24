package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
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
import org.datavaultplatform.common.util.RetrievedChunks;
import org.datavaultplatform.common.util.StoredChunks;
import org.jsondoc.core.annotation.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.*;


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
    private final int userFsRetryMaxAttempts;
    private final long userFsRetryDelaySeconds1;
    private final long userFsRetryDelaySeconds2;

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
        @Value("${userFsRetryMaxAttempts:10}") int userFsRetryMaxAttempts,
        @Value("${userFsRetryDelaySeconds1:60}") long userFsRetryDelaySeconds1,
        @Value("${userFsRetryDelaySeconds2:60}") long userFsRetryDelaySeconds2,
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
        this.userFsRetryMaxAttempts = userFsRetryMaxAttempts;
        this.userFsRetryDelaySeconds1 = userFsRetryDelaySeconds1;
        this.userFsRetryDelaySeconds2 = userFsRetryDelaySeconds2;
        this.mapper = mapper;
    }



    @GetMapping("/deposits/{depositid}")
    public DepositInfo getDeposit(@RequestHeader(HEADER_USER_ID) String userID,
                                  @PathVariable("depositid") String depositID) throws Exception {

        User user = getUser(userID);
        return getUserDeposit(user, depositID).convertToResponse();
    }

    @PostMapping("/deposits")
    public ResponseEntity<DepositInfo> addDeposit(@RequestHeader(HEADER_USER_ID) String userID,
                                             @RequestBody CreateDeposit createDeposit) throws Exception {

        User user = getUser(userID);

        Deposit deposit = new Deposit();

        Vault vault = vaultsService.getUserVault(user, createDeposit.getVaultID());

        deposit.setName(createDeposit.getName());
        deposit.setDescription(createDeposit.getDescription());
        deposit.setHasPersonalData("yes".equalsIgnoreCase(createDeposit.getHasPersonalData()));
        deposit.setPersonalDataStatement(createDeposit.getPersonalDataStatement());
        deposit.setDepositPaths(new ArrayList<>());

        deposit.setUser(user);

        // Add the file upload path
        DepositPath fileUploadPath = new DepositPath(deposit, createDeposit.getFileUploadHandle(), Path.PathType.USER_UPLOAD);
        deposit.getDepositPaths().add(fileUploadPath);

        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        if (archiveStores.isEmpty()) {
            throw new Exception("No configured archive storage");
        }

        addArchiveSpecificOptions(archiveStores);

        logger.info("Deposit File Path: ");
        for (DepositPath dPath : deposit.getDepositPaths()){
            logger.info("\t- {}", dPath.getFilePath());
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

        User user = getUser(userID);
        Deposit deposit = getUserDeposit(user, depositID);

        List<FileFixity> manifest = new ArrayList<>();
        
        if (deposit.getStatus() == Deposit.Status.COMPLETE) {
            manifest = metadataService.getManifest(deposit.getBagId());
        }
        
        return manifest;
    }

    @GetMapping("/deposits/{depositid}/events")
    public List<EventInfo> getDepositEvents(@RequestHeader(HEADER_USER_ID) String userID,
                                            @PathVariable("depositid") String depositID) throws Exception {

        User user = getUser(userID);
        Deposit deposit = getUserDeposit(user, depositID);

        List<EventInfo> events = new ArrayList<>();
        
        for (Event event : deposit.getEvents()) {
            events.add(event.convertToResponse());
        }
        
        return events;
    }

    @GetMapping("/deposits/{depositid}/retrieves")
    public List<Retrieve> getDepositRetrieves(@RequestHeader(HEADER_USER_ID) String userID,
                                            @PathVariable("depositid") String depositID) throws Exception {

        User user = getUser(userID);
        Deposit deposit = getUserDeposit(user, depositID);

        return deposit.getRetrieves();
    }

    @GetMapping("/deposits/{depositid}/jobs")
    public List<Job> getDepositJobs(@RequestHeader(HEADER_USER_ID) String userID,
                                    @PathVariable("depositid") String depositID) throws Exception {

        User user = getUser(userID);
        Deposit deposit = getUserDeposit(user, depositID);

        return deposit.getJobs();
    }

    @PostMapping( "/deposits/{depositid}/retrieve")
    public Boolean retrieveDeposit(@RequestHeader(HEADER_USER_ID) String userID,
                                  @PathVariable("depositid") String depositID,
                                  @RequestBody Retrieve retrieve) throws Exception {
        User user = getUser(userID);
        Deposit deposit = getUserDeposit(user, depositID);
        return runRetrieveDeposit(user, deposit, retrieve, null);
    }

    /*
    Added this method to easier testing of retrieve restarts with just a retrieve id
     */
    @PostMapping( "/retrieve/{retrieveId}/restart")
    public boolean retrieveRestart(@PathVariable("retrieveId") String retrieveId) throws Exception {
        Retrieve retrieve = getRetrieve(retrieveId);
        Deposit deposit = retrieve.getDeposit();
        User user = retrieve.getUser();
        return retrieveDepositRestart(user.getID(), deposit.getID(), retrieveId );
    }

    @PostMapping( "/deposits/{depositId}/retrieve/{retrieveId}/restart")
    public boolean retrieveDepositRestart(@RequestHeader(HEADER_USER_ID) String userID,
                                   @PathVariable("depositId") String depositId,
                                   @PathVariable("retrieveId") String retrieveId) throws Exception {
        User user = adminService.ensureAdminUser(userID);
        Deposit deposit = getUserDeposit(user, depositId);
        Assert.isTrue(deposit.getNonRestartJobId() != null, "The non restart job id should not be null");
        Retrieve retrieve = getRetrieve(retrieveId);
        user = retrieve.getUser();
        boolean retrieveMatchesDeposit = retrieve.getDeposit().equals(deposit);
        Assert.isTrue(retrieveMatchesDeposit, "The depositId[%s] does not match retrieve's depositId[%s]".formatted(depositId, retrieve.getDeposit().getID()));
        Event lastEvent = depositsService.getLastNotFailedRetrieveEvent(depositId, retrieveId);
        Assert.isTrue(lastEvent != null, "There is no last event - so can't restart retrieve");
        return runRetrieveDeposit(user, deposit, retrieve, lastEvent);
    }

    protected boolean runRetrieveDeposit(User user, Deposit deposit, Retrieve retrieve, Event lastEvent) throws Exception {
        
        deposit = depositsService.getDepositForRetrieves(deposit.getID());
        
        Assert.isTrue(user != null, "The user cannot be null");
        Assert.isTrue(deposit != null, "The deposit cannot be null");
        Assert.isTrue(retrieve != null, "The retrieve cannot be null");

        if (lastEvent == null) {
            retrieve.setUser(user);
            checkForInProgressJob(deposit.getJobs());
        }

        StorageIdAndRetrievePath storageIdAndRetrievePath = StorageIdAndRetrievePath.fromFullPath(retrieve.getRetrievePath());
        String storageID = storageIdAndRetrievePath.storageID();
        String retrievePath = storageIdAndRetrievePath.retrievePath();
        
        // Fetch the ArchiveStore that is flagged for retrieval. We store it in a list as the Task parameters require a list.
        ArchiveStore archiveStore = archiveStoreService.getForRetrieval();
        Assert.isTrue(archiveStore != null, "NO ARCHIVE STORES CONFIGURED FOR RETRIEVAL");
        List<ArchiveStore> archiveStores = List.of(archiveStore);
        addArchiveSpecificOptions(archiveStores);

        // Find the Archive that matches the ArchiveStore.
        String archiveID = getDepositArchive(deposit, archiveStore);
        
        FileStore userStore = getUserStore(user, storageID);

        // Check the source file path is valid
        if (!filesService.validPath(retrievePath, userStore)) {
            throw new IllegalArgumentException("Path '" + retrievePath + "' is invalid");
        }

        // Create a job to track this retrieve
        Job job = new Job(Job.TASK_CLASS_RETRIEVE);
        jobsService.addJob(deposit, job);

        boolean isRestart = lastEvent != null;

        if (!isRestart) {
            deposit.setNonRestartJobId(job.getID());
            depositsService.updateDeposit(deposit);
            // Add the retrieve object
            retrievesService.addRetrieve(retrieve, deposit, retrieve.getRetrievePath());
        }

        // Ask the worker to process the data retrieve
        try {
            HashMap<String, String> retrieveProperties = getRetrieveProperties(user, deposit, retrieve, retrievePath, archiveID);

            // Add a single entry for the user file storage
            Map<String, String> userFileStoreClasses = Map.of(storageID, userStore.getStorageClass());
            Map<String, Map<String, String>> userFileStoreProperties = Map.of(storageID, userStore.getProperties());
            
            // get chunks checksums
            var chunksDigest = new HashMap<Integer,String>();
            deposit.getDepositChunks().forEach(dc -> chunksDigest.put(dc.getChunkNum(), dc.getArchiveDigest()));
            
            // Get encryption IVs
            byte[] tarIVs = deposit.getEncIV();
            var chunksIVs = new HashMap<Integer,byte[]>();
            deposit.getDepositChunks().forEach(dc -> chunksIVs.put(dc.getChunkNum(), dc.getEncIV()));
            
            // Get encrypted digests
            String encTarDigest = deposit.getEncArchiveDigest();
            var encChunksDigests = new HashMap<Integer, String>();
            deposit.getDepositChunks().forEach(dc -> encChunksDigests.put(dc.getChunkNum(), dc.getEcnArchiveDigest()));

            RetrievedChunks retrievedChunks = depositsService.getChunksRetrieved(deposit.getID(), retrieve.getID());
            String retrievedChunksJson = RetrievedChunks.toJson(retrievedChunks);
            // for restarts
            retrieveProperties.put(PropNames.NON_RESTART_JOB_ID, deposit.getNonRestartJobId());
            retrieveProperties.put(PropNames.DEPOSIT_CHUNKS_RETRIEVED, retrievedChunksJson);
            
            Task retrieveTask = new Task(
                    job, retrieveProperties, archiveStores, 
                    userFileStoreProperties, userFileStoreClasses, 
                    null, null, 
                    chunksDigest,
                    tarIVs, chunksIVs,
                    encTarDigest, encChunksDigests, lastEvent);
            String jsonRetrieve = mapper.writeValueAsString(retrieveTask);

            sender.send(jsonRetrieve, isRestart);
        } catch (Exception e) {
            logger.error("unexpected exception", e);
        }

        // Check the retention policy of the newly created vault
        vaultsService.checkRetentionPolicy(deposit.getVault().getID());

        return true;
    }

    protected String getDepositArchive(Deposit deposit, ArchiveStore archiveStore) throws Exception {
        String archiveID = null;
        if (deposit.getArchives() != null) {
            for (Archive archive : deposit.getArchives()) {
                if( archive != null &&
                        archive.getArchiveStore() != null &&
                        archive.getArchiveStore().getID().equals(archiveStore.getID())) {
                    archiveID = archive.getArchiveId();
                }
            }
        }

        // Worth checking that we found a matching Archive for the ArchiveStore.
        if (archiveID == null) {
            throw new Exception("No valid archive for retrieval");
        }
        return archiveID;
    }

    protected void checkForInProgressJob(List<Job> jobs) {
        if (jobs == null) {
            return;
        }
        for (Job job : jobs) {
            if (!job.isError() && job.getState() != job.getStates().size() - 1) {
                // There's an in-progress job for this deposit
                throw new IllegalArgumentException("Job in-progress for this Deposit");
            }
        }
    }

    private ArchiveStore getArchiveStore() {
        ArchiveStore archiveStore = archiveStoreService.getForRetrieval();
        List<ArchiveStore> archiveStores = new ArrayList<>();
        archiveStores.add(archiveStore);
        this.addArchiveSpecificOptions(archiveStores);
        return  archiveStore;
    }

    private HashMap<String,String> getRetrieveProperties(User user, Deposit deposit, Retrieve retrieve, String retrievePath, String archiveID) {
        var result = new HashMap<String,String>();
        result.put(PropNames.DEPOSIT_ID, deposit.getID());
        result.put(PropNames.DEPOSIT_CREATION_DATE, DateTimeUtils.formatDateBasicISO(deposit.getCreationTime()));
        result.put(PropNames.RETRIEVE_ID, retrieve.getID());
        result.put(PropNames.BAG_ID, deposit.getBagId());
        result.put(PropNames.RETRIEVE_PATH, retrievePath); // No longer the absolute path
        result.put(PropNames.ARCHIVE_ID, archiveID);
        result.put(PropNames.ARCHIVE_SIZE, Long.toString(deposit.getArchiveSize()));
        result.put(PropNames.USER_ID, user.getID());
        result.put(PropNames.ARCHIVE_DIGEST, deposit.getArchiveDigest());
        result.put(PropNames.ARCHIVE_DIGEST_ALGORITHM, deposit.getArchiveDigestAlgorithm());
        result.put(PropNames.NUM_OF_CHUNKS, Integer.toString(deposit.getNumOfChunks()));
        result.put(PropNames.USER_FS_RETRY_MAX_ATTEMPTS, String.valueOf(this.userFsRetryMaxAttempts));
        result.put(PropNames.USER_FS_RETRY_DELAY_MS_1, String.valueOf(this.userFsRetryDelaySeconds1));
        result.put(PropNames.USER_FS_RETRY_DELAY_MS_2, String.valueOf(this.userFsRetryDelaySeconds2));
        return result;
    }

    private FileStore getUserStore(User user, String storageID) {
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
        return userStore;
    }

    private void addArchiveSpecificOptions(List<ArchiveStore> archiveStores) {
        if (archiveStores == null || archiveStores.isEmpty()) {
            return;
        }
        for (ArchiveStore archiveStore : archiveStores) {
            final HashMap<String, String> asProps = archiveStore.getProperties();
            if (archiveStore.isTivoliStorageManager()) {
                if (StringUtils.isNotEmpty(optionsDir)) {
                    asProps.put(PropNames.OPTIONS_DIR, optionsDir);
                }
                if (StringUtils.isNotEmpty(tempDir)) {
                    asProps.put(PropNames.TEMP_DIR, tempDir);
                }
                if (StringUtils.isNotEmpty(tsmRetryTime)) {
                    asProps.put(PropNames.TSM_RETRY_TIME, tsmRetryTime);
                }
                if (StringUtils.isNotEmpty(tsmMaxRetries)) {
                    asProps.put(PropNames.TSM_MAX_RETRIES, tsmMaxRetries);
                }
                if (StringUtils.isNotEmpty(tsmReverse)) {
                    asProps.put(PropNames.TSM_REVERSE, tsmReverse);
                }
                archiveStore.setProperties(asProps);
            }

            if (archiveStore.isOracle()) {
                if (StringUtils.isEmpty(occRetryTime)) {
                    asProps.put(PropNames.OCC_RETRY_TIME, occRetryTime);
                }
                if (StringUtils.isEmpty(occMaxRetries)) {
                    asProps.put(PropNames.OCC_MAX_RETRIES, occMaxRetries);
                }
                if (StringUtils.isEmpty(ociBucketName)) {
                    asProps.put(PropNames.OCI_BUCKET_NAME, ociBucketName);
                }
                if (StringUtils.isNotEmpty(ociNameSpace)) {
                    asProps.put(PropNames.OCI_NAME_SPACE, ociNameSpace);
                }
                archiveStore.setProperties(asProps);
            }

            if (archiveStore.isAmazonS3()) {
                if (StringUtils.isNotBlank(bucketName)) {
                    asProps.put(PropNames.AWS_S3_BUCKET_NAME, bucketName);
                }
                if (StringUtils.isNotBlank(region)) {
                    asProps.put(PropNames.AWS_S3_REGION, region);
                }
                if (StringUtils.isNotBlank(awsAccessKey)) {
                    asProps.put(PropNames.AWS_ACCESS_KEY, awsAccessKey);
                }
                if (StringUtils.isNotBlank(awsSecretKey)) {
                    asProps.put(PropNames.AWS_SECRET_KEY, awsSecretKey);
                }

                archiveStore.setProperties(asProps);
            }
        }
    }

    @PostMapping("/deposits/{depositid}/restart")
    public Deposit restartDeposit(@RequestHeader(HEADER_USER_ID) String userID,
                                   @PathVariable("depositid") String depositID) throws Exception{

        User user = adminService.ensureAdminUser(userID);
        Deposit deposit = getUserDeposit(user, depositID);

        List<FileStore> userStores = user.getFileStores();
        logger.info("There is {}user stores.", userStores.size());

        ArrayList<String> paths = new ArrayList<>();
        for(DepositPath dPath : deposit.getDepositPaths()){
            if(dPath.getPathType() == Path.PathType.FILESTORE) {
                paths.add(dPath.getFilePath());
            }
        }
        logger.info("There is {} deposit path", paths.size());
        if (paths.isEmpty()) {
            throw new Exception("There are no file paths for restarted deposit - Exiting");
        }
        // Get last Deposit Event
        Event lastEvent = depositsService.getLastNotFailedDepositEvent(deposit.getID());
        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        addArchiveSpecificOptions(archiveStores);
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

                StorageIdAndRetrievePath rec = StorageIdAndRetrievePath.fromFullPath(path);
                String storageID = rec.storageID();
                String storagePath = rec.retrievePath();

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
                    logger.info("Add deposit path: {}", path);
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
        Job job = new Job(Job.TASK_CLASS_DEPOSIT);
        jobsService.addJob(deposit, job);

        boolean isRestart = lastEvent != null;
        if (!isRestart) {
            deposit.setNonRestartJobId(job.getID());
            depositsService.updateDeposit(deposit);
        }

        HashMap<String, String> depositProperties = new HashMap<>();
        depositProperties.put(PropNames.DEPOSIT_ID, deposit.getID());
        depositProperties.put(PropNames.BAG_ID, deposit.getBagId());
        depositProperties.put(PropNames.USER_ID, user.getID());

        if (deposit.getNumOfChunks() != 0) {
            logger.debug("Restart num of chunks: {}", deposit.getNumOfChunks());
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
                logger.debug("Adding Filestore path {}", path.getFilePath());
                filestorePaths.add(path.getFilePath());
            } else if (path.getPathType() == Path.PathType.USER_UPLOAD) {
                logger.debug("Adding User upload path {}", path.getFilePath());
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
        depositProperties.put(PropNames.USER_FS_RETRY_MAX_ATTEMPTS, String.valueOf(this.userFsRetryMaxAttempts));
        depositProperties.put(PropNames.USER_FS_RETRY_DELAY_MS_1, String.valueOf(this.userFsRetryDelaySeconds1));
        depositProperties.put(PropNames.USER_FS_RETRY_DELAY_MS_2, String.valueOf(this.userFsRetryDelaySeconds2));

        StoredChunks storedChunks = depositsService.getChunksStored(deposit.getID());
        String storedChunksJson = mapper.writeValueAsString(storedChunks);
        // for restarts        
        depositProperties.put(PropNames.NON_RESTART_JOB_ID, deposit.getNonRestartJobId());
        depositProperties.put(PropNames.DEPOSIT_CHUNKS_STORED, storedChunksJson);

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
        sender.send(jsonDeposit, isRestart);

        return job;
    }
    private User getUser(String userID) throws Exception {
        User user = usersService.getUser(userID);
        if (user == null) {
            throw new Exception("User '" + userID + "' does not exist");
        }
        return user;
    }
    private Deposit getUserDeposit(User user, String depositID) throws  Exception {
        Deposit deposit = depositsService.getUserDeposit(user, depositID);
        if (deposit == null) {
            throw new Exception("Deposit '" + depositID + "' does not exist");
        }  
        return deposit;
    }
    private Retrieve getRetrieve(String retrieveId) throws Exception {
        Retrieve retrieve = retrievesService.getRetrieve(retrieveId);
        if(retrieve == null){
            throw new Exception("Retrieve '" + retrieveId + "' does not exist");
        }
        return retrieve;
    }
    
    private Deposit getDeposit(String depositId) throws Exception {
        Deposit deposit = depositsService.getDeposit(depositId);
        if(deposit == null){
            throw new Exception("Deposit '" + depositId + "' does not exist");
        }
        return deposit;
    }
}
