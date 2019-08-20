package org.datavaultplatform.webapp.authentication;

import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.webapp.security.ScopedGrantedAuthority;
import org.datavaultplatform.webapp.model.AdminDashboardPermissionsModel;
import org.datavaultplatform.webapp.services.PermissionsService;
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
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Robin Taylor
 * Date: 06/11/2015
 * Time: 12:03
 */

@Component
public class DatabaseAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAuthenticationProvider.class);

    private RestService restService;

    private PermissionsService permissionsService;

	public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public void setPermissionsService(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Note: - name here is equivalent to our User.id
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        boolean isUserValid = false;
        try{
            isUserValid = restService.isValid(new ValidateUser(name, password));
        }catch(Exception e){
            System.err.println("Error when trying to check if user is valid with Broker!");
            e.printStackTrace();
        }

        if (!isUserValid) {
            logger.info("Invalid username or password for " + name);
            throw new BadCredentialsException("Invalid userid or password");
        }

        logger.info("Authentication success for " + name);

        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        List<RoleAssignment> roleAssignments = restService.getRoleAssignmentsForUser(name);

        for (RoleAssignment assignment : roleAssignments) {
            if (assignment.getVault() != null || assignment.getSchool() != null) {
                grantedAuths.add(new ScopedGrantedAuthority(assignment));
            }
        }

        boolean isAdmin = false;
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

        logger.info("Granting user " + name + " ROLE_USER");
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(name, password, grantedAuths);

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
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
