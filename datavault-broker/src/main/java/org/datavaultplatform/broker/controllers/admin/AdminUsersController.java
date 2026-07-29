package org.datavaultplatform.broker.controllers.admin;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.User;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User: Robin Taylor
 * Date: 29/04/2016
 * Time: 15:31
 */

@RestController
//@CrossOrigin
@Tag(name="admin-users-controller", description = "Administrator User functions")
public class AdminUsersController {

    private final UsersService usersService;

    public AdminUsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @Operation(
            summary = "Search Users",
            description = "Searches for users based on a query string."
    )
    @GetMapping(value = "/admin/users/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getUsers(@RequestHeader(HEADER_USER_ID) String userId,
                               @RequestParam String query) {
        return usersService.search(query);
    }

    @Operation(
            summary = "Get the number of Users",
            description = "Gets the total number of Users in the DataVault system."
    )
    @GetMapping(value = "/admin/users/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public long getUsersCount( @RequestHeader(value = HEADER_USER_ID, required = false) String userID) {
        return usersService.count();
    }

    @Operation(
            summary = "Create a new DataVault User",
            description = "Creates a new user in the DataVault system."
    )
    @PostMapping(value = "/admin/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public User addUser(@RequestHeader(HEADER_USER_ID) String userId,
                        @RequestBody User user) {
        usersService.addUser(user);

        // Add default fileStores for the new user?

        return user;
    }

    @Operation(
            summary = "Edit a DataVault User",
            description = "Updates an existing user in the DataVault system."
    )
    @PutMapping(value = "/admin/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public User editUser(@RequestHeader(HEADER_USER_ID) String userId,
                         @RequestBody User user) {

        usersService.updateUser(user);

        return user;
    }

}
