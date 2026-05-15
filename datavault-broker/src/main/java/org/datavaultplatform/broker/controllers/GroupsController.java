package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.GroupsService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.response.VaultInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
//@CrossOrigin
@Tag(name = "groups-controller", description = "Interact with DataVault Groups")
public class GroupsController {
    
    private final GroupsService groupsService;
    private final UsersService usersService;
    private final AdminService adminService;

    @Autowired
    public GroupsController(GroupsService groupsService, UsersService usersService,
        AdminService adminService) {
        this.groupsService = groupsService;
        this.usersService = usersService;
        this.adminService = adminService;
    }

    @Operation(
            summary = "Get a list of all Groups",
            description = "Gets a list of all Groups in the DataVault system."
    )
    @GetMapping(value = "/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Group> getGroups( @RequestHeader(HEADER_USER_ID) String userId) {
        return groupsService.getGroups();
    }

    @Operation(
            summary = "Get a list of Groups by Scoped Permissions",
            description = "Gets a list of Groups based on the user's scoped permissions."
    )
    @GetMapping(value = "/groups/byScopedPermissions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Group> getGroupsByScopedPermissions( @RequestHeader(HEADER_USER_ID) String userId) {
        return groupsService.getGroups(userId);
    }
    
    @Operation(
            summary = "Add a new Group",
            description = "Adds a new Group to the DataVault system."
    )
    @PostMapping(value = "/groups", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Group addGroup(@RequestHeader(HEADER_USER_ID) String userId,
                          @RequestBody Group group) throws Exception {

        adminService.ensureAdminUser(userId);
        
        groupsService.addGroup(group);
        return group;
    }

    @Operation(
            summary = "Enable a Group",
            description = "Enables a specific Group by its ID."
    )
    @PutMapping("/groups/{groupId}/enable")
    public void enableGroup(@RequestHeader(HEADER_USER_ID) String userId,
                            @PathVariable String groupId) throws Exception {

        adminService.ensureAdminUser(userId);
        
        Group group = groupsService.getGroup(groupId);
        group.setEnabled(true);
        groupsService.updateGroup(group);
    }
    
    @Operation(
            summary = "Disable a Group",
            description = "Disables a specific Group by its ID."
    )
    @PutMapping("/groups/{groupId}/disable")
    public void disableGroup(@RequestHeader(HEADER_USER_ID) String userId,
                             @PathVariable String groupId) throws Exception {

        adminService.ensureAdminUser(userId);
        
        Group group = groupsService.getGroup(groupId);
        group.setEnabled(false);
        groupsService.updateGroup(group);
    }
    
    @Operation(
            summary = "Add a Group Owner",
            description = "Adds a user as an owner to a specific Group."
    )
    @PutMapping("/groups/{groupId}/users/{ownerUserId}")
    public void addGroupOwner(@RequestHeader(HEADER_USER_ID) String userId,
                              @PathVariable String groupId,
                              @PathVariable String ownerUserId) throws Exception {

        adminService.ensureAdminUser(userId);
        
        User ownerUser = usersService.getUser(ownerUserId);
        if (ownerUser == null) {
            throw new Exception("Owner User '" + ownerUserId + "' does not exist");
        }
        
        Group group = groupsService.getGroup(groupId);
        List<User> owners = group.getOwners();
        
        if (!owners.contains(ownerUser)) {
            owners.add(ownerUser);
            groupsService.updateGroup(group);
        }
    }

    @Operation(
            summary = "Remove a Group Owner",
            description = "Removes a user as an owner from a specific Group."
    )
    @DeleteMapping("/groups/{groupId}/users/{ownerUserId}")
    public void removeGroupOwner(@RequestHeader(HEADER_USER_ID) String userId,
                                 @PathVariable String groupId,
                                 @PathVariable String ownerUserId) throws Exception {

        adminService.ensureAdminUser(userId);

        User ownerUser = usersService.getUser(ownerUserId);
        if (ownerUser == null) {
            throw new Exception("Owner User '" + ownerUserId + "' does not exist");
        }
        
        Group group = groupsService.getGroup(groupId);
        group.getOwners().remove(ownerUser);
        groupsService.updateGroup(group);
    }
    
    @Operation(
            summary = "Get the number of Groups",
            description = "Gets the total number of Groups in the DataVault system."
    )
    @GetMapping(value = "/groups/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getGroupsCount(@RequestHeader(HEADER_USER_ID) String userId) {
        return groupsService.count(userId);
    }

    @Operation(
            summary = "Get a specific DataVault Group",
            description = "Gets details for a specific DataVault Group by its ID."
    )
    @GetMapping(value = "/groups/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Group getGroup( @RequestHeader(HEADER_USER_ID) String userId,
                           @PathVariable String groupId) {
        return groupsService.getGroup(groupId);
    }

    @Operation(
            summary = "Get the number of Vaults owned by a Group",
            description = "Gets the total number of Vaults associated with a specific Group."
    )
    @GetMapping(value = "/groups/{groupId}/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getGroupVaultCount(@RequestHeader(HEADER_USER_ID) String userId,
                                  @PathVariable String groupId) {
        return groupsService.getGroup(groupId).getVaults().size();
    }

    @Operation(
            summary = "Get a list of all Vaults owned by a given Group",
            description = "Gets a list of all Vaults associated with a specific Group."
    )
    @GetMapping(value = "/groups/{groupId}/vaults", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VaultInfo> getGroupVaults(@RequestHeader(HEADER_USER_ID) String userId,
                                          @PathVariable String groupId) {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        for (Vault vault : groupsService.getGroup(groupId).getVaults()) {
            vaultResponses.add(vault.convertToResponse());
        }
        return vaultResponses;
    }

    @Operation(
            summary = "Delete a Group",
            description = "Deletes a Group from the DataVault system. Only possible if the group is not attached to any vaults or has no owners."
    )
    @DeleteMapping(value = "/groups/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean deleteGroup(@RequestHeader(HEADER_USER_ID) String userId,
                               @PathVariable String groupId) {

        Group group = groupsService.getGroup(groupId);
        // Only attempt to delete if there are no associated vaults or group owners
        if ((group.getVaults().isEmpty()) && (group.getOwners().isEmpty())) {
            groupsService.deleteGroup(group);
            return true;
        }
        return false;
    }

    @Operation(
            summary = "Update an existing Group",
            description = "Updates an existing Group in the DataVault system."
    )
    @PostMapping(value = "/groups/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Group updateGroup(@RequestHeader(HEADER_USER_ID) String userId,
                             @RequestBody Group group) throws Exception {

        adminService.ensureAdminUser(userId);

        groupsService.updateGroup(group);
        return group;
    }
}
