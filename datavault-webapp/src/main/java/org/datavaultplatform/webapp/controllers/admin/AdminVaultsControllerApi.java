package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.datavaultplatform.common.response.ResponseType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface AdminVaultsControllerApi {

    @Operation(description = "returns the vaults listing HTML page")
    String searchVaults(ModelMap model,
                        @RequestParam(value = "query", defaultValue = "") String query,
                        @RequestParam(value = "sort", defaultValue = "creationTime") String sort,
                        @RequestParam(value = "order", defaultValue = "desc") String order,
                        @RequestParam(value = "pageId", defaultValue = "1") int pageId) throws Exception;

    @Operation(description = "returns the vaults listing CSV file",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Successful export of vaults CSV",
                    content = @Content(
                            mediaType = ResponseType.TEXT_CSV_VALUE,
                            schema = @Schema(type = "string", format = "binary") /* 'binary' tells Browser it's a file download */)))
    void exportVaults(HttpServletResponse response,
                      @RequestParam(value = "query", required = false) String query,
                      @RequestParam(value = "sort", required = false) String sort,
                      @RequestParam(value = "order", required = false) String order) throws Exception;

    @Operation(description = "returns the vaults HTML page for the specified vault")
    String showVault(ModelMap model, @PathVariable String vaultId) throws Exception;

    @Operation(description = "redirects to vaults HTML page for the specified vault after checking vault retention policy",
            responses = @ApiResponse(
                    responseCode = "302",
                    description = "Redirects to the vault details page (`/admin/vaults/{vaultId}`) after the retention policy check is complete.",
                    headers = @Header(
                            name = "Location",
                            description = "The target URL for the redirect.",
                            schema = @Schema(type = "string"))))
    String checkPolicy(ModelMap model, @PathVariable String vaultId) throws Exception;
}
