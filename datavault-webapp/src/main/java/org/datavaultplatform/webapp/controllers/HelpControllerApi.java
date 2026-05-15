package org.datavaultplatform.webapp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ui.ModelMap;

public interface HelpControllerApi {

    @Operation(description = "displays the help HTML page")
    String help(ModelMap model);

}
