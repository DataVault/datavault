package org.datavaultplatform.broker.controllers.admin;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

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

    private final UsersService usersService;

    public AdminUsersController(UsersService usersService) {
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
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/admin/users/search")
    public List<User> getUsers(@RequestHeader(HEADER_USER_ID) String userID,
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
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/admin/users/count")
    public long getUsersCount(@RequestHeader(HEADER_USER_ID) String userID) {
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
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID"),
            @ApiHeader(name=HEADER_CLIENT_KEY, description="DataVault API Client Key")
    })
    @PostMapping("/admin/users")
    public User addUser(@RequestHeader(HEADER_USER_ID) String userID,
                        @RequestBody User user) {
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
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID"),
            @ApiHeader(name=HEADER_CLIENT_KEY, description="DataVault API Client Key")
    })
    @PutMapping("/admin/users")
    public User editUser(@RequestHeader(HEADER_USER_ID) String userID,
                         @RequestBody User user) {

        usersService.updateUser(user);

        return user;
    }

}
