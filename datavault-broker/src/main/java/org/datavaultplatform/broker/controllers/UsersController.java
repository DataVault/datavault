package org.datavaultplatform.broker.controllers;

import org.datavaultplatform.broker.services.RolesAndPermissionsService;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.request.ValidateUser;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(name="Users", description = "Interact with DataVault Users")
public class UsersController {

    private RolesAndPermissionsService rolesAndPermissionsService;
    
    private UsersService usersService;

    public void setRolesAndPermissionsService(RolesAndPermissionsService rolesAndPermissionsService) {
        this.rolesAndPermissionsService = rolesAndPermissionsService;
    }

    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    @ApiMethod(
            path = "/users}",
            verb = ApiVerb.POST,
            description = "Create a new DataVault User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public User addUser(@RequestBody User user) {
        usersService.addUser(user);
        return user;
    }

    @ApiMethod(
            path = "/users/{userid}",
            verb = ApiVerb.GET,
            description = "Get a specific DataVault User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @RequestMapping(value = "/users/{userid}", method = RequestMethod.GET)
    public User getUser(@PathVariable("userid") @ApiPathParam(name = "User ID", description = "The User ID to retrieve") String queryUserID) {
        return usersService.getUser(queryUserID);
    }

    @RequestMapping(value = "/auth/users/exists", method = RequestMethod.POST)
    public Boolean exists(@RequestBody ValidateUser validateUser) {
        return usersService.getUser(validateUser.getUserid()) != null;
    }

    @RequestMapping(value = "/auth/users/isvalid", method = RequestMethod.POST)
    public Boolean validateUser(@RequestBody ValidateUser validateUser) {
        return usersService.validateUser(validateUser.getUserid(), validateUser.getPassword());
    }

    @RequestMapping(value = "/auth/users/isadmin", method = RequestMethod.POST)
    public Boolean isAdmin(@RequestBody ValidateUser validateUser) {
        return rolesAndPermissionsService.hasAdminDashboardPermissions(validateUser.getUserid());
    }
}
