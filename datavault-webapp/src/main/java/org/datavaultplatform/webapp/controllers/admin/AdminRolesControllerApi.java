package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.webapp.controllers.OpenApiSupport;
import org.datavaultplatform.webapp.model.RoleViewModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;

public interface AdminRolesControllerApi {
    String OP_ID = "op-id";
    String DEOP_ID = "deop-id";

    @Operation(description = "returns the roles listing HTML page")
    ModelAndView getRolesListing(Principal principal);

    @Operation(description = "returns the HTML page showing Super(IS) Admins")
    ModelAndView getSuperAdminUsersListing(Principal principal);

    @Operation(description = "Saves a Role that has been edited",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.RoleSaveRequest.class))))
    ResponseEntity<?> save(@RequestParam(value = "id") long id,
                           @RequestParam(value = "name") String name,
                           @RequestParam(value = "type") String type,
                           @RequestParam(value = "status") String status,
                           @RequestParam(value = "description", required = false) String description,
                           @RequestParam(value = "permissions", required = false) String[] permissions);

    @Operation(description = "Deletes the specified role as long as it is not assigned",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.DeleteRoleRequest.class))))
    ResponseEntity<?> deleteRole(@RequestParam("id") long roleId);

    @Operation(description = "assigns the specified user the SuperAdmin(IS) Role",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.SuperAdminRequest.class))))
    ResponseEntity<?> addSuperAdmin(@RequestParam(OP_ID) String userId);

    @Operation(description = "removes the specified user from the SuperAdmin(IS) Role",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.DeleteSuperAdminRequest.class))))
    ResponseEntity<?> deleteSuperAdmin(Principal principal, HttpServletRequest request, @RequestParam(DEOP_ID) String userId) throws ServletException;

    @Operation(description = "gets the RoleModelView for the specified roleId")
    ResponseEntity<RoleViewModel> getRole(@PathVariable long roleId);

    @Operation(description = "returns a list of PermissionModels for all Vault permissions")
    ResponseEntity<List<PermissionModel>> getAllVaultPermissions();

    @Operation(description = "returns a list of PermissionModels for all School permissions")
    ResponseEntity<List<PermissionModel>> getAllSchoolPermissions();
}
