package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.datavaultplatform.common.response.ResponseType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

public interface AdminDepositsControllerApi {
    @Operation(description = "returns the deposits listing HTML page")
    String getDepositsListing(ModelMap model,
                              @RequestParam(value = "query", required = false, defaultValue = "") String query,
                              @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                              @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                              @RequestParam(value = "pageId", defaultValue = "1") int pageId)
            throws Exception;

    @Operation(description = "returns DEPOSITS report as CSV",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Successful CSV export",
                    content = @Content(mediaType = ResponseType.TEXT_CSV_VALUE, schema = @Schema(type = "string", format = "binary"))))
    void exportVaults(HttpServletResponse response,
                      @RequestParam(value = "query", required = false, defaultValue = "") String query,
                      @RequestParam(value = "sort", required = false, defaultValue = "creationTime") String sort,
                      @RequestParam(value = "order", required = false, defaultValue = "desc") String order) throws Exception;

    @Operation(description = "deletes the specified deposit")
    String deleteDeposit(@PathVariable String depositId, @RequestParam(value = "vaultId") String vaultId);

    @Operation(description = "returns an HTML page for running a deposit audit")
    String runDepositAudit();

    @Operation(description = "returns an HTML for all Audits")
    String getAuditsListing(ModelMap model) throws Exception;

    @Operation(description = "returns an HTML page for deposit audits")
    String getDepositsAuditsListing(ModelMap model,
                                    @RequestParam(value = "sort", required = false) String sort)
            throws Exception;
}
