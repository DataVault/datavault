package org.datavaultplatform.webapp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.servlet.view.RedirectView;

public interface WelcomeControllerApi {
    @Operation(description = "redirects / to /vaults",
            responses = @ApiResponse(
                    responseCode = "302",
                    description = "Redirects to /vaults",
                    headers = @Header(
                            name = "Location",
                            description = "The URL to display vaults",
                            schema = @Schema(type = "string"))))
    RedirectView getVaultsListing();

    @Operation(description = "displays the welcome HTML page")
    String welcome();
}
