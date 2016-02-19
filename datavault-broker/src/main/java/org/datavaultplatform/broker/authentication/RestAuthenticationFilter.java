package org.datavaultplatform.broker.authentication;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

import javax.servlet.http.HttpServletRequest;

/**
 * Under the covers this builds an PreAuthenticatedAuthenticationToken which is passed to the AuthenticationManager
 * and then on to the AuthenticationProvider(s).
 *
 * User: Robin Taylor
 * Date: 13/11/2015
 * Time: 13:16
 */
public class RestAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    // todo : set both of these in Spring config?
    private String UserIdRequestHeader = "X-UserID";
    private boolean exceptionIfHeaderMissing = true;

    /**
     * Read and returns the header named by {@code apiKeyRequestHeader} from the
     * request.
     *
     * @throws PreAuthenticatedCredentialsNotFoundException if the header is missing and
     * {@code exceptionIfHeaderMissing} is set to {@code true}.
     */
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String principal = request.getHeader(UserIdRequestHeader);

        if (principal == null && exceptionIfHeaderMissing) {
            throw new PreAuthenticatedCredentialsNotFoundException(UserIdRequestHeader
                    + " header not found in request.");
        }

        return principal;
    }

    /**
     * Credentials aren't usually applicable, but if a {@code credentialsRequestHeader} is
     * set, this will be read and used as the credentials value. Otherwise a dummy value
     * will be used.
     */
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

}
