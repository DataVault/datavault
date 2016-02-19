package org.datavaultplatform.broker.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: Robin Taylor
 * Date: 18/02/2016
 * Time: 14:15
 */
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationFailureHandler.class);

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {

        logger.debug("Authentication Failure - returning " + HttpServletResponse.SC_UNAUTHORIZED);

        // Commented out the next line as I'm not sure its good practice to tell unknown user what went wrong
        //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed: " + exception.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Get thee behind me Satan!");

    }
}
