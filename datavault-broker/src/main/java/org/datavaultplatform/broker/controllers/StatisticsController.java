package org.datavaultplatform.broker.controllers;

import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.EventService;
import org.datavaultplatform.broker.services.RetrievesService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Retrieve;
import org.jsondoc.core.annotation.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(name="Statistics", description = "System statistics")
public class StatisticsController {

    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RetrievesService retrievesService;
    private EventService eventService;

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }

    public void setRetrievesService(RetrievesService retrievesService) {
        this.retrievesService = retrievesService;
    }
    
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @RequestMapping(value = "/statistics/count", method = RequestMethod.GET)
    public int getVaultsCount(@RequestHeader(value = "X-UserID", required = true) String userID) {

        return vaultsService.count(userID);
    }

    @RequestMapping(value = "/statistics/size", method = RequestMethod.GET)
    public Long getVaultsSize(@RequestHeader(value = "X-UserID", required = true) String userID) {

        return depositsService.size(userID);
    }

    @RequestMapping(value = "/statistics/depositcount", method = RequestMethod.GET)
    public int getDepositsCount(@RequestHeader(value = "X-UserID", required = true) String userID) {

        return depositsService.count(userID);
    }

    @RequestMapping(value = "/statistics/depositinprogresscount", method = RequestMethod.GET)
    public int getDepositsInProgressCount(@RequestHeader(value = "X-UserID", required = true) String userID) {

        return depositsService.inProgressCount(userID);
    }

    @RequestMapping(value = "/statistics/retrievecount", method = RequestMethod.GET)
    public int getRetrievesCount(@RequestHeader(value = "X-UserID", required = true) String userID) {

        return retrievesService.count(userID);
    }

    @RequestMapping(value = "/statistics/retrieveinprogresscount", method = RequestMethod.GET)
    public int getRetrievesInProgressCount(@RequestHeader(value = "X-UserID", required = true) String userID) {

        return retrievesService.inProgressCount(userID);
    }

    @RequestMapping(value = "/vaults/depositqueuecount", method = RequestMethod.GET)
    public int getDepositsQueueCount(@RequestHeader(value = "X-UserID", required = true) String userID) {

        return depositsService.queueCount(userID);
    }

    @RequestMapping(value = "/vaults/depositinprogress", method = RequestMethod.GET)
    public List<Deposit> getDepositsInProgress(@RequestHeader(value = "X-UserID", required = true) String userID) {

        return depositsService.inProgress();
    }

    @RequestMapping(value = "/vaults/retrievequeuecount", method = RequestMethod.GET)
    public int getRetrievesQueuedCount(@RequestHeader(value = "X-UserID", required = true) String userID) {

        return retrievesService.queueCount(userID);
    }

    @RequestMapping(value = "/vaults/retrieveinprogress", method = RequestMethod.GET)
    public List<Retrieve> getRetrievesInProgress(@RequestHeader(value = "X-UserID", required = true) String userID) {

        return retrievesService.inProgress();
    }


    @RequestMapping(value = "/vaults/retentionpolicycount/{status}", method = RequestMethod.GET)
    public int getPolicyStatusCount(@RequestHeader(value = "X-UserID", required = true) String userID,
                                    @PathVariable("status") int status) {

        return vaultsService.getRetentionPolicyCount(status);
    }
    
    @RequestMapping(value = "/statistics/eventcount", method = RequestMethod.GET)
    public int getEventCount(@RequestHeader(value = "X-UserID", required = true) String userID) {

        return eventService.count();
    }
}
