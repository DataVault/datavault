package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.EventService;
import org.datavaultplatform.broker.services.PendingVaultsService;
import org.datavaultplatform.broker.services.RetrievesService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Retrieve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@CrossOrigin
@Tag(name="statistics-controller", description = "System statistics")
public class StatisticsController {

    private final VaultsService vaultsService;
    private final PendingVaultsService pendingVaultsService;
    private final DepositsService depositsService;
    private final RetrievesService retrievesService;
    private final EventService eventService;

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    public StatisticsController(VaultsService vaultsService,
        PendingVaultsService pendingVaultsService, DepositsService depositsService,
        RetrievesService retrievesService, EventService eventService) {
        this.vaultsService = vaultsService;
        this.pendingVaultsService = pendingVaultsService;
        this.depositsService = depositsService;
        this.retrievesService = retrievesService;
        this.eventService = eventService;
    }

    @Operation(
            summary = "Get the total count of Vaults",
            description = "Retrieves the total number of Vaults in the system."
    )
    @GetMapping(value = "/statistics/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getVaultsCount( @RequestHeader(HEADER_USER_ID) String userId) {

        return vaultsService.count(userId);
    }
    
    @Operation(
            summary = "Get the total count of Pending Vaults",
            description = "Retrieves the total number of Pending Vaults in the system."
    )
    @GetMapping(value = "/statistics/pendingVaultsTotal", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getTotalNumberOfPendingVaults() {

        return pendingVaultsService.getTotalNumberOfPendingVaults();
    }

    @Operation(
            summary = "Get the total size of Vaults",
            description = "Retrieves the combined size of all Vaults in the system."
    )
    @GetMapping(value = "/statistics/size", produces = MediaType.APPLICATION_JSON_VALUE)
    public Long getVaultsSize( @RequestHeader(HEADER_USER_ID) String userId) {

        return depositsService.size(userId);
    }

    @Operation(
            summary = "Get the total count of Deposits",
            description = "Retrieves the total number of Deposits in the system."
    )
    @GetMapping(value = "/statistics/depositcount", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getDepositsCount( @RequestHeader(HEADER_USER_ID) String userId) {

        return depositsService.count(userId);
    }

    @Operation(
            summary = "Get the count of Deposits in progress",
            description = "Retrieves the number of Deposits currently in progress."
    )
    @GetMapping(value = "/statistics/depositinprogresscount", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getDepositsInProgressCount( @RequestHeader(HEADER_USER_ID) String userId) {

        return depositsService.inProgressCount(userId);
    }

    @Operation(
            summary = "Get the total count of Retrieves",
            description = "Retrieves the total number of Retrieves in the system."
    )
    @GetMapping(value = "/statistics/retrievecount", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getRetrievesCount( @RequestHeader(HEADER_USER_ID) String userId) {

        return retrievesService.count(userId);
    }

    @Operation(
            summary = "Get the count of Retrieves in progress",
            description = "Retrieves the number of Retrieves currently in progress."
    )
    @GetMapping(value = "/statistics/retrieveinprogresscount", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getRetrievesInProgressCount( @RequestHeader(HEADER_USER_ID) String userId) {

        return retrievesService.inProgressCount(userId);
    }

    @Operation(
            summary = "Get the count of Deposits in queue",
            description = "Retrieves the number of Deposits currently in the queue."
    )
    @GetMapping(value = "/vaults/depositqueuecount", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getDepositsQueueCount( @RequestHeader(HEADER_USER_ID) String userId) {

        return depositsService.queueCount(userId);
    }

    @Operation(
            summary = "Get Deposits in progress",
            description = "Retrieves a list of Deposits currently in progress."
    )
    @GetMapping(value = "/vaults/depositinprogress", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Deposit> getDepositsInProgress( @RequestHeader(HEADER_USER_ID) String userId) {

        return depositsService.inProgress();
    }

    @Operation(
            summary = "Get the count of Retrieves in queue",
            description = "Retrieves the number of Retrieves currently in the queue."
    )
    @GetMapping(value = "/vaults/retrievequeuecount", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getRetrievesQueuedCount( @RequestHeader(HEADER_USER_ID) String userId) {

        return retrievesService.queueCount(userId);
    }

    @Operation(
            summary = "Get Retrieves in progress",
            description = "Retrieves a list of Retrieves currently in progress."
    )
    @GetMapping(value = "/vaults/retrieveinprogress", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Retrieve> getRetrievesInProgress( @RequestHeader(HEADER_USER_ID) String userId) {

        return retrievesService.inProgress();
    }


    @Operation(
            summary = "Get count of Vaults by Retention Policy Status",
            description = "Retrieves the number of Vaults matching a specific Retention Policy status."
    )
    @GetMapping(value = "/vaults/retentionpolicycount/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getPolicyStatusCount(@RequestHeader(HEADER_USER_ID) String userId,
                                    @PathVariable int status) {

        return vaultsService.getRetentionPolicyCount(status);
    }

    @Operation(
            summary = "Get the total count of Events",
            description = "Retrieves the total number of Events in the system."
    )
    @GetMapping(value = "/statistics/eventcount", produces = MediaType.APPLICATION_JSON_VALUE)
    public long getEventCount( @RequestHeader(HEADER_USER_ID) String userId) {

        return eventService.count();
    }
}
