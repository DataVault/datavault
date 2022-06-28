package org.datavaultplatform.webapp.authentication.shib;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.webapp.authentication.AuthenticationSuccess;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.ArrayList;
import java.util.HashMap;
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

    private final RestService restService;
    private final LDAPService ldapService;

    private final ShibGrantedAuthorityService shibGrantedAuthorityService;

    private final boolean ldapEnabled;

    @Autowired
    private AuthenticationSuccess authenticationSuccess;

    public ShibAuthenticationProvider(RestService restService, LDAPService ldapService, boolean ldapEnabled, ShibGrantedAuthorityService shibGrantedAuthorityService ) {
        this.restService = restService;
        this.ldapService = ldapService;
        this.ldapEnabled = ldapEnabled;
        this.shibGrantedAuthorityService = shibGrantedAuthorityService;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = "N/A";

        ShibWebAuthenticationDetails swad = (ShibWebAuthenticationDetails) authentication.getDetails();

        List<GrantedAuthority> grantedAuths = new ArrayList<>();

        boolean userExists = false;
        try{
            userExists = restService.userExists(new ValidateUser(name, password));
        } catch(Exception e){
            logger.error("Error when trying to check if user exists with Broker!", e);
        }

        if (!userExists) {
            logger.info("Creating new account for new user " + name);
            User user = new User();
            user.setID(name);
            user.setFirstname(swad.getFirstname());
            user.setLastname(swad.getLastname());
            user.setEmail(swad.getEmail());

            if (ldapEnabled) {
                user.setProperties(getLDAPAttributes(name));
            }

            try{
                restService.addUser(user);
            } catch(Exception e){
                logger.error("Error when trying to add user with Broker!",e);
            }
        } else {
            List<GrantedAuthority> grantedAuthsForUser = this.shibGrantedAuthorityService.getGrantedAuthoritiesForUser(name, authentication);
            grantedAuths.addAll(grantedAuthsForUser);
        }

        grantedAuths.add(ShibUtils.ROLE_USER);
        return new PreAuthenticatedAuthenticationToken(name, password, grantedAuths);

    }


    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }

    private HashMap<String, String> getLDAPAttributes(String name) {
        try {
            return ldapService.getLDAPAttributes(name);
        }catch (Exception e) {
            throw new AuthenticationServiceException("LDAP Exception", e);
        }
    }
}
