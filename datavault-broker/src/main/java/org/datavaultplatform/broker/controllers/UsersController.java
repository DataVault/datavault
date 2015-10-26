package org.datavaultplatform.broker.controllers;

import java.util.List;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.broker.services.UsersService;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(name="Users", description = "Interact with DataVault Users")
public class UsersController {
    
    private UsersService usersService;
    
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    @ApiMethod(
            path = "/users",
            verb = ApiVerb.GET,
            description = "Gets a list of all Users",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public List<User> getUsers(@RequestHeader(value = "X-UserID", required = true) String userID) {
        return usersService.getUsers();
    }

    @ApiMethod(
            path = "/users/count",
            verb = ApiVerb.GET,
            description = "Gets the number of Users in the DataVault",
            produces = { MediaType.TEXT_PLAIN_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/users/count", method = RequestMethod.GET)
    public int getUsersCount(@RequestHeader(value = "X-UserID", required = true) String userID) {
        return usersService.count();
    }

    @ApiMethod(
            path = "/users/{userid}",
            verb = ApiVerb.GET,
            description = "Get a specific DataVault User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/users/{userid}", method = RequestMethod.GET)
    public User getUser(@RequestHeader(value = "X-UserID", required = true) String userID,
                        @PathVariable("userid") @ApiPathParam(name = "User ID", description = "The User ID to retrieve") String queryUserID) {
        return usersService.getUser(queryUserID);
    }

    @ApiMethod(
            path = "/users}",
            verb = ApiVerb.POST,
            description = "Create a new DataVault User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public User addUser(@RequestHeader(value = "X-UserID", required = true) String userID,
                          @RequestBody User user) throws Exception {
        usersService.addUser(user);
        return user;
    }
}
