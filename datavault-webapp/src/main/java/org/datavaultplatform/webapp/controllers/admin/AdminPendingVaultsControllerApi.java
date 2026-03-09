package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.webapp.controllers.OpenApiSupport;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

public interface AdminPendingVaultsControllerApi {
    @Operation(description = "returns the pending vaults listing HTML page")
    String searchPendingVaults(ModelMap model,
                               @RequestParam(value = "query", defaultValue = "") String query,
                               @RequestParam(value = "sort", defaultValue = "creationTime") String sort,
                               @RequestParam(value = "order", defaultValue = "desc") String order,
                               @RequestParam(value = "pageId", defaultValue = "1") int pageId);

    // The Admin Edit PV page
    @Operation(description = "returns the HTML page to edit pending vault specified by vaultId")
    String getPendingVault(ModelMap model, @PathVariable String vaultId);

    @Operation(description = "returns an HTML page of saved PendingVaults that match the query/pageId parameters sorted using 'sort and order' parameters")
    String searchSavedPendingVaults(ModelMap model,
                                    @RequestParam(value = "query", defaultValue = "") String query,
                                    @RequestParam(value = "sort", defaultValue = "creationTime") String sort,
                                    @RequestParam(value = "order", defaultValue = "desc") String order,
                                    @RequestParam(value = "pageId", defaultValue = "1") int pageId);

    @Operation(description = "returns an HTML page of confirmed PendingVaults that match the query/pageId parameters sorted using 'sort and order' parameters")
    String searchConfirmedPendingVaults(ModelMap model,
                                        @RequestParam(value = "query", defaultValue = "") String query,
                                        @RequestParam(value = "sort", defaultValue = "creationTime") String sort,
                                        @RequestParam(value = "order", defaultValue = "desc") String order,
                                        @RequestParam(value = "pageId", defaultValue = "1") int pageId);


    @Operation(description = "returns an HTML page which shows summary of pending vault specified via pendingVaultId")
    String getVault(ModelMap model, @PathVariable("pendingVaultId") String vaultID, Principal principal);

    @Operation(description = "returns redirect to show vault after pending vault has been upgraded",
            responses = @ApiResponse(
                    responseCode = "302",
                    description = "Redirects to /vaults/{vaultId}",
                    headers = @Header(name = "Location", description = "The URL of the upgraded vault", schema = @Schema(type = "string"))))
    String upgradeVault(@PathVariable("pendingVaultId") String pendingVaultID,
                        @Parameter(
                                description = "The date the vault was reviewed in ISO 8601 format (YYYY-MM-DD)",
                                examples = @ExampleObject("2026-03-10"),
                                schema = @Schema(type = "string", format = "date") // This triggers the ISO 8601 'date' format
                        )
                        @RequestParam("reviewDate") String reviewDateString);

    // Process the completed 'create new vault' page
    @Operation(description = "updates pending vault then return a redirect to continue edit of a pending vault",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = OpenApiSupport.CreateVaultForm.class))),
            responses = @ApiResponse(
                    responseCode = "302",
                    description = "Redirects to /admin/pendingVaults/edit/{vaultId}",
                    headers = @Header(name = "Location", description = "The URL to continue editing the pending vault", schema = @Schema(type = "string"))))
    String editPendingVault(@ModelAttribute CreateVault vault, ModelMap model, @RequestParam String action,
                            Principal principal);

    @Operation(description = "Deletes a pending vault and returns redirect to show pending vaults",
            responses = @ApiResponse(
                    responseCode = "302",
                    description = "Redirects to /admin/pendingVaults",
                    headers = @Header(name = "Location", description = "The URL to show pending vaults", schema = @Schema(type = "string"))))
    String deletePendingVault(ModelMap model, @PathVariable String pendingVaultId);

}
