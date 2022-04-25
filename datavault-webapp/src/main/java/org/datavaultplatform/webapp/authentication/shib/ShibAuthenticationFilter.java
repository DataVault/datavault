package org.datavaultplatform.webapp.authentication.shib;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

import javax.servlet.http.HttpServletRequest;
import org.springframework.util.Assert;

/**
 * Under the covers this builds an PreAuthenticatedAuthenticationToken which is passed to the AuthenticationManager
 * and then on to the AuthenticationProvider(s).
 *
 * User: Robin Taylor
 * Date: 13/11/2015
 * Time: 13:16
 */
public class ShibAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    private String principalRequestHeader;
    private boolean exceptionIfHeaderMissing = true;

    public void setPrincipalRequestHeader(String principalRequestHeader) {
        this.principalRequestHeader = principalRequestHeader;
    }


    /**
     * Read and returns the header named by {@code principalRequestHeader} from the
     * request.
     *
     * @throws PreAuthenticatedCredentialsNotFoundException if the header is missing and
     * {@code exceptionIfHeaderMissing} is set to {@code true}.
     */
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String principal = request.getHeader(principalRequestHeader);

        if (principal == null && exceptionIfHeaderMissing) {
            throw new PreAuthenticatedCredentialsNotFoundException(principalRequestHeader
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

    /**
     * Defines whether an exception should be raised if the principal header is missing.
     * Defaults to {@code true}.
     *
     * @param exceptionIfHeaderMissing set to {@code false} to override the default
     * behaviour and allow the request to proceed if no header is found.
     */
    public void setExceptionIfHeaderMissing(boolean exceptionIfHeaderMissing) {
        this.exceptionIfHeaderMissing = exceptionIfHeaderMissing;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.notNull(this.principalRequestHeader, "The principal request header must be set");
    }

}
