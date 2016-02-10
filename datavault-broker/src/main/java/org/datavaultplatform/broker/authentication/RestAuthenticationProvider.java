package org.datavaultplatform.broker.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.ArrayList;
import java.util.List;

/**
 * In truth this class does not do any true authentication as it is assumed the user was authenticated by the client
 * application, it just checks to see that the combination of user and client application is authorised to do what
 * they want to do.
 *
 * User: Robin Taylor
 * Date: 13/11/2015
 * Time: 15:10
 */

public class RestAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        RestWebAuthenticationDetails rwad = (RestWebAuthenticationDetails) authentication.getDetails();


        logger.info("Client Key is " + rwad.getClientKeyRequestHeader());
        logger.info("Client IP address is " + rwad.getRemoteAddress());

        // todo : validate the contents of rwad and set a role accordingly

        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new PreAuthenticatedAuthenticationToken(name, password, grantedAuths);

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
