package org.datavaultplatform.broker.controllers.admin;

import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.User;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiHeader;
import org.jsondoc.core.annotation.ApiHeaders;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User: Robin Taylor
 * Date: 29/04/2016
 * Time: 15:31
 */

@RestController
@Api(name="AdminUsers", description = "Administrator User functions")
public class AdminUsersController {

    private UsersService usersService;

    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    @ApiMethod(
            path = "/admin/users/search",
            verb = ApiVerb.GET,
            description = "Search Users",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/admin/users/search", method = RequestMethod.GET)
    public List<User> getUsers(@RequestHeader(value = "X-UserID", required = true) String userID,
                               @RequestParam String query) {
        return usersService.search(query);
    }

    @ApiMethod(
            path = "/admin/users/count",
            verb = ApiVerb.GET,
            description = "Gets the number of Users in the DataVault",
            produces = { MediaType.TEXT_PLAIN_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/admin/users/count", method = RequestMethod.GET)
    public int getUsersCount(@RequestHeader(value = "X-UserID", required = true) String userID) {
        return usersService.count();
    }

    @ApiMethod(
            path = "/admin/users}",
            verb = ApiVerb.POST,
            description = "Create a new DataVault User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @RequestMapping(value = "/admin/users", method = RequestMethod.POST)
    public User addUser(@RequestHeader(value = "X-UserID", required = true) String userID,
                        @RequestBody User user) throws Exception {
        usersService.addUser(user);

        // Add default fileStores for the new user?

        return user;
    }

    @ApiMethod(
            path = "/admin/users}",
            verb = ApiVerb.PUT,
            description = "Edit a DataVault User",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @RequestMapping(value = "/admin/users", method = RequestMethod.PUT)
    public User editUser(@RequestHeader(value = "X-UserID", required = true) String userID,
                         @RequestBody User user) throws Exception {

        usersService.updateUser(user);

        return user;
    }

}
