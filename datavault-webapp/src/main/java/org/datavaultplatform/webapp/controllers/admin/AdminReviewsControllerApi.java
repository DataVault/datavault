package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.datavaultplatform.webapp.controllers.OpenApiSupport;
import org.datavaultplatform.webapp.model.VaultReviewModel;
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
}
