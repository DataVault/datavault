package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.ValidateUser;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(name="Users", description = "Interact with DataVault Users")
public class UsersController {

    private final AdminService adminService;
    
    private final UsersService usersService;

    @Autowired
    public UsersController(AdminService adminService, UsersService usersService) {
        this.adminService = adminService;
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
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping( "/users")
    public List<User> getUsers(@RequestHeader(HEADER_USER_ID) String userID) {
        return usersService.getUsers();
    }

    @ApiMethod(
            path = "/users}",
            verb = ApiVerb.POST,
            description = "Create a new DataVault User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID"),
            @ApiHeader(name=HEADER_CLIENT_KEY, description="DataVault API Client Key")
    })
    @PostMapping("/users")
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
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID"),
            @ApiHeader(name=HEADER_CLIENT_KEY, description="DataVault API Client Key")
    })
    @GetMapping("/users/{userid}")
    public User getUser(@PathVariable("userid") @ApiPathParam(name = "User ID", description = "The User ID to retrieve") String queryUserID) {
        return usersService.getUser(queryUserID);
    }

    @PostMapping("/auth/users/exists")
    public Boolean exists(@RequestBody ValidateUser validateUser) {
        return usersService.getUser(validateUser.getUserid()) != null;
    }

    @PostMapping("/auth/users/isvalid")
    public Boolean validateUser(@RequestBody ValidateUser validateUser) {
        return usersService.validateUser(validateUser.getUserid(), validateUser.getPassword());
    }

    @PostMapping("/auth/users/isadmin")
    public Boolean isAdmin(@RequestBody ValidateUser validateUser) {
        return adminService.isAdminUser(validateUser.getUserid());
    }
}
