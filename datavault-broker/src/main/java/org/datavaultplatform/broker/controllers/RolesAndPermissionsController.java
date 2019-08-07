package org.datavaultplatform.broker.controllers;

import org.datavaultplatform.broker.services.RolesAndPermissionsService;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@Api(name="Permissions", description = "Interact with DataVault Roles and Permissions")
public class RolesAndPermissionsController {

    private RolesAndPermissionsService rolesAndPermissionsService;

    public void setRolesAndPermissionsService(RolesAndPermissionsService rolesAndPermissionsService) {
        this.rolesAndPermissionsService = rolesAndPermissionsService;
    }

    @ApiMethod(
            path = "/permissions/role",
            verb = ApiVerb.POST,
            description = "Create a new DataVault Role",
            consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @PostMapping("/role")
    public RoleModel createRole(@RequestBody RoleModel role) {
        return rolesAndPermissionsService.createRole(role);
    }

    @ApiMethod(
            path = "/permissions/roleAssignment",
            verb = ApiVerb.POST,
            description = "Create a new DataVault Role Assignment",
            consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @PostMapping("/roleAssignment")
    public RoleAssignment createRoleAssignment(@RequestBody RoleAssignment roleAssignment) {
        return rolesAndPermissionsService.createRoleAssignment(roleAssignment);
    }

    @ApiMethod(
            path = "/permissions/school",
            verb = ApiVerb.GET,
            description = "Gets all school permissions",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/school")
    public PermissionModel[] getSchoolPermissions() {
        List<PermissionModel> schoolPermissions = rolesAndPermissionsService.getSchoolPermissions();
        return schoolPermissions.toArray(new PermissionModel[0]);
    }

    @ApiMethod(
            path = "/permissions/school",
            verb = ApiVerb.GET,
            description = "Gets all school permissions",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/vault")
    public PermissionModel[] getVaultPermissions() {
        List<PermissionModel> vaultPermissions = rolesAndPermissionsService.getVaultPermissions();
        return vaultPermissions.toArray(new PermissionModel[0]);
    }

    @ApiMethod(
            path = "/permissions/role/{roleId}",
            verb = ApiVerb.GET,
            description = "Gets all roles which can be edited",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/role/{roleId}")
    public RoleModel getRole(@PathVariable("roleId") @ApiPathParam(name = "Role ID", description = "The ID of the role to get") Long id) {
        return rolesAndPermissionsService.getRole(id);
    }

    @ApiMethod(
            path = "/permissions/role/isAdmin",
            verb = ApiVerb.GET,
            description = "Gets all roles which can be edited",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/role/isAdmin")
    public RoleModel getIsAdmin() {
        return rolesAndPermissionsService.getIsAdmin();
    }

    @ApiMethod(
            path = "/permissions/roles",
            verb = ApiVerb.GET,
            description = "Gets all roles which can be edited",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/roles")
    public RoleModel[] getEditableRoles() {
        List<RoleModel> editableRoles = rolesAndPermissionsService.getEditableRoles();
        return editableRoles.toArray(new RoleModel[0]);
    }

    @ApiMethod(
            path = "/permissions/roles/readOnly",
            verb = ApiVerb.GET,
            description = "Gets all roles which can be viewed but not edited",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/roles/readOnly")
    public RoleModel[] getViewableRoles() {
        List<RoleModel> viewableRoles = rolesAndPermissionsService.getViewableRoles();
        return viewableRoles.toArray(new RoleModel[0]);
    }

    @ApiMethod(
            path = "/permissions/roles/school",
            verb = ApiVerb.GET,
            description = "Gets all school roles",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/roles/school")
    public RoleModel[] getAllSchoolRoles() {
        List<RoleModel> editableRoles = rolesAndPermissionsService.getSchoolRoles();
        return editableRoles.toArray(new RoleModel[0]);
    }

    @ApiMethod(
            path = "/permissions/roles/vault",
            verb = ApiVerb.GET,
            description = "Gets all vault roles",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/roles/vault")
    public RoleModel[] getAllVaultRoles() {
        List<RoleModel> editableRoles = rolesAndPermissionsService.getVaultRoles();
        return editableRoles.toArray(new RoleModel[0]);
    }

    @ApiMethod(
            path = "/permissions/roleAssignments/school/{assignmentId}",
            verb = ApiVerb.GET,
            description = "Gets a specified role assignment",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/roleAssignment/{assignmentId}")
    public RoleAssignment getRoleAssignment(
            @ApiPathParam(name = "Role ID", description = "The ID of the school to get role assignments for") @PathVariable("assignmentId") Long assignmentId) {
        return rolesAndPermissionsService.getRoleAssignment(assignmentId);
    }

    @ApiMethod(
            path = "/permissions/roleAssignments/school/{schoolId}",
            verb = ApiVerb.GET,
            description = "Gets all role assignments for a given school",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/roleAssignments/school/{schoolId}")
    public RoleAssignment[] getRoleAssignmentsForSchool(
            @ApiPathParam(name = "Role ID", description = "The ID of the school to get role assignments for") @PathVariable("schoolId") String schoolId) {
        List<RoleAssignment> schoolRoleAssignments = rolesAndPermissionsService.getRoleAssignmentsForSchool(schoolId);
        return schoolRoleAssignments.toArray(new RoleAssignment[0]);
    }

    @ApiMethod(
            path = "/permissions/roleAssignments/vault/{vaultId}",
            verb = ApiVerb.GET,
            description = "Gets all role assignments for a given school",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/roleAssignments/vault/{vaultId}")
    public RoleAssignment[] getRoleAssignmentsForVault(
            @ApiPathParam(name = "Vault ID", description = "The ID of the vault to get role assignments for") @PathVariable("vaultId") String vaultId) {
        List<RoleAssignment> schoolRoleAssignments = rolesAndPermissionsService.getRoleAssignmentsForVault(vaultId);
        return schoolRoleAssignments.toArray(new RoleAssignment[0]);
    }

    @ApiMethod(
            path = "/permissions/roleAssignments/user/{userId}",
            verb = ApiVerb.GET,
            description = "Gets all role assignments for a given school",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/roleAssignments/user/{userId}")
    public RoleAssignment[] getRoleAssignmentsForUser(
            @ApiPathParam(name = "User ID", description = "The ID of the user to get role assignments for") @PathVariable("userId") String userId) {
        List<RoleAssignment> schoolRoleAssignments = rolesAndPermissionsService.getRoleAssignmentsForUser(userId);
        return schoolRoleAssignments.toArray(new RoleAssignment[0]);
    }

    @ApiMethod(
            path = "/permissions/roleAssignments/role/{roleId}",
            verb = ApiVerb.GET,
            description = "Gets all role assignments for a given role",
            produces = {MediaType.APPLICATION_JSON_VALUE},
            responsestatuscode = "200 - OK"
    )
    @GetMapping("/roleAssignments/role/{roleId}")
    public RoleAssignment[] getRoleAssignmentsForRole(
            @ApiPathParam(name = "Role ID", description = "The ID of the role to get role assignments for") @PathVariable("roleId") Long roleId) {
        List<RoleAssignment> roleAssignments = rolesAndPermissionsService.getRoleAssignmentsForRole(roleId);
        return roleAssignments.toArray(new RoleAssignment[0]);
    }

    @ApiMethod(
            path = "/permissions/role",
            verb = ApiVerb.PUT,
            description = "Updates a role",
            consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @PutMapping("/role")
    public RoleModel updateRole(@RequestBody RoleModel role) {
        return rolesAndPermissionsService.updateRole(role);
    }

    @ApiMethod(
            path = "/permissions/roleAssignment",
            verb = ApiVerb.PUT,
            description = "Update an existing DataVault Role Assignment",
            consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @PutMapping("/roleAssignment")
    public RoleAssignment updateRoleAssignment(@RequestBody RoleAssignment roleAssignment) {
        return rolesAndPermissionsService.updateRoleAssignment(roleAssignment);
    }

    @ApiMethod(
            path = "/permissions/role/{roleId}",
            verb = ApiVerb.DELETE,
            description = "Deletes a role",
            responsestatuscode = "200 - OK"
    )
    @DeleteMapping("/role/{roleId}")
    public ResponseEntity deleteRole(@PathVariable("roleId") @ApiPathParam(name = "Role ID", description = "The ID of the role to delete") Long roleId) {
        rolesAndPermissionsService.deleteRole(roleId);
        return ResponseEntity.ok().build();
    }

    @ApiMethod(
            path = "/permissions/roleAssignment/{roleAssignmentId}",
            verb = ApiVerb.DELETE,
            description = "Delete a DataVault Role Assignment",
            consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @DeleteMapping("/roleAssignment/{roleAssignmentId}")
    public ResponseEntity deleteRoleAssignment(
            @PathVariable("roleAssignmentId") @ApiPathParam(name = "Role ID", description = "The ID of the role assignment to delete") Long roleAssignmentId) {
        rolesAndPermissionsService.deleteRoleAssignment(roleAssignmentId);
        return ResponseEntity.ok().build();
    }
}
