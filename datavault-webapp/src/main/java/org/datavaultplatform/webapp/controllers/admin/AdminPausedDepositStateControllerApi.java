package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.ModelAndView;

public interface AdminPausedDepositStateControllerApi {
    @Operation(description = "returns the paused deposit state HTML page",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "HTML page showing'paused deposit state' history",
                    content = @Content(mediaType = MediaType.TEXT_HTML_VALUE)))
    ModelAndView showPausedDepositHistory(Authentication auth);

    @Operation(description = "toggles the 'pause deposits state' and returns the paused deposit state HTML page",
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "State toggled successfully. Redirecting to history page.",
                            headers = @Header(
                                    name = "Location",
                                    description = "URL of the history page",
                                    schema = @Schema(type = "string")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User lacks IS_ADMIN role")})
    String toggleDepositPause(Authentication auth);
}
