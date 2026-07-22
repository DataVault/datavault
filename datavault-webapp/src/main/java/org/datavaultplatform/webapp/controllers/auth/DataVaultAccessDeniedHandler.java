package org.datavaultplatform.webapp.controllers.auth;

import java.io.IOException;
import java.util.Set;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.Assert;

public class DataVaultAccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {

    private static final Set<String> STATE_CHANGING_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Assert.isTrue(accessDeniedException != null, "An AccessDeniedException must be provided");

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        if (STATE_CHANGING_METHODS.contains(request.getMethod().toUpperCase())) {
            // For state-changing requests (POST, PUT, DELETE, PATCH), just send the 403 error without forwarding.
            // This avoids the "405 Method Not Allowed" error when the CSRF token is missing.
            // This will NOT render the auth/denied.html template
            response.getWriter().write("Forbidden: " + accessDeniedException.getMessage());
            return;
        }

        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, accessDeniedException);
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE, accessDeniedException.getMessage());
        
        String requestURI = request.getRequestURI();
        Assert.hasText(requestURI, "Request URI must not be null or empty");
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, requestURI);
        request.getRequestDispatcher("/auth/denied").forward(request, response);
    }
}
