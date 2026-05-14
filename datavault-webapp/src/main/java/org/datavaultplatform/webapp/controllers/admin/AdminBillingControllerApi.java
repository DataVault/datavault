package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.datavaultplatform.common.response.BillingInformation;
import org.datavaultplatform.common.response.ResponseType;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

public interface AdminBillingControllerApi {
    @Operation(description = "returns BILLING report as HTML page")
    String billingByVaults(ModelMap model,
                           @RequestParam(value = "query", required = false, defaultValue = "") String query,
                           @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                           @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                           @RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId)
            throws Exception;

    @Operation(description = "returns BILLING report as CSV",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Successful CSV export",
                    content = @Content(mediaType = ResponseType.TEXT_CSV_VALUE, schema = @Schema(type = "string", format = "binary"))))
    void exportBillingVaults(HttpServletResponse response,
                             @RequestParam(value = "query", required = false, defaultValue = "") String query,
                             @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                             @RequestParam(value = "order", required = false, defaultValue = "desc") String order)
            throws Exception;

    @Operation(description = "returns HTML page for billing details for specified vaultId")
    String retrieveBillingInfo(ModelMap model, @PathVariable String vaultId);

    @Operation(description = "updates billing details and returns HTML page based on billing type",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = BillingInformation.class))))
    @PostMapping(value = "/admin/billing/updateBillingDetails", produces = MediaType.TEXT_HTML_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String updateBillingDetails(ModelMap model,
                                @ModelAttribute("billingDetails") BillingInformation billingDetails
    ) throws Exception;

}
