package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestParam;

public interface AdminEventsControllerApi {

    @Operation(description = "returns the events listing HTML page")
    String getEventsListing(ModelMap model,
                            @RequestParam(value = "query", required = false) String query,
                            @RequestParam(value = "sort", required = false) String sort) throws Exception;
}
