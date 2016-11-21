package org.datavaultplatform.webapp.authentication;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.webapp.services.LDAPService;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    private RestService restService;
    private LDAPService ldapService;
    private Boolean ldapEnabled;


    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public void setLdapService(LDAPService ldapService) {
        this.ldapService = ldapService;
    }

    public void setLdapEnabled(Boolean ldapEnabled) {
        this.ldapEnabled = ldapEnabled;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = "N/A";

        ShibWebAuthenticationDetails swad = (ShibWebAuthenticationDetails) authentication.getDetails();

        List<GrantedAuthority> grantedAuths = new ArrayList<>();

        if (!restService.userExists(new ValidateUser(name, password))) {
            logger.info("Creating new account for new user " + name);
            User user = new User();
            user.setID(name);
            user.setAdmin(false);
            user.setFirstname(swad.getFirstname());
            user.setLastname(swad.getLastname());
            user.setEmail(swad.getEmail());

            if (ldapEnabled) {
                user.setProperties(getLDAPAttributes(name));
            }

            restService.addUser(user);


        } else {
            logger.info("Existing user " + name);
            if (restService.isAdmin(new ValidateUser(name, null))) {
                logger.debug("user is an admin " + name);
                grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        }

        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new PreAuthenticatedAuthenticationToken(name, password, grantedAuths);

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }

    private HashMap<String, String> getLDAPAttributes(String name) {
        HashMap<String, String> attributes;

        try {
            ldapService.getConnection();
            attributes = ldapService.search(name);

        }catch (Exception e) {
            throw new AuthenticationServiceException("LDAP Exception", e);

        } finally {
            try {
                ldapService.closeConnection();
            } catch (LdapException e) {
                throw new AuthenticationServiceException("LDAP Exception", e);
            }
        }

        return attributes;
    }
}
