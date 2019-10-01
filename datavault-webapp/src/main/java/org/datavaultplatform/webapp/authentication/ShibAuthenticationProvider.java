package org.datavaultplatform.webapp.authentication;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.webapp.model.AdminDashboardPermissionsModel;
import org.datavaultplatform.webapp.security.ScopedGrantedAuthority;
import org.datavaultplatform.webapp.services.LDAPService;
import org.datavaultplatform.webapp.services.PermissionsService;
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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    private PermissionsService permissionsService;
    private Boolean ldapEnabled;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public void setLdapService(LDAPService ldapService) {
        this.ldapService = ldapService;
    }

    public void setPermissionsService(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
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

        boolean userExists = false;
        try{
            userExists = restService.userExists(new ValidateUser(name, password));
        } catch(Exception e){
            System.err.println("Error when trying to check if user exists with Broker!");
            e.printStackTrace();
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
                System.err.println("Error when trying to add user with Broker!");
                e.printStackTrace();
            }
        } else {
            List<RoleAssignment> roles = restService.getRoleAssignmentsForUser(name);
            List<ScopedGrantedAuthority> scopedAuthorities = ScopedGrantedAuthority.fromRoleAssignments(roles);

            grantedAuths.addAll(scopedAuthorities);

            logger.info("Existing user " + name);
            boolean isAdmin;
            try{
                isAdmin = restService.isAdmin(new ValidateUser(name, null));
                if (isAdmin) {
                    grantedAuths.add(new SimpleGrantedAuthority("ROLE_IS_ADMIN"));
                }
            } catch(Exception e){
                System.err.println("Error when trying to check if user is admin with Broker!");
                e.printStackTrace();
            }

            Collection<GrantedAuthority> adminAuthorities = getAdminAuthorities(authentication);
            if (!adminAuthorities.isEmpty()) {
                logger.info("Granting user " + name + " ROLE_ADMIN");
                grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                grantedAuths.addAll(adminAuthorities);
            }
        }

        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new PreAuthenticatedAuthenticationToken(name, password, grantedAuths);

    }

    private Collection<GrantedAuthority> getAdminAuthorities(Principal principal) {
        AdminDashboardPermissionsModel adminPermissions = permissionsService.getDashboardPermissions(principal);
        return adminPermissions.getUnscopedPermissions().stream()
                .filter(p -> p.getPermission().getRoleName() != null)
                .map(p -> new SimpleGrantedAuthority(p.getPermission().getRoleName()))
                .collect(Collectors.toSet());
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
