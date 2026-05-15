package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestParam;

public interface AdminRetrievesControllerApi {

    @Operation(description = "returns the retrieves listing HTML page")
    String getRetrievesListing(ModelMap model,
                               @RequestParam(value = "query", required = false, defaultValue = "") String query,
                               @RequestParam(value = "sort", required = false, defaultValue = "timestamp") String sort,
                               @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                               @RequestParam(value = "pageId", defaultValue = "1") int pageId) throws Exception;
}
