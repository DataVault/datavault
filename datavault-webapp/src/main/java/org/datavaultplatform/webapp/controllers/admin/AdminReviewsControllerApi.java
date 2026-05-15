package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.util.PageDTO;
import org.datavaultplatform.common.util.PageDTOVaultInfo;
import org.datavaultplatform.webapp.controllers.OpenApiSupport;
import org.datavaultplatform.webapp.model.VaultReviewModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface AdminReviewsControllerApi {
    
    @Operation(description = "returns the vault reviews HTML page")
    String getVaultsForReview(ModelMap model);

    @Operation(description = "returns the reviews HTML page for the specified vault")
    String showReview(ModelMap model,
                      @PathVariable String vaultId,
                      @RequestParam(value = "error", required = false) String error);

    @Operation(description = "processes the updated vault review",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content =
            @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    schema = @Schema(implementation = OpenApiSupport.VaultReviewModelForm.class))),
            responses = @ApiResponse(
                    responseCode = "302",
                    description = """
                            ### Possible Redirects:
                            * **Success:** Redirects to `/admin/reviews` after the review is processed.
                            * **Cancel:** Redirects to `/admin/reviews` if the action is 'cancel'.
                            * **Invalid Date:** Redirects to `/admin/vaults/{vaultId}/reviews` if the action is 'submit' and the date is invalid.""",
                    headers = @Header(
                            name = "Location",
                            description = "The destination URL based on the logic described above.",
                            schema = @Schema(type = "string"))))
    String processReview(@ModelAttribute VaultReviewModel vaultReviewModel,
                         RedirectAttributes redirectAttributes,
                         @PathVariable String vaultId,
                         @PathVariable String reviewId,
                         @Parameter(hidden = true) @RequestParam String action);

    @Operation(description = "searches for vaults that can be reviewed - used to support dynamic in-page searching of vaults. First 50 only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "422", description = "Returns a comma seperated list of validation errors", content = {
                    @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string"))
            }),
            @ApiResponse(responseCode = "200", description = "Returns a list of vaults where the name matches the search parameter", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PageDTOVaultInfo.class)))
    })
    PageDTOVaultInfo searchVaultsForReview(
            @Schema(description = "search term - part of the vault name")
            @NotBlank(message = "Search term cannot be empty")
            @Size(min = 3, max = 400, message = "Please enter between 3 and 400 characters")
            String partialVaultName);
}
