package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.GroupsService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.response.VaultInfo;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Api(name="Groups", description = "Interact with DataVault Groups")
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

    @ApiMethod(
            path = "/groups",
            verb = ApiVerb.GET,
            description = "Gets a list of all Groups",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/groups")
    public List<Group> getGroups(@RequestHeader(HEADER_USER_ID) String userID) {
        return groupsService.getGroups();
    }

    @ApiMethod(
            path = "/groups/byScopedPermissions",
            verb = ApiVerb.GET,
            description = "Gets a list of all Groups",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/groups/byScopedPermissions")
    public List<Group> getGroupsByScopedPermissions(@RequestHeader(HEADER_USER_ID) String userId) {
        return groupsService.getGroups(userId);
    }
    
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID"),
            @ApiHeader(name=HEADER_CLIENT_KEY, description="DataVault API Client Key")
    })
    @PostMapping("/groups")
    public Group addGroup(@RequestHeader(HEADER_USER_ID) String userID,
                          @RequestBody Group group) throws Exception {

        adminService.ensureAdminUser(userID);
        
        groupsService.addGroup(group);
        return group;
    }

    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID"),
            @ApiHeader(name=HEADER_CLIENT_KEY, description="DataVault API Client Key")
    })
    @PutMapping("/groups/{groupid}/enable")
    public @ResponseBody void enableGroup(@RequestHeader(HEADER_USER_ID) String userID,
                                          @PathVariable("groupid") String groupId) throws Exception {

        adminService.ensureAdminUser(userID);
        
        Group group = groupsService.getGroup(groupId);
        group.setEnabled(true);
        groupsService.updateGroup(group);
    }
    
        @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID"),
            @ApiHeader(name=HEADER_CLIENT_KEY, description="DataVault API Client Key")
    })
    @PutMapping("/groups/{groupid}/disable")
    public @ResponseBody void disableGroup(@RequestHeader(HEADER_USER_ID) String userID,
                                           @PathVariable("groupid") String groupId) throws Exception {

            adminService.ensureAdminUser(userID);
        
        Group group = groupsService.getGroup(groupId);
        group.setEnabled(false);
        groupsService.updateGroup(group);
    }
    
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID"),
            @ApiHeader(name=HEADER_CLIENT_KEY, description="DataVault API Client Key")
    })
    @PutMapping("/groups/{groupid}/users/{owneruserid}")
    public @ResponseBody void addGroupOwner(@RequestHeader(HEADER_USER_ID) String userID,
                                            @PathVariable("groupid") String groupId,
                                            @PathVariable("owneruserid") String ownerUserId) throws Exception {

        adminService.ensureAdminUser(userID);
        
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

    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID"),
            @ApiHeader(name=HEADER_CLIENT_KEY, description="DataVault API Client Key")
    })
    @DeleteMapping("/groups/{groupid}/users/{owneruserid}")
    public @ResponseBody void removeGroupOwner(@RequestHeader(HEADER_USER_ID) String userID,
                                               @PathVariable("groupid") String groupId,
                                               @PathVariable("owneruserid") String ownerUserId) throws Exception {

        adminService.ensureAdminUser(userID);

        User ownerUser = usersService.getUser(ownerUserId);
        if (ownerUser == null) {
            throw new Exception("Owner User '" + ownerUserId + "' does not exist");
        }
        
        Group group = groupsService.getGroup(groupId);
        group.getOwners().remove(ownerUser);
        groupsService.updateGroup(group);
    }
    
    @ApiMethod(
            path = "/groups/count",
            verb = ApiVerb.GET,
            description = "Gets the number of Groups in the DataVault",
            produces = { MediaType.TEXT_PLAIN_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/groups/count")
    public int getGroupsCount(@RequestHeader(HEADER_USER_ID) String userID) {
        return groupsService.count(userID);
    }

    @ApiMethod(
            path = "/groups/{groupid}",
            verb = ApiVerb.GET,
            description = "Get a specific DataVault Group",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/groups/{groupid}")
    public Group getGroup(@RequestHeader(HEADER_USER_ID) String userID,
                          @PathVariable("groupid") @ApiPathParam(name = "Group ID", description = "The Group ID to retrieve") String queryGroupID) {
        return groupsService.getGroup(queryGroupID);
    }

    @ApiMethod(
            path = "/groups/{groupid}/count",
            verb = ApiVerb.GET,
            description = "Get the number of Vaults owned by a Group",
            produces = { MediaType.TEXT_PLAIN_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/groups/{groupid}/count")
    public int getGroupVaultCount(@RequestHeader(HEADER_USER_ID) String userID,
                                  @PathVariable("groupid") @ApiPathParam(name = "Group ID", description = "The Group ID to retrieve") String groupID) {
        return groupsService.getGroup(groupID).getVaults().size();
    }

    @ApiMethod(
            path = "/groups/{groupid}/vaults",
            verb = ApiVerb.GET,
            description = "Gets a list of all Vaults owned by a given Group",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @GetMapping("/groups/{groupid}/vaults")
    public List<VaultInfo> getGroupVaults(@RequestHeader(HEADER_USER_ID) String userID,
                                          @PathVariable("groupid") String groupID) {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        for (Vault vault : groupsService.getGroup(groupID).getVaults()) {
            vaultResponses.add(vault.convertToResponse());
        }
        return vaultResponses;
    }

    @ApiMethod(
            path = "/groups/{groupid}",
            verb = ApiVerb.DELETE,
            description = "Delete Group (only possible on groups not attached to any vault)",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name=HEADER_USER_ID, description="DataVault Broker User ID")
    })
    @DeleteMapping("/groups/{groupid}")
    public boolean deleteGroup(@RequestHeader(HEADER_USER_ID) String userID,
                               @PathVariable("groupid") String groupID) {

        Group group = groupsService.getGroup(groupID);
        // Only attempt to delete if there are no associated vaults or group owners
        if ((group.getVaults().size() == 0) && (group.getOwners().size() == 0)) {
            groupsService.deleteGroup(group);
            return true;
        }
        return false;
    }

    @PostMapping("/groups/update")
    public ResponseEntity<Group> updateGroup(@RequestHeader(HEADER_USER_ID) String userID,
                                          @RequestBody Group group) throws Exception {

        adminService.ensureAdminUser(userID);

        groupsService.updateGroup(group);
        return new ResponseEntity<>(group, HttpStatus.OK);
    }
}
