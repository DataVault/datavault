package org.datavaultplatform.webapp.authentication;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Robin Taylor
 * Date: 06/11/2015
 * Time: 12:03
 */

@Component
public class DatabaseAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAuthenticationProvider.class);

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Note: - name here is equivalent to our User.id
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        if (!restService.isValid(new ValidateUser(name, password))) {
            logger.info("Invalid username or password for " + name);
            throw new BadCredentialsException("Invalid userid or password");
        }

        logger.info("Authentication success for " + name);

        List<GrantedAuthority> grantedAuths = new ArrayList<>();

        if (restService.isAdmin(new ValidateUser(name, null))) {
            logger.info("Granting user " + name + " ROLE_ADMIN");
            grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        logger.info("Granting user " + name + " ROLE_USER");
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(name, password, grantedAuths);

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}