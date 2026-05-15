package org.datavaultplatform.webapp.controllers.auth;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.Assert;

import java.io.IOException;

public class DataVaultAccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Assert.isTrue(accessDeniedException != null, "An AccessDeniedException must be provided");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, accessDeniedException);
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE, accessDeniedException.getMessage());
        
        String requestURI = request.getRequestURI();
        Assert.hasText(requestURI, "Request URI must not be null or empty");
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, requestURI);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.getRequestDispatcher("/auth/denied").forward(request, response);
    }
}
