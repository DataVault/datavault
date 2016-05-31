package org.datavaultplatform.webapp.authentication;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.webapp.services.RestService;
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
 * In truth this class does not do any true authentication as the user was pre-authenticated by Shib, it just
 * does any other actions required at logon time.
 *
 * User: Robin Taylor
 * Date: 13/11/2015
 * Time: 15:10
 */

public class ShibAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(ShibAuthenticationProvider.class);

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = "N/A";

        ShibWebAuthenticationDetails swad = (ShibWebAuthenticationDetails) authentication.getDetails();

        if (!restService.userExists(new ValidateUser(name, password))) {
            logger.info("Creating new account for new user " + name);
            // First time the user has logged on so create a new user account
            User user = new User();
            user.setID(name);
            user.setAdmin(false);
            user.setFirstname(swad.getFirstname());
            user.setLastname(swad.getLastname());
            user.setEmail(swad.getEmail());
            restService.addUser(user);
        } else {
            logger.info("Existing user " + name);
        }

        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new PreAuthenticatedAuthenticationToken(name, password, grantedAuths);

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
