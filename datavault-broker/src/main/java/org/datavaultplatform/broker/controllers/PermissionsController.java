package org.datavaultplatform.broker.controllers;

import org.datavaultplatform.broker.services.PermissionsService;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleModel;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@Api(name="Permissions", description = "Interact with DataVault Roles and Permissions")
public class PermissionsController {

    private PermissionsService permissionsService;

    public void setPermissionsService(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @ApiMethod(
            path = "/permissions/role}",
            verb = ApiVerb.POST,
            description = "Create a new DataVault Role",
            consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @PostMapping("/role")
    public RoleModel createRole(@RequestBody RoleModel role) {
        return permissionsService.createRole(role);
    }

    @ApiMethod(
            path = "/permissions/school}",
            verb = ApiVerb.GET,
            description = "Gets all school permissions",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @GetMapping("/school")
    public List<PermissionModel> getSchoolPermissions() {
        return permissionsService.getSchoolPermissions();
    }

    @ApiMethod(
            path = "/permissions/school}",
            verb = ApiVerb.GET,
            description = "Gets all school permissions",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @GetMapping("/vault")
    public List<PermissionModel> getVaultPermissions() {
        return permissionsService.getVaultPermissions();
    }

    @ApiMethod(
            path = "/role}",
            verb = ApiVerb.PUT,
            description = "Updates a role",
            consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @PutMapping("/role")
    public RoleModel updateRole(@RequestBody RoleModel role) {
        return permissionsService.updateRole(role);
    }

    @ApiMethod(
            path = "/role/{roleId}}",
            verb = ApiVerb.DELETE,
            description = "Deletes a role",
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID"),
            @ApiHeader(name="X-Client-Key", description="DataVault API Client Key")
    })
    @DeleteMapping("/role/{roleId}")
    public ResponseEntity deleteRole(@PathVariable("roleId") @ApiPathParam(name = "Role ID", description = "The ID of the role to delete") String roleId) {
        permissionsService.deleteRole(roleId);
        return ResponseEntity.ok().build();
    }
}
