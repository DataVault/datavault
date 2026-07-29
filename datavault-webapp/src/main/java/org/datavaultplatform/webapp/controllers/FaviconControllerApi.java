package org.datavaultplatform.webapp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "System", description = "Get Favicon")
public interface FaviconControllerApi {

    String IMAGE_X_ICON = "image/x-icon";

    @Operation(description = "returns the favicon")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = IMAGE_X_ICON, schema = @Schema(type = "string", format = "binary")))
    String favicon();
}
