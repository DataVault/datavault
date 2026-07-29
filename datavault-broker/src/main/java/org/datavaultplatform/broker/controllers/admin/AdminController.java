package org.datavaultplatform.broker.controllers.admin;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.response.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Created by Robin Taylor on 08/03/2016.
 */


@RestController
@Tag(name="admin-controller", description = "Administrator functions")
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
    private final AdminDepositService adminDepositService;
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
        RolesAndPermissionsService permissionsService, AdminDepositService adminDepositService,
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
        this.adminDepositService = adminDepositService;
        this.optionsDir = optionsDir;
        this.tempDir = tempDir;
        this.bucketName = bucketName;
        this.region = region;
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
    }


    @Operation(
            summary = "Get the total count of Deposits",
            description = "Retrieves the total number of Deposits in the system, with an optional query filter.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = true, description = "DataVault Broker User ID", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "query", description = "Deposit query field", schema = @Schema(type = "string"))
            }
    )
    @GetMapping(value = "/admin/deposits/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getDepositsCount(@RequestHeader(HEADER_USER_ID) String userId,
                                @RequestParam(value = "query", required = false) String query) {
        return depositsService.getTotalDepositsCount(userId, query);
    }

    @Operation(
            summary = "Get the total count of Retrieves",
            description = "Retrieves the total number of Retrieves in the system, with an optional query filter.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = true, description = "DataVault Broker User ID", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "query", required = false, description = "Retrieve query field", schema = @Schema(type = "string"))
            }
    )
    @GetMapping(value = "/admin/retrieves/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getRetrievesCount(@RequestHeader(HEADER_USER_ID) String userId,
                                 @RequestParam(value = "query", required = false) String query) {
        return retrievesService.getTotalRetrievesCount(userId, query);
    }

    public ExternalMetadataService getExternalMetadataService() {
        return externalMetadataService;
    }

    @Operation(
            summary = "Get a list of all Deposits",
            description = "Retrieves a list of all Deposits in the system, with optional sorting and filtering.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = true, description = "DataVault Broker User ID", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "query", required = false, description = "Deposit query field", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "sort", required = false, description = "Deposit sort field", schema = @Schema(type = "string", allowableValues = {"name", "depositSize", "creationTime", "status", "depositor", "vaultName", "pureId", "school", "id", "vaultId", "owner", "reviewDate"}, defaultValue = "creationTime")),
                    @Parameter(in = ParameterIn.QUERY, name = "order", required = false, description = "Deposit sort order", schema = @Schema(type = "string", allowableValues = {"asc", "desc"}, defaultValue = "desc")),
                    @Parameter(in = ParameterIn.QUERY, name = "offset", required = false, description = "Deposit row id", schema = @Schema(type = "integer", format = "int32", defaultValue = "0")),
                    @Parameter(in = ParameterIn.QUERY, name = "maxResult", required = false, description = "Number of records", schema = @Schema(type = "integer", format = "int32"))
            }
    )
    @GetMapping(value = "/admin/deposits", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DepositInfo> getDepositsAll(@RequestHeader(HEADER_USER_ID) String userId,
                                            @RequestParam(value = "query", required = false) String query,
                                            @RequestParam(value = "sort", required = false) String sort,
                                            @RequestParam(value = "order", required = false) String order,
                                            @RequestParam(value = "offset", required = false) int offset,
                                            @RequestParam(value = "maxResult", required = false) int maxResult) {
        List<DepositInfo> depositResponses = new ArrayList<>();
        List<Deposit> deposits = depositsService.getDeposits(query, userId, sort, order, offset, maxResult);
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

    @Operation(
            summary = "Gets a list of all Vaults",
            description = "Retrieves a list of all Vaults in the system, with optional sorting.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = true, description = "DataVault Broker User ID", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "sort", description = "Vault sort field", schema = @Schema(type = "string"))
            }
    )
    @GetMapping(value = "/admin/deposits/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public DepositsData getDepositsAllData(@RequestHeader(HEADER_USER_ID) String userId,
                                           @RequestParam(value = "sort", required = false) String sort
    ) {

        if (sort == null) sort = "";
        long recordsTotal = 0L;
        List<DepositInfo> depositResponses = new ArrayList<>();
        List<Deposit> deposits = depositsService.getDeposits("", userId, sort, null, 0, 10);
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

    @Operation(
            summary = "Get a list of all Retrieves",
            description = "Retrieves a list of all Retrieves in the system, with optional sorting and filtering.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = true, description = "DataVault Broker User ID", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "query", description = "Retrieve query field", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "sort", description = "Retrieve sort field", schema = @Schema(type = "string", allowableValues = {"timestamp"}, defaultValue = "timestamp")),
                    @Parameter(in = ParameterIn.QUERY, name = "order", description = "Retrieve sort order", schema = @Schema(type = "string", allowableValues = {"asc", "desc"}, defaultValue = "desc")),
                    @Parameter(in = ParameterIn.QUERY, name = "offset", description = "Retrieve row id", schema = @Schema(type = "integer", format = "int32", defaultValue = "0")),
                    @Parameter(in = ParameterIn.QUERY, name = "maxResult", description = "Number of records", schema = @Schema(type = "integer", format = "int32"))
            }
    )
    @GetMapping(value = "/admin/retrieves", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Retrieve> getRetrievesAll(@RequestHeader(HEADER_USER_ID) String userId,
                                          @RequestParam(value = "query", required = false) String query,
                                          @RequestParam(value = "sort", required = false) String sort,
                                          @RequestParam(value = "order", required = false) String order,
                                          @RequestParam(value = "offset", required = false) int offset,
                                          @RequestParam(value = "maxResult", required = false) int maxResult) {
        List<Retrieve> retrieves = retrievesService.getRetrieves(query, userId, sort, order, offset, maxResult);

        return retrieves;
    }


    @Operation(
            summary = "Gets a list of all Vaults",
            description = "Retrieves a list of all Vaults in the system, with optional sorting and filtering.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = true, description = "DataVault Broker User ID", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "sort", description = "Vault sort field", schema = @Schema(type = "string", allowableValues = {"id", "name", "description", "vaultSize", "user", "policy", "creationTime", "groupID", "reviewDate"}, defaultValue = "creationTime")),
                    @Parameter(in = ParameterIn.QUERY, name = "order", description = "Vault sort order", schema = @Schema(type = "string", allowableValues = {"asc", "desc"}, defaultValue = "desc")),
                    @Parameter(in = ParameterIn.QUERY, name = "offset", description = "Vault row id", schema = @Schema(type = "string", defaultValue = "0")),
                    @Parameter(in = ParameterIn.QUERY, name = "maxResult", description = "Number of records", schema = @Schema(type = "string"))
            }
    )
    @GetMapping(value = "/admin/vaults", produces = MediaType.APPLICATION_JSON_VALUE)
    public VaultsData getVaultsAll(@Parameter(hidden = true) @RequestHeader(HEADER_USER_ID) String userId,
                                   @Parameter(hidden = true) @RequestParam(value = "sort", required = false) String sort,
                                   @Parameter(hidden = true) @RequestParam(value = "order", required = false) String order,
                                   @Parameter(hidden = true) @RequestParam(value = "offset", required = false) String offset,
                                   @Parameter(hidden = true) @RequestParam(value = "maxResult", required = false) String maxResult) {

        if (sort == null) sort = "";
        if (order == null) order = "asc";
        int recordsTotal = 0;
        List<VaultInfo> vaultResponses = new ArrayList<>();
        List<Vault> vaults = vaultsService.getVaults(userId, sort, order,offset, maxResult);
        if(CollectionUtils.isNotEmpty(vaults)) {
            for (Vault vault : vaults) {
                vaultResponses.add(vault.convertToResponse());
            }
            recordsTotal = vaultsService.getTotalNumberOfVaults(userId);
            Map<String, Long> projectSizeMap = vaultsService.getAllProjectsSize();
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

    @Operation(
            summary = "Get a list of all Events",
            description = "Retrieves a list of all Events in the system, with optional sorting.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = true, description = "DataVault Broker User ID", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "sort", description = "Event sort field", schema = @Schema(type = "string"))
            }
    )
    @GetMapping(value = "/admin/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EventInfo> getEventsAll(@RequestHeader(HEADER_USER_ID) String userId,
                                        @RequestParam(value = "sort", required = false) String sort) {

        List<EventInfo> events = new ArrayList<>();
        for (Event event : eventService.getEvents(sort)) {
            events.add(event.convertToResponse());
        }
        return events;
    }

    @Operation(
            summary = "Get a list of all Audits",
            description = "Retrieves a list of all Audits in the system.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = true, description = "DataVault Broker User ID", schema = @Schema(type = "string"))
            }
    )
    @GetMapping(value = "/admin/audits", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditInfo> getAuditsAll(@RequestHeader(HEADER_USER_ID) String userId) {
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

    @Operation(
            summary = "Run a Deposit audit",
            description = "Triggers a Deposit audit process.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = false, description = "DataVault Broker User ID", schema = @Schema(type = "string"))
            }
    )
    @GetMapping(value = "/admin/deposits/audit", produces = MediaType.TEXT_PLAIN_VALUE)
    public String runDepositAudit(@RequestHeader(value = HEADER_USER_ID, required = false) String userId,
                                  HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        log.info("remoteAddr: {}", remoteAddr);

        String query = "";
        String sort = "";
        List<DepositChunk> chunks = depositsService.getChunksForAudit();
        return "Success";
    }
    
    @Operation(
            summary = "Delete a Deposit",
            description = "Deletes a Deposit from the system.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = HEADER_USER_ID, required = true, description = "DataVault Broker User ID", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.PATH, name = "depositID", required = true, description = "The ID of the Deposit to delete", schema = @Schema(type = "string"))
            }
    )
    @DeleteMapping("/admin/deposits/{depositId}")
    public ResponseEntity<Void> deleteDeposit(@RequestHeader(HEADER_USER_ID) String userId,
                                                @PathVariable String depositId) throws Exception {

        LOGGER.info("Delete deposit with ID : {}", depositId);

        User user = usersService.getUser(userId);
        Deposit deposit = depositsService.getUserDeposit(user, depositId);

        if (user == null) {
            throw new Exception("User '" + userId + "' does not exist");
        }
        adminDepositService.deleteDeposit(deposit, user);
        return new ResponseEntity<>(HttpStatus.OK);
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
}
