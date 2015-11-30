package org.datavaultplatform.broker.controllers;

import java.util.ArrayList;
import org.datavaultplatform.broker.services.GroupsService;
import org.datavaultplatform.common.model.Group;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.response.VaultInfo;

@RestController
@Api(name="Groups", description = "Interact with DataVault Groups")
public class GroupsController {
    
    private GroupsService groupsService;
    
    public void setGroupsService(GroupsService groupsService) {
        this.groupsService = groupsService;
    }

    @ApiMethod(
            path = "/groups",
            verb = ApiVerb.GET,
            description = "Gets a list of all Groups",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    public List<Group> getGroups(@RequestHeader(value = "X-UserID", required = true) String userID) {
        return groupsService.getGroups();
    }

    @ApiMethod(
            path = "/groups/count",
            verb = ApiVerb.GET,
            description = "Gets the number of Groups in the DataVault",
            produces = { MediaType.TEXT_PLAIN_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/groups/count", method = RequestMethod.GET)
    public int getGroupsCount(@RequestHeader(value = "X-UserID", required = true) String userID) {
        return groupsService.count();
    }

    @ApiMethod(
            path = "/groups/{groupid}",
            verb = ApiVerb.GET,
            description = "Get a specific DataVault Group",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/groups/{groupid}", method = RequestMethod.GET)
    public Group getGroup(@RequestHeader(value = "X-UserID", required = true) String userID,
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
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/groups/{groupid}/count", method = RequestMethod.GET)
    public int getGroupVaultCount(@RequestHeader(value = "X-UserID", required = true) String userID,
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
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/groups/{groupid}/vaults", method = RequestMethod.GET)
    public List<VaultInfo> getGroupVaults(@RequestHeader(value = "X-UserID", required = true) String userID,
                                          @PathVariable("groupid") String groupID) throws Exception {

        List<VaultInfo> vaultResponses = new ArrayList<>();
        for (Vault vault : groupsService.getGroup(groupID).getVaults()) {
            vaultResponses.add(vault.convertToResponse());
        }
        return vaultResponses;
    }
}
