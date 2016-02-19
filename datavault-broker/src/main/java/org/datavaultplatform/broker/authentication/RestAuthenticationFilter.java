package org.datavaultplatform.broker.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: Robin Taylor
 * Date: 18/02/2016
 * Time: 10:45
 */
public class RestAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationFilter.class);

    // Can be overridden in Spring config
    private String principalRequestHeader = "X-UserID";


    public RestAuthenticationFilter() {
        // Doesn't matter what goes in here as it should be overridden in the Spring config
        super("/dummy");
    }


    public void setPrincipalRequestHeader(String principalRequestHeader) {
        this.principalRequestHeader = principalRequestHeader;
    }


    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        Object principal = getPreAuthenticatedPrincipal(request);
        Object credentials = getPreAuthenticatedCredentials(request);

        PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(principal, credentials);
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);

    }

    /**
     * Read and returns the header named by {@code apiKeyRequestHeader} from the
     * request.
     *
     * @throws PreAuthenticatedCredentialsNotFoundException if the header is missing and
     * {@code exceptionIfHeaderMissing} is set to {@code true}.
     */
    private Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return request.getHeader(principalRequestHeader);
    }

    /**
     * Credentials aren't usually applicable, but if a {@code credentialsRequestHeader} is
     * set, this will be read and used as the credentials value. Otherwise a dummy value
     * will be used.
     */
    private Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }
}
