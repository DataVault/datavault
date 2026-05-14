package org.datavaultplatform.webapp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface RoleAssignmentControllerApi {
    @Operation(description = "updates the role assignment",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.UpdateRoleAssignmentRequest.class))))
    ResponseEntity<Void> updateRoleAssignment(
            @PathVariable String roleType,
            @PathVariable String targetId,
            @Valid @NotNull @RequestParam("assignment") Long assignmentId,
            @Valid @NotNull(message = "Please specify a role") @RequestParam("role") Long roleId);

    @Operation(description = "removes a role assignment",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.RemoveRoleAssignmentRequest.class))))
    ResponseEntity<Void> removeRoleAssignment(@PathVariable String roleType,
                                              @PathVariable String targetId,
                                              @Schema(hidden = true) @RequestParam("assignment") long assignmentId);

    @Operation(description = "creates a new role assignment",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = RoleAssignmentRequest.class))))
    ResponseEntity<?> createRoleAssignment(
            @PathVariable String roleType,
            @PathVariable String targetId,
            @Valid RoleAssignmentRequest request);

    @Data
    class RoleAssignmentRequest {
        @NotNull(message = "Please specify a role")
        Long role;

        @NotNull
        @NotEmpty(message = "Please specify a user")
        String user;
    }
}
