package org.datavaultplatform.broker.controllers.admin;

import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.RetrievesService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.EventInfo;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import org.datavaultplatform.broker.services.EventService;

/**
 * Created by Robin Taylor on 08/03/2016.
 */


@RestController
@Api(name="Admin", description = "Administrator functions")
public class AdminController {

    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RetrievesService retrievesService;
    private EventService eventService;
    
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

    @RequestMapping(value = "/admin/deposits", method = RequestMethod.GET)
    public List<DepositInfo> getDepositsAll(@RequestHeader(value = "X-UserID", required = true) String userID,
                                            @RequestParam(value = "sort", required = false) String sort) throws Exception {

        List<DepositInfo> depositResponses = new ArrayList<>();
        for (Deposit deposit : depositsService.getDeposits(sort)) {
            depositResponses.add(deposit.convertToResponse());
        }
        return depositResponses;
    }

    @RequestMapping(value = "/admin/retrieves", method = RequestMethod.GET)
    public List<Retrieve> getRetrievesAll(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return retrievesService.getRetrieves();
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
    public List<VaultInfo> getVaultsAll(@RequestHeader(value = "X-UserID", required = true) String userID,
                                        @RequestParam(value = "sort", required = false)
                                        @ApiQueryParam(name = "sort", description = "Vault sort field", allowedvalues = {"id", "name", "description", "vaultSize", "user", "policy", "creationTime"}, defaultvalue = "creationTime", required = false) String sort,
                                        @RequestParam(value = "order", required = false)
                                        @ApiQueryParam(name = "order", description = "Vault sort order", allowedvalues = {"asc", "dec"}, defaultvalue = "asc", required = false) String order) throws Exception {

        if (sort == null) sort = "";
        if (order == null) order = "asc";

        List<VaultInfo> vaultResponses = new ArrayList<>();
        for (Vault vault : vaultsService.getVaults(sort, order)) {
            vaultResponses.add(vault.convertToResponse());
        }
        return vaultResponses;
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
}
