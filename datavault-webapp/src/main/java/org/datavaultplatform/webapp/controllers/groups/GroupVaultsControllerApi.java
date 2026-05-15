package org.datavaultplatform.webapp.controllers.groups;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ui.ModelMap;

public interface GroupVaultsControllerApi {

    @Operation(description = "returns the groups listing HTML page")
    String getGroupVaultsListing(ModelMap model);
}
