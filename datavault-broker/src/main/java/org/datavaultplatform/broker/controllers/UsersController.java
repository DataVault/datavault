package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.ValidateUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@CrossOrigin
@Tag(name="users-controller", description = "Interact with DataVault Users")
public class UsersController {

    private final AdminService adminService;
    
    private final UsersService usersService;

    @Autowired
    public UsersController(AdminService adminService, UsersService usersService) {
        this.adminService = adminService;
        this.usersService = usersService;
    }

    @Operation(
            summary = "Gets a list of all Users",
            description = "Retrieves a list of all users in the DataVault system."
    )
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getUsers( @RequestHeader(HEADER_USER_ID) String userId) {
        return usersService.getUsers();
    }

    @Operation(
            summary = "Create a new DataVault User",
            description = "Creates a new user in the DataVault system."
    )
    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public User addUser(@RequestBody User user) {
        usersService.addUser(user);
        return user;
    }

    @Operation(
            summary = "Get a specific DataVault User",
            description = "Retrieves details for a specific user by their ID."
    )
    @GetMapping(value = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getUser(@PathVariable String userId) {
        return usersService.getUser(userId);
    }

    @Operation(
            summary = "Check if a user exists",
            description = "Checks if a user exists in the DataVault system."
    )
    @PostMapping(value = "/auth/users/exists", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean exists(@RequestBody ValidateUser validateUser) {
        return usersService.getUser(validateUser.getUserid()) != null;
    }

    @Operation(
            summary = "Validate a user's credentials",
            description = "Validates a user's credentials in the DataVault system."
    )
    @PostMapping(value = "/auth/users/isvalid", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean validateUser(@RequestBody ValidateUser validateUser) {
        return usersService.validateUser(validateUser.getUserid(), validateUser.getPassword());
    }

    @Operation(
            summary = "Check if a user is an admin",
            description = "Checks if a user is an administrator in the DataVault system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns true if the user is an admin",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(type = "boolean", examples = "true")
                            )
                    )
            }
    )
    @PostMapping(value = "/auth/users/isadmin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean isAdmin(@RequestBody ValidateUser validateUser) {
        return adminService.isAdminUser(validateUser.getUserid());
    }
}
