package org.datavaultplatform.webapp.authentication.database;

import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleName;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.webapp.authentication.AuthenticationUtils;
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

public class DatabaseAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAuthenticationProvider.class);

    private final RestService restService;

    private final PermissionsService permissionsService;

    public DatabaseAuthenticationProvider(RestService restService, PermissionsService permissionsService) {
        this.restService = restService;
        this.permissionsService = permissionsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Note: - name here is equivalent to our User.id
        String name = authentication.getName();
        Object credentials = authentication.getCredentials();
        String password = credentials == null ? null : credentials.toString();

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
        List<RoleAssignment> roles = restService.getRoleAssignmentsForUser(name);
        List<ScopedGrantedAuthority> scopedAuthorities = ScopedGrantedAuthority.fromRoleAssignments(roles);

        if(scopedAuthorities != null) {
            grantedAuths.addAll(scopedAuthorities);
        }

        try{
            boolean isAdmin = restService.isAdmin(new ValidateUser(name, null));
            if (isAdmin) {
                grantedAuths.add(new SimpleGrantedAuthority(RoleName.ROLE_IS_ADMIN));
            }
        } catch(Exception e){
            logger.error("Error when trying to check if user is admin with Broker!",e);
        }

        Collection<GrantedAuthority> adminAuthorities = getAdminAuthorities(authentication);
        if (adminAuthorities != null && !adminAuthorities.isEmpty()) {
            logger.info("Granting user " + name + " " + RoleName.ROLE_ADMIN);
            grantedAuths.add(new SimpleGrantedAuthority(RoleName.ROLE_ADMIN));
            grantedAuths.addAll(adminAuthorities);
        }

        logger.info("Granting user " + name + " " + RoleName.ROLE_USER);
        grantedAuths.add(new SimpleGrantedAuthority(RoleName.ROLE_USER));
        AuthenticationUtils.validateGrantedAuthorities(grantedAuths);
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
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }
}
