package org.datavaultplatform.webapp.controllers.auth;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestParam;

public interface AuthControllerApi {

    @Operation(description = "returns the login page")
    String getLoginPage(@RequestParam(value = "error", required = false) boolean error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "security", required = false) String security,
                        ModelMap model);

    @Operation(description = "returns the logout page")
    String redirectToLogout(ModelMap model, HttpSession session);

    @Operation(description = "returns the denied page")
    String getDeniedPage(HttpServletRequest request, HttpServletResponse response, Model model);

    @Operation(description = "returns the confirmation page")
    String getConfirmationPage(ModelMap model);
}
