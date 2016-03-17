package org.datavaultplatform.broker.controllers;

import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.datavaultplatform.common.request.CreateClientEvent;

@RestController
@Api(name="Notify", description = "Inform the broker about an event")
public class NotifyController {
    
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
                           @RequestBody CreateClientEvent clientEvent) throws Exception {
        System.out.println("Broker login event: " + userID);
        System.out.println("\tRemote address: " + clientEvent.getRemoteAddress());
        System.out.println("\tUser agent: " + clientEvent.getUserAgent());
        return "";
    }
}
