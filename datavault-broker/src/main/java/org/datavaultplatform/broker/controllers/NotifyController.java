package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

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
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @PutMapping("/notify/login")
    public String login(@RequestHeader(HEADER_USER_ID) String userID,
                        @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                        @RequestBody CreateClientEvent clientEvent) {
        
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
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @PutMapping("/notify/logout")
    public String logout(@RequestHeader(HEADER_USER_ID) String userID,
                         @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                         @RequestBody CreateClientEvent clientEvent) {
        
        Logout logoutEvent = new Logout(clientEvent.getRemoteAddress());
        logoutEvent.setUser(usersService.getUser(userID));
        logoutEvent.setAgentType(Agent.AgentType.WEB);
        logoutEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
        eventService.addEvent(logoutEvent);
        
        return "";
    }
}
