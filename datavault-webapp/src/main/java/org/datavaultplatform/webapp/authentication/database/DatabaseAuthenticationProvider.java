package org.datavaultplatform.webapp.authentication.database;

import org.datavaultplatform.common.model.RoleName;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.webapp.authentication.authorization.GrantedAuthorityService;
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

import java.util.ArrayList;
import java.util.List;

/**
 * User: Robin Taylor
 * Date: 06/11/2015
 * Time: 12:03
 */

public class DatabaseAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAuthenticationProvider.class);

    private final RestService restService;

    private final GrantedAuthorityService grantedAuthorityService;

    public DatabaseAuthenticationProvider(RestService restService, GrantedAuthorityService grantedAuthorityService) {
        this.restService = restService;
        this.grantedAuthorityService = grantedAuthorityService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Note: - name here is equivalent to our User.id
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        boolean isUserValid = false;
        try {
            isUserValid = restService.isValid(new ValidateUser(name, password));
        } catch(Exception e) {
            logger.error("Error when trying to check if user is valid with Broker!",e);
        }

        if (!isUserValid) {
            logger.info("Invalid username or password for " + name);
            throw new BadCredentialsException("Invalid userid or password");
        }

        logger.info("Authentication success for " + name);

        List<GrantedAuthority> grantedAuths = new ArrayList<>();

        List<GrantedAuthority> grantedAuthsForUser = this.grantedAuthorityService.getGrantedAuthoritiesForUser(name, authentication);
        grantedAuths.addAll(grantedAuthsForUser);

        logger.info("Granting user " + name + " " + RoleName.ROLE_USER);
        grantedAuths.add(new SimpleGrantedAuthority(RoleName.ROLE_USER));
        return new UsernamePasswordAuthenticationToken(name, password, grantedAuths);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
