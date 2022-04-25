package org.datavaultplatform.broker.controllers.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Robin Taylor on 08/03/2016.
 */


@RestController
@Api(name="Admin", description = "Administrator functions")
public class AdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    private VaultsService vaultsService;
    private UsersService usersService;
    private DepositsService depositsService;
    private RetrievesService retrievesService;
    private EventService eventService;
    private ArchiveStoreService archiveStoreService;
    private JobsService jobsService;
    private ExternalMetadataService externalMetadataService;
    private AuditsService auditsService;
    private RolesAndPermissionsService permissionsService;
    private Sender sender;
    private String optionsDir;
    private String tempDir;
    private String bucketName;
    private String region;
    private String awsAccessKey;
    private String awsSecretKey;

    public void setOptionsDir(String optionsDir) {
        this.optionsDir = optionsDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
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

    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }

    public void setRetrievesService(RetrievesService retrievesService) {
        this.retrievesService = retrievesService;
    }

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    public void setAuditsService(AuditsService auditsService) {
        this.auditsService = auditsService;
    }

    public void setPermissionsService(RolesAndPermissionsService rolesAndPermissionsService) {
        this.permissionsService = rolesAndPermissionsService;
    }

    public void setArchiveStoreService(ArchiveStoreService archiveStoreService) {
        this.archiveStoreService = archiveStoreService;
    }

    public void setJobsService(JobsService jobsService) {
        this.jobsService = jobsService;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    @RequestMapping(value = "/admin/deposits/count", method = RequestMethod.GET)
    public Integer getDepositsAll(@RequestHeader(value = "X-UserID", required = true) String userID,
                                  @RequestParam(value = "query", required = false)
                                  @ApiQueryParam(name = "query",
                                          description = "Deposit query field",
                                          required = false) String query) throws Exception {
        return depositsService.getTotalDepositsCount(userID, query);
    }
  
    public ExternalMetadataService getExternalMetadataService() {
        return externalMetadataService;
    }

    public void setExternalMetadataService(ExternalMetadataService externalMetadataService) {
        this.externalMetadataService = externalMetadataService;
    }

    @RequestMapping(value = "/admin/deposits", method = RequestMethod.GET)
    public List<DepositInfo> getDepositsAll(@RequestHeader(value = "X-UserID", required = true) String userID,
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
                                                    required = false) int maxResult) throws Exception {
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
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/admin/deposits/data", method = RequestMethod.GET)
    public DepositsData getDepositsAllData(@RequestHeader(value = "X-UserID", required = true) String userID,
                                           @RequestParam(value = "sort", required = false)
                                           @ApiQueryParam(name = "sort", description = "Vault sort field") String sort
    ) throws Exception {

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



    @RequestMapping(value = "/admin/retrieves", method = RequestMethod.GET)
    public List<Retrieve> getRetrievesAll(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

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
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/admin/vaults", method = RequestMethod.GET)
    public VaultsData getVaultsAll(@RequestHeader(value = "X-UserID", required = true) String userID,
                                   @RequestParam(value = "sort", required = false)
                                   @ApiQueryParam(name = "sort", description = "Vault sort field", allowedvalues = {"id", "name", "description", "vaultSize", "user", "policy", "creationTime", "groupID", "reviewDate"}, defaultvalue = "creationTime", required = false) String sort,
                                   @RequestParam(value = "order", required = false)
                                   @ApiQueryParam(name = "order", description = "Vault sort order", allowedvalues = {"asc", "desc"}, defaultvalue = "desc", required = false) String order,
                                   @RequestParam(value = "offset", required = false)
                                   @ApiQueryParam(name = "offset", description = "Vault row id ", defaultvalue = "0", required = false) String offset,
                                   @RequestParam(value = "maxResult", required = false)
                                   @ApiQueryParam(name = "maxResult", description = "Number of records", required = false) String maxResult) throws Exception {

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

    @RequestMapping(value = "/admin/events", method = RequestMethod.GET)
    public List<EventInfo> getEventsAll(@RequestHeader(value = "X-UserID", required = true) String userID,
                                        @RequestParam(value = "sort", required = false) String sort) throws Exception {

        List<EventInfo> events = new ArrayList<>();
        for (Event event : eventService.getEvents(sort)) {
            events.add(event.convertToResponse());
        }
        return events;
    }

    @RequestMapping(value = "/admin/audits", method = RequestMethod.GET)
    public List<AuditInfo> getAuditsAll(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {
	    List<AuditInfo> audits = new ArrayList<>();

	    for (Audit audit : auditsService.getAudits()){
            AuditInfo auditInfo = audit.convertToResponse();

            List<AuditChunkStatus> auditChunks = auditsService.getAuditChunkStatus(audit);
            ArrayList<AuditChunkStatusInfo> auditChunksInfo = new ArrayList<AuditChunkStatusInfo>();
            for (AuditChunkStatus auditChunk : auditChunks){
                auditChunksInfo.add(auditChunk.convertToResponse());
            }
            auditInfo.setAuditChunks(auditChunksInfo);

            audits.add(auditInfo);
        }

	    return audits;
    }

    @RequestMapping(value = "/admin/deposits/audit", method = RequestMethod.GET)
    public String runDepositAudit(@RequestHeader(value = "X-UserID", required = true) String userID,
                                HttpServletRequest request) throws Exception{
        // Make sure it's admin or localhost
        String remoteAdrr = request.getRemoteAddr();
        System.out.println("remoteAdrr: "+remoteAdrr);

        // Get oldest Audit
        String query = "";
        String sort = "";
        List<DepositChunk> chunks = depositsService.getChunksForAudit();

        return "Success";
    }
    
    @RequestMapping(value = "/admin/deposits/{depositID}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> deleteDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
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
