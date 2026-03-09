package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.datavaultplatform.webapp.controllers.OpenApiSupport;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

public interface AdminSchoolsControllerApi {
    @Operation(description = "if there is 1 manageble school redirects to show that otherwise displays page showing all manageable schools",
            responses = {
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden if there are no manageable schools"
                    ),
                    @ApiResponse(
                            responseCode = "302",
                            description = "Redirect: redirects to /admin/schools/{schoolId}",
                            headers = @Header(name = "Location", schema = @Schema(type = "string"))
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "A page showing multiple manageable schools",
                            content = @Content(mediaType = MediaType.TEXT_HTML_VALUE, schema = @Schema(type = "string")))})
    ModelAndView getSchoolsListingPage();

    @Operation(description = "shows the roles for the specified school if the user has permission")
    ModelAndView getSchoolRoleAssignmentsPage(@PathVariable String schoolId, Principal principal);

    @Operation(description = "adds a new role assignment for the specified userId and role if the user has permission",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.AddNewRoleAssignmentRequest.class))))
    ResponseEntity<?> addNewRoleAssignment(@PathVariable String schoolId,
                                           @RequestParam("user") String userId,
                                           @RequestParam("role") Long roleId);

    @Operation(description = "updates role assignment with the specified role if the user has permission",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.UpdateExistingRoleAssignmentRequest.class))))
    ResponseEntity<?> updateExistingRoleAssignment(@PathVariable String schoolId,
                                                   @RequestParam("assignment") Long assignmentId,
                                                   @RequestParam("role") Long roleId);

    @Operation(description = "deletes the specified role assignment if the user has permission",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.DeleteRoleAssignmentRequest.class))))
    ResponseEntity<?> deleteRoleAssignment(@PathVariable String schoolId,
                                           @RequestParam("assignment") Long assignmentId);
}
