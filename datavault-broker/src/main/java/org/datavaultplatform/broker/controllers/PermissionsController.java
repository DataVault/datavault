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
    @GetMapping("/school")
    public PermissionModel[] getSchoolPermissions() {
        List<PermissionModel> schoolPermissions = permissionsService.getSchoolPermissions();
        return schoolPermissions.toArray(new PermissionModel[0]);
    }

    @ApiMethod(
            path = "/permissions/school}",
            verb = ApiVerb.GET,
            description = "Gets all school permissions",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/vault")
    public PermissionModel[] getVaultPermissions() {
        List<PermissionModel> vaultPermissions = permissionsService.getVaultPermissions();
        return vaultPermissions.toArray(new PermissionModel[0]);
    }

    @ApiMethod(
            path = "/permissions/roles}",
            verb = ApiVerb.GET,
            description = "Gets all roles which can be edited",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/roles")
    public RoleModel[] getEditableRoles() {
        List<RoleModel> editableRoles = permissionsService.getEditableRoles();
        return editableRoles.toArray(new RoleModel[0]);
    }

    @ApiMethod(
            path = "/role}",
            verb = ApiVerb.PUT,
            description = "Updates a role",
            consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
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
    @DeleteMapping("/role/{roleId}")
    public ResponseEntity deleteRole(@PathVariable("roleId") @ApiPathParam(name = "Role ID", description = "The ID of the role to delete") Long roleId) {
        permissionsService.deleteRole(roleId);
        return ResponseEntity.ok().build();
    }
}
