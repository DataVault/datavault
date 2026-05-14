package org.datavaultplatform.webapp.controllers;

import io.swagger.v3.oas.annotations.Operation;

public interface AccessibilityControllerApi {
    @Operation(description = "returns the accessibility HTML page")
    String accessibility();
}
