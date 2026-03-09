package org.datavaultplatform.webapp.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ui.ModelMap;

import java.security.Principal;

public interface AdminControllerApi {

    @Operation(description = "returns the admin dashboard HTML page")
    String adminIndex(ModelMap modelMap, Principal principal);
}
