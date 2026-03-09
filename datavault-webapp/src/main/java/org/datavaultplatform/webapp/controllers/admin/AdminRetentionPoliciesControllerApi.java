package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

public interface AdminRetentionPoliciesControllerApi {
    @Operation(description = "returns the retention policies listing HTML page")
    String getRetentionPoliciesListing(ModelMap model);

    @Operation(description = "deletes a retention policy")
    ResponseEntity<Void> deleteRetentionPoliciesListing(ModelMap model, @PathVariable String policyId) throws Exception;

    @Operation(description = "returns an HTML page to allow a new retentionpolicy to be added")
    String addRetentionPolicyPage(ModelMap model);

    @Operation(description = "add a new retention policy",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = CreateRetentionPolicy.class))),
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Redirects to /admin/retentionpolicies",
                            headers = @Header(name = "Location", description = "The URL to display all retentionpolicies", schema = @Schema(type = "string"))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid date format or ID")})
    String addRetentionPolicy(@ModelAttribute CreateRetentionPolicy createRetentionPolicy, ModelMap model, @RequestParam String action) throws Exception;

    @Operation(description = "returns an HTML page to allow a retentionpolicy to be edited")
    String editRetentionPolicyPage(ModelMap model, @PathVariable String retentionPolicyId) throws Exception;

    @Operation(description = "edits a retention policy and then redirects to show all retention policies",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = CreateRetentionPolicy.class))),
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Redirects to /admin/retentionpolicies",
                            headers = @Header(name = "Location", description = "The URL to display all retentionpolicies", schema = @Schema(type = "string"))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid date format or ID")})
    String editRetentionPolicy(@ModelAttribute CreateRetentionPolicy createRetentionPolicy, ModelMap model, @PathVariable String retentionPolicyId, @RequestParam String action) throws Exception;
}
