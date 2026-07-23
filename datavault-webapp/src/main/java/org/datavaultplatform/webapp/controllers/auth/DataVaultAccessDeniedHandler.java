package org.datavaultplatform.webapp.controllers.auth;

import java.io.IOException;
import java.util.Set;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

@Slf4j
public class DataVaultAccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {

    private static final Set<String> STATE_CHANGING_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("Access denied for request {} {}: {}", request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // For state-changing requests (POST, PUT, DELETE, PATCH), just send the 403 error without forwarding.
        // This avoids the "405 Method Not Allowed" error when the CSRF token is missing.
        // This will NOT render the auth/denied.html template
        if (STATE_CHANGING_METHODS.contains(request.getMethod().toUpperCase())) {
            if (accessDeniedException instanceof MissingCsrfTokenException) {
                // The session died, taking the token with it
                response.getWriter().write("Your session has expired due to inactivity. Please refresh the page and try again.");
            } else if (accessDeniedException instanceof InvalidCsrfTokenException) {
                // The session exists, but the token didn't match (potential attack or multi-tab issue)
                response.getWriter().write("Invalid security token. Please refresh the page and try again.");
            } else {
                // Standard authorization failure
                response.getWriter().write("Forbidden: " + accessDeniedException.getMessage());
            }
            return;
        }

        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, accessDeniedException);
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE, accessDeniedException.getMessage());
        
        String requestURI = request.getRequestURI();
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, requestURI);
        request.getRequestDispatcher("/auth/denied").forward(request, response);
    }
}
