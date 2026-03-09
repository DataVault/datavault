package org.datavaultplatform.webapp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.response.DepositInfo;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

public interface DepositsControllerApi {

    // Return an 'create new deposit' page
    @Operation(description = "returns the deposit creation HTML page")
    String createDeposit(ModelMap model, @PathVariable String vaultId) throws Exception;

    // Process the completed 'create new deposit' page
    // templates/deposits/create.html
    @Operation(description = "adds a deposit then redirects to show new vault deposit page",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Deposit details sent as form data",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = CreateDeposit.class))),
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Redirects to the vault details page (`/vaults/{vaultId}/deposits/{depositId}`) after deposit added.",
                            headers = @Header(
                                    name = "Location",
                                    description = "The target URL for the redirect.",
                                    schema = @Schema(type = "string")))})
    String createAndAddDeposit(@ModelAttribute CreateDeposit deposit,
                               @PathVariable String vaultId);

    // View properties of a single deposit
    @Operation(description = "returns the deposit HTML page")
    String getDeposit(ModelMap model,
                      @PathVariable String vaultId,
                      @PathVariable String depositId) throws Exception;

    // View properties of a single deposit as a JSON object
    @Operation(description = "returns the deposit information as json")
    DepositInfo getDepositJson(
            @PathVariable String vaultId,
            @PathVariable String depositId) throws Exception;

    // View jobs related to a single deposit as a JSON object
    @Operation(description = "returns the deposit jobs as json")
    Job[] getDepositJobsJson(@PathVariable String vaultId, @PathVariable String depositId) throws Exception;

    // Return a 'retrieve deposit' page
    @Operation(description = "shows to the retrieves page")
    String retrieveDeposit(
            @ModelAttribute Retrieve retrieve,
            ModelMap model,
            @PathVariable String vaultId,
            @PathVariable String depositId) throws Exception;

    // Process the completed 'retrieve deposit' page
    // templates/deposits/retrieve.html
    @Operation(description = "starts a deposit retrieval then redirects to show deposit page",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content =
            @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    schema = @Schema(implementation = Retrieve.class))),
            responses = @ApiResponse(
                    responseCode = "302",
                    description = "Redirects to the vault details page (`/vaults/{vaultId}/deposits/{depositId}`) after retrieve started.",
                    headers = @Header(
                            name = "Location",
                            description = "The target URL for the redirect.",
                            schema = @Schema(type = "string"))))
    String processRetrieve(@ModelAttribute Retrieve retrieve,
                           @PathVariable String vaultId,
                           @PathVariable String depositId
    );

    // Process the completed 'restart retrieve' page
    @Operation(description = "re-starts a deposit retrieval then redirects to show deposit page",
            responses = @ApiResponse(
                    responseCode = "302",
                    description = "Redirects to the vault details page (`/vaults/{vaultId}/deposits/{depositId}`) after retrieve restarted.",
                    headers = @Header(
                            name = "Location",
                            description = "The target URL for the redirect.",
                            schema = @Schema(type = "string"))))
    String restartRetrieve(ModelMap model,
                           @PathVariable String vaultId,
                           @PathVariable String depositId,
                           @PathVariable String retrieveId) throws Exception;

    // Process the completed 'restart deposit' page
    @Operation(description = "re-starts a deposit then redirects to show deposit page",
            responses =
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirects to the vault details page (`/vaults/{vaultId}/deposits/{depositId}`) after deposit restarted.",
                    headers = @Header(
                            name = "Location",
                            description = "The target URL for the redirect.",
                            schema = @Schema(type = "string"))))
    String restartDeposit(ModelMap model,
                          @PathVariable String vaultId,
                          @PathVariable String depositId) throws Exception;

}
