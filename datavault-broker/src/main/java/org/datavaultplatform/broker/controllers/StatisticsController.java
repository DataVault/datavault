package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.EventService;
import org.datavaultplatform.broker.services.PendingVaultsService;
import org.datavaultplatform.broker.services.RetrievesService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Retrieve;
import org.jsondoc.core.annotation.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(name="Statistics", description = "System statistics")
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

    @GetMapping("/statistics/count")
    public int getVaultsCount(@RequestHeader(HEADER_USER_ID) String userID) {

        return vaultsService.count(userID);
    }
    
    @GetMapping("/statistics/pendingVaultsTotal")
    public int getTotalNumberOfPendingVaults() {

        return pendingVaultsService.getTotalNumberOfPendingVaults();
    }

    @GetMapping("/statistics/size")
    public Long getVaultsSize(@RequestHeader(HEADER_USER_ID) String userID) {

        return depositsService.size(userID);
    }

    @GetMapping(value = "/statistics/depositcount")
    public int getDepositsCount(@RequestHeader(HEADER_USER_ID) String userID) {

        return depositsService.count(userID);
    }

    @GetMapping("/statistics/depositinprogresscount")
    public int getDepositsInProgressCount(@RequestHeader(HEADER_USER_ID) String userID) {

        return depositsService.inProgressCount(userID);
    }

    @GetMapping("/statistics/retrievecount")
    public int getRetrievesCount(@RequestHeader(HEADER_USER_ID) String userID) {

        return retrievesService.count(userID);
    }

    @GetMapping("/statistics/retrieveinprogresscount")
    public int getRetrievesInProgressCount(@RequestHeader(HEADER_USER_ID) String userID) {

        return retrievesService.inProgressCount(userID);
    }

    @GetMapping("/vaults/depositqueuecount")
    public int getDepositsQueueCount(@RequestHeader(HEADER_USER_ID) String userID) {

        return depositsService.queueCount(userID);
    }

    @GetMapping("/vaults/depositinprogress")
    public List<Deposit> getDepositsInProgress(@RequestHeader(HEADER_USER_ID) String userID) {

        return depositsService.inProgress();
    }

    @GetMapping("/vaults/retrievequeuecount")
    public int getRetrievesQueuedCount(@RequestHeader(HEADER_USER_ID) String userID) {

        return retrievesService.queueCount(userID);
    }

    @GetMapping("/vaults/retrieveinprogress")
    public List<Retrieve> getRetrievesInProgress(@RequestHeader(HEADER_USER_ID) String userID) {

        return retrievesService.inProgress();
    }


    @GetMapping(value = "/vaults/retentionpolicycount/{status}")
    public int getPolicyStatusCount(@RequestHeader(HEADER_USER_ID) String userID,
                                    @PathVariable("status") int status) {

        return vaultsService.getRetentionPolicyCount(status);
    }
    
    @GetMapping("/statistics/eventcount")
    public int getEventCount(@RequestHeader(HEADER_USER_ID) String userID) {

        return eventService.count();
    }
}
