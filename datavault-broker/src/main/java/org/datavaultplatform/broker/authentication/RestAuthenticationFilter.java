package org.datavaultplatform.broker.authentication;



import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import java.lang.reflect.Field;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * User: Robin Taylor
 * Date: 18/02/2016
 * Time: 10:45
 *
 * Note - Although we don't do any real authentication of the user, and in fact could have a null user, we couldn't
 * extend the AbstractPreAuthenticatedProcessingFilter because a null user would have caused it to return immediately,
 * whilst we still need to validate the client app in our provider.
 *
 */
public class RestAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationFilter.class);

    // Can be overridden in Spring config
    private String principalRequestHeader = HEADER_USER_ID;


    public RestAuthenticationFilter(RequestMatcher matcher) {
        super(matcher);
    }

    @SneakyThrows
    public RequestMatcher getRequestMatcher() {
        Field f = AbstractAuthenticationProcessingFilter.class.getDeclaredField("requiresAuthenticationRequestMatcher");
        f.setAccessible(true);
        RequestMatcher result = (RequestMatcher)f.get(this);
        return result;
    }


    public void setPrincipalRequestHeader(String principalRequestHeader) {
        this.principalRequestHeader = principalRequestHeader;
    }


    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!requiresAuthentication(request, response)) {
            chain.doFilter(request, response);

            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Request is to process authentication");
        }

        Authentication authResult;

        try {
            authResult = attemptAuthentication(request, response);
            if (authResult == null) {
                // return immediately as subclass has indicated that it hasn't completed
                // authentication
                return;
            }
            //sessionStrategy.onAuthentication(authResult, request, response);
        }
        catch (InternalAuthenticationServiceException failed) {
            logger.error(
                    "An internal error occurred while trying to authenticate the user.",
                    failed);
            unsuccessfulAuthentication(request, response, failed);

            return;
        }
        catch (AuthenticationException failed) {
            // Authentication failed
            unsuccessfulAuthentication(request, response, failed);

            return;
        }

        successfulAuthentication(request, response, chain, authResult);

        chain.doFilter(request, response);
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, ServletException {

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
