package org.datavaultplatform.broker.controllers.admin;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.Event;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.response.*;

import org.datavaultplatform.common.task.Task;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiHeader;
import org.jsondoc.core.annotation.ApiHeaders;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.pojo.ApiVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Robin Taylor on 08/03/2016.
 */


@RestController
@Api(name="Admin", description = "Administrator functions")
@Slf4j
public class AdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    private final VaultsService vaultsService;
    private final UsersService usersService;
    private final DepositsService depositsService;
    private final RetrievesService retrievesService;
    private final EventService eventService;
    private final ArchiveStoreService archiveStoreService;
    private final JobsService jobsService;
    private final ExternalMetadataService externalMetadataService;
    private final AuditsService auditsService;
    private final RolesAndPermissionsService permissionsService;
    private final Sender sender;
    private final String optionsDir;
    private final String tempDir;
    private final String bucketName;
    private final String region;
    private final String awsAccessKey;
    private final String awsSecretKey;

    @Autowired
    public AdminController(VaultsService vaultsService, UsersService usersService,
        DepositsService depositsService, RetrievesService retrievesService,
        EventService eventService, ArchiveStoreService archiveStoreService, JobsService jobsService,
        ExternalMetadataService externalMetadataService, AuditsService auditsService,
        RolesAndPermissionsService permissionsService, Sender sender,
        @Value("${optionsDir:#{null}}") String optionsDir,
        @Value("${tempDir:#{null}}") String tempDir,
        @Value("${s3.bucketName:#{null}}") String bucketName,
        @Value("${s3.region:#{null}}") String region,
        @Value("${s3.awsAccessKey:#{null}}") String awsAccessKey,
        @Value("${s3.awsSecretKey:#{null}}") String awsSecretKey) {
        this.vaultsService = vaultsService;
        this.usersService = usersService;
        this.depositsService = depositsService;
        this.retrievesService = retrievesService;
        this.eventService = eventService;
        this.archiveStoreService = archiveStoreService;
        this.jobsService = jobsService;
        this.externalMetadataService = externalMetadataService;
        this.auditsService = auditsService;
        this.permissionsService = permissionsService;
        this.sender = sender;
        this.optionsDir = optionsDir;
        this.tempDir = tempDir;
        this.bucketName = bucketName;
        this.region = region;
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
    }


    @GetMapping(value = "/admin/deposits/count")
    public Integer getDepositsCount(@RequestHeader(HEADER_USER_ID) String userID,
                                  @RequestParam(value = "query", required = false)
                                  @ApiQueryParam(name = "query",
                                          description = "Deposit query field",
                                          required = false) String query) {
        return depositsService.getTotalDepositsCount(userID, query);
    }
  
    public ExternalMetadataService getExternalMetadataService() {
        return externalMetadataService;
    }

    @GetMapping("/admin/deposits")
    public List<DepositInfo> getDepositsAll(@RequestHeader(HEADER_USER_ID) String userID,
                                            @RequestParam(value = "query", required = false)
                                            @ApiQueryParam(name = "query",
                                                    description = "Deposit query field",
                                                    required = false) String query,
                                            @RequestParam(value = "sort", required = false)
                                            @ApiQueryParam(name = "sort",
                                                    description = "Deposit sort field",
                                                    allowedvalues = {"name", "depositSize", "creationTime", "status",
                                                            "depositor", "vaultName", "pureId", "school", "id",
                                                            "vaultId", "owner", "reviewDate"},
                                                    defaultvalue = "creationTime", required = false) String sort,
                                            @RequestParam(value = "order", required = false)
                                            @ApiQueryParam(name = "order",
                                                    description = "Deposit sort order",
                                                    allowedvalues = {"asc", "desc"},
                                                    defaultvalue = "desc", required = false) String order,
                                            @RequestParam(value = "offset", required = false)
                                            @ApiQueryParam(name = "offset",
                                                    description = "Deposit row id",
                                                    defaultvalue = "0", required = false) int offset,
                                            @RequestParam(value = "maxResult", required = false)
                                            @ApiQueryParam(name = "maxResult",
                                                    description = "Number of records",
                                                    required = false) int maxResult) {
        List<DepositInfo> depositResponses = new ArrayList<>();
        List<Deposit> deposits = depositsService.getDeposits(query, userID, sort, order, offset, maxResult);
        for (Deposit deposit : deposits) {
            DepositInfo depositInfo = deposit.convertToResponse();
            User depositor = usersService.getUser(depositInfo.getUserID());
            depositInfo.setUserName(depositor.getFirstname() + " " + depositor.getLastname());
            Vault vault = vaultsService.getVault(depositInfo.getVaultID());
            depositInfo.setVaultName(vault.getName());
            User vaultOwner = permissionsService.getVaultOwner(vault.getID());
            if (vaultOwner != null) {
                depositInfo.setVaultOwnerID(vaultOwner.getID());
                depositInfo.setVaultOwnerName(vaultOwner.getFirstname() + " " + vaultOwner.getLastname());
            }
            if (vault.getDataset() != null) {
                depositInfo.setDatasetID(vault.getDataset().getID());
            }
            depositInfo.setGroupName(vault.getGroup().getName());
            depositInfo.setGroupID(vault.getGroup().getID());
            depositInfo.setVaultReviewDate(vault.getReviewDate().toString());
            if (vault.getDataset() != null) {
                depositInfo.setCrisID(vault.getDataset().getCrisId());
            }
            depositResponses.add(depositInfo);
        }
        return depositResponses;
    }

    @ApiMethod(
            path = "/admin/deposits/data",
            verb = ApiVerb.GET,
            description = "Gets a list of all Vaults",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/admin/deposits/data")
    public DepositsData getDepositsAllData(@RequestHeader(HEADER_USER_ID) String userID,
                                           @RequestParam(value = "sort", required = false)
                                           @ApiQueryParam(name = "sort", description = "Vault sort field") String sort
    ) {

        if (sort == null) sort = "";
        Long recordsTotal = 0L;
        List<DepositInfo> depositResponses = new ArrayList<>();
        List<Deposit> deposits = depositsService.getDeposits("", userID, sort, null, 0, 10);
        if(CollectionUtils.isNotEmpty(deposits)) {
            for (Deposit deposit : deposits) {
                DepositInfo depositInfo = deposit.convertToResponse();
                User depositor = usersService.getUser(depositInfo.getUserID());
                depositInfo.setUserName(depositor.getFirstname() + " " + depositor.getLastname());
                Vault vault = vaultsService.getVault(depositInfo.getVaultID());
                depositInfo.setVaultName(vault.getName());
                User vaultOwner = permissionsService.getVaultOwner(vault.getID());
                if(vaultOwner != null) {
                    depositInfo.setVaultOwnerID(vaultOwner.getID());
                    depositInfo.setVaultOwnerName(vaultOwner.getFirstname() + " " + vaultOwner.getLastname());
                }
                depositInfo.setDatasetID(vault.getDataset().getID());
                depositInfo.setGroupName(vault.getGroup().getName());
                depositInfo.setGroupID(vault.getGroup().getID());
                depositInfo.setVaultReviewDate(vault.getReviewDate().toString());
                depositResponses.add(depositInfo);
            }

        }
        DepositsData data = new DepositsData();
        data.setRecordsTotal(recordsTotal);
        data.setData(depositResponses);
        return data;
    }



    @GetMapping("/admin/retrieves")
    public List<Retrieve> getRetrievesAll(@RequestHeader(HEADER_USER_ID) String userID) {

        return retrievesService.getRetrieves(userID);
    }

    @ApiMethod(
            path = "/admin/vaults",
            verb = ApiVerb.GET,
            description = "Gets a list of all Vaults",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping(value = "/admin/vaults")
    public VaultsData getVaultsAll(@RequestHeader(HEADER_USER_ID) String userID,
                                   @RequestParam(value = "sort", required = false)
                                   @ApiQueryParam(name = "sort", description = "Vault sort field", allowedvalues = {"id", "name", "description", "vaultSize", "user", "policy", "creationTime", "groupID", "reviewDate"}, defaultvalue = "creationTime", required = false) String sort,
                                   @RequestParam(value = "order", required = false)
                                   @ApiQueryParam(name = "order", description = "Vault sort order", allowedvalues = {"asc", "desc"}, defaultvalue = "desc", required = false) String order,
                                   @RequestParam(value = "offset", required = false)
                                   @ApiQueryParam(name = "offset", description = "Vault row id ", defaultvalue = "0", required = false) String offset,
                                   @RequestParam(value = "maxResult", required = false)
                                   @ApiQueryParam(name = "maxResult", description = "Number of records", required = false) String maxResult) {

        if (sort == null) sort = "";
        if (order == null) order = "asc";
        int recordsTotal = 0;
        List<VaultInfo> vaultResponses = new ArrayList<>();
        List<Vault> vaults = vaultsService.getVaults(userID, sort, order,offset, maxResult);
        if(CollectionUtils.isNotEmpty(vaults)) {
            for (Vault vault : vaults) {
                vaultResponses.add(vault.convertToResponse());
            }
            recordsTotal = vaultsService.getTotalNumberOfVaults(userID);
            //Map of project with its size
            Map<String, Long> projectSizeMap = vaultsService.getAllProjectsSize();
            //update project Size in the response
            for(VaultInfo vault: vaultResponses) {
                if(vault.getProjectId() != null) {
                    vault.setProjectSize(projectSizeMap.get(vault.getProjectId()));
                }
            }
        }
        VaultsData data = new VaultsData();
        data.setRecordsTotal(recordsTotal);
        data.setData(vaultResponses);
        return data;
    }

    @GetMapping(value = "/admin/events")
    public List<EventInfo> getEventsAll(@RequestHeader(HEADER_USER_ID) String userID,
                                        @RequestParam(value = "sort", required = false) String sort) {

        List<EventInfo> events = new ArrayList<>();
        for (Event event : eventService.getEvents(sort)) {
            events.add(event.convertToResponse());
        }
        return events;
    }

    @GetMapping("/admin/audits")
    public List<AuditInfo> getAuditsAll(@RequestHeader(HEADER_USER_ID) String userID) {
	    List<AuditInfo> audits = new ArrayList<>();

	    for (Audit audit : auditsService.getAudits()){
            AuditInfo auditInfo = audit.convertToResponse();

            List<AuditChunkStatus> auditChunks = auditsService.getAuditChunkStatus(audit);
            ArrayList<AuditChunkStatusInfo> auditChunksInfo = new ArrayList<>();
            for (AuditChunkStatus auditChunk : auditChunks){
                auditChunksInfo.add(auditChunk.convertToResponse());
            }
            auditInfo.setAuditChunks(auditChunksInfo);

            audits.add(auditInfo);
        }

	    return audits;
    }

    //TODO - looks like this method could do with TLC
    @GetMapping("/admin/deposits/audit")
    public String runDepositAudit(@RequestHeader(HEADER_USER_ID) String userID,
                                HttpServletRequest request) {
        // Make sure it's admin or localhost
        String remoteAddr = request.getRemoteAddr();
        log.info("remoteAddr: {}", remoteAddr);

        // Get oldest Audit
        String query = "";
        String sort = "";
        List<DepositChunk> chunks = depositsService.getChunksForAudit();
        //TODO - doesn't seem right - ignores 'chunks'
        return "Success";
    }
    
    @DeleteMapping("/admin/deposits/{depositID}")
    public ResponseEntity<Object> deleteDeposit(@RequestHeader(HEADER_USER_ID) String userID,
                                                @PathVariable("depositID") String depositID) throws Exception {

        LOGGER.info("Delete deposit with ID : {}", depositID);

        User user = usersService.getUser(userID);
        Deposit deposit = depositsService.getUserDeposit(user, depositID);

        if (user == null) {
            throw new Exception("User '" + userID + "' does not exist");
        }

        List<Job> jobs = deposit.getJobs();
        for (Job job : jobs) {
            if (job.isError() == false && job.getState() != job.getStates().size() - 1) {
                // There's an in-progress job for this deposit
                throw new IllegalArgumentException("Job in-progress for this Deposit");
            }
        }

        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        if (archiveStores.size() == 0) {
            throw new Exception("No configured archive storage");
        }
        LOGGER.info("Delete deposit archiveStores : {}", archiveStores);
        archiveStores = this.addArchiveSpecificOptions(archiveStores);

        // Create a job to track this delete
        Job job = new Job("org.datavaultplatform.worker.tasks.Delete");
        jobsService.addJob(deposit, job);

        // Ask the worker to process the data delete
        try {
            HashMap<String, String> deleteProperties = new HashMap<>();
            deleteProperties.put("depositId", deposit.getID());
            deleteProperties.put("bagId", deposit.getBagId());
            deleteProperties.put("archiveSize", Long.toString(deposit.getArchiveSize()));
            deleteProperties.put("userId", user.getID());
            deleteProperties.put("numOfChunks", Integer.toString(deposit.getNumOfChunks()));
            for (Archive archive : deposit.getArchives()) {
                deleteProperties.put(archive.getArchiveStore().getID(), archive.getArchiveId());
            }

            // Add a single entry for the user file storage
            Map<String, String> userFileStoreClasses = new HashMap<>();
            Map<String, Map<String, String>> userFileStoreProperties = new HashMap<>();
            //userFileStoreClasses.put(storageID, userStore.getStorageClass());
            //userFileStoreProperties.put(storageID, userStore.getProperties());


            Task deleteTask = new Task(
                    job, deleteProperties, archiveStores,
                    userFileStoreProperties, userFileStoreClasses,
                    null, null,
                    null,
                    null, null,
                    null, null, null);
            ObjectMapper mapper = new ObjectMapper();
            String jsonDelete = mapper.writeValueAsString(deleteTask);
            sender.send(jsonDelete);
        } catch (Exception e) {
            LOGGER.error("Exception while deleting a deposit", e);
        }
        return new ResponseEntity<>(HttpStatus.OK);

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
}
