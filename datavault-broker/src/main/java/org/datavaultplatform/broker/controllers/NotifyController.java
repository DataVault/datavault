package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.services.EventService;
import org.datavaultplatform.broker.services.ClientsService;
import org.datavaultplatform.broker.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.common.event.client.*;
import org.datavaultplatform.common.model.Agent;

@RestController
//@CrossOrigin
@Tag(name = "notify-controller", description = "Inform the broker about an event")
@Slf4j
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

    @Operation(
            summary = "Register a login event",
            description = "Registers a user login event with the DataVault broker.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Logout event registered successfully",
                            content = @Content(
                                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                                    schema = @Schema(
                                            type = "string",
                                            description = "Always an empty string",
                                            example = ""
                                    )
                            )
                    )
            }
    )
    @PutMapping(value = "/notify/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String login(@RequestHeader(HEADER_USER_ID) String userId,
                        @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                        @RequestBody CreateClientEvent clientEvent) {

        log.info("USER [{}] logged IN from Client [{}]", userId, clientKey);
        Login loginEvent = new Login(clientEvent.getRemoteAddress(), clientEvent.getUserAgent());
        loginEvent.setUser(usersService.getUser(userId));
        loginEvent.setAgentType(Agent.AgentType.WEB);
        loginEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
        eventService.addEvent(loginEvent);
        
        return "";
    }

    @Operation(
            summary = "Register a logout event",
            description = "Registers a user logout event with the DataVault broker.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Logout event registered successfully",
                            content = @Content(
                                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                                    schema = @Schema(
                                            type = "string",
                                            description = "Always an empty string",
                                            example = ""
                                    )
                            )
                    )
            }
    )
    @PutMapping(value = "/notify/logout", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String logout(@RequestHeader(HEADER_USER_ID) String userId,
                         @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                         @RequestBody CreateClientEvent clientEvent) {

        log.info("USER [{}] logged OUT from Client [{}]", userId, clientKey);

        Logout logoutEvent = new Logout(clientEvent.getRemoteAddress());
        logoutEvent.setUser(usersService.getUser(userId));
        logoutEvent.setAgentType(Agent.AgentType.WEB);
        logoutEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
        eventService.addEvent(logoutEvent);
        
        return "";
    }
}
