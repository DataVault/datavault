package org.datavaultplatform.broker.controllers;

import org.datavaultplatform.broker.services.EventService;
import org.datavaultplatform.broker.services.ClientsService;
import org.datavaultplatform.broker.services.UsersService;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.common.event.client.*;
import org.datavaultplatform.common.model.Agent;

@RestController
@Api(name="Notify", description = "Inform the broker about an event")
public class NotifyController {
    
    private final EventService eventService;
    private final ClientsService clientsService;
    private final UsersService usersService;

    @Autowired
    public NotifyController(EventService eventService, ClientsService clientsService,
        UsersService usersService) {
        this.eventService = eventService;
        this.clientsService = clientsService;
        this.usersService = usersService;
    }

    @ApiMethod(
            path = "/notify/login}",
            verb = ApiVerb.PUT,
            description = "Notify the broker about a client login event",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/notify/login", method = RequestMethod.PUT)
    public String login(@RequestHeader(value = "X-UserID", required = true) String userID,
                        @RequestHeader(value = "X-Client-Key", required = true) String clientKey,
                        @RequestBody CreateClientEvent clientEvent) throws Exception {
        
        Login loginEvent = new Login(clientEvent.getRemoteAddress(), clientEvent.getUserAgent());
        loginEvent.setUser(usersService.getUser(userID));
        loginEvent.setAgentType(Agent.AgentType.WEB);
        loginEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
        eventService.addEvent(loginEvent);
        
        return "";
    }
    
    @ApiMethod(
            path = "/notify/logout}",
            verb = ApiVerb.PUT,
            description = "Notify the broker about a client logout event",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/notify/logout", method = RequestMethod.PUT)
    public String logout(@RequestHeader(value = "X-UserID", required = true) String userID,
                         @RequestHeader(value = "X-Client-Key", required = true) String clientKey,
                         @RequestBody CreateClientEvent clientEvent) throws Exception {
        
        Logout logoutEvent = new Logout(clientEvent.getRemoteAddress());
        logoutEvent.setUser(usersService.getUser(userID));
        logoutEvent.setAgentType(Agent.AgentType.WEB);
        logoutEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
        eventService.addEvent(logoutEvent);
        
        return "";
    }
}
