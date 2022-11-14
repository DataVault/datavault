package org.datavaultplatform.webapp.authentication.shib;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.webapp.model.AdminDashboardPermissionsModel;
import org.datavaultplatform.webapp.security.ScopedGrantedAuthority;
import org.datavaultplatform.webapp.services.PermissionsService;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Slf4j
public class ShibGrantedAuthorityService {

    private final RestService restService;

    private final PermissionsService permissionsService;

    public ShibGrantedAuthorityService(RestService restService, PermissionsService permissionsService) {
        this.restService = restService;
        this.permissionsService = permissionsService;
    }

    public List<GrantedAuthority> getGrantedAuthoritiesForUser(String name, Authentication authentication) {
        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        List<RoleAssignment> roles = restService.getRoleAssignmentsForUser(name);
        List<ScopedGrantedAuthority> scopedAuthorities = ScopedGrantedAuthority.fromRoleAssignments(roles);

        grantedAuths.addAll(scopedAuthorities);

        log.info("Existing user {}", name);
        boolean isAdmin;
        try {
            isAdmin = restService.isAdmin(new ValidateUser(name, null));
            if (isAdmin) {
                grantedAuths.add(ShibUtils.ROLE_IS_ADMIN);
            }
        } catch (Exception ex) {
            log.error("Error when trying to check if user is admin with Broker!", ex);
        }

        Collection<GrantedAuthority> adminAuthorities = getAdminAuthorities(authentication);
        if (!adminAuthorities.isEmpty()) {
            log.info("Granting user {} ROLE_ADMIN", name);
            grantedAuths.add(ShibUtils.ROLE_ADMIN);
            grantedAuths.addAll(adminAuthorities);
        }
        return grantedAuths;
    }

    private Collection<GrantedAuthority> getAdminAuthorities(Principal principal) {
        AdminDashboardPermissionsModel adminPermissions = permissionsService.getDashboardPermissions(principal);
        return adminPermissions.getUnscopedPermissions().stream()
                .filter(p -> p.getPermission().getRoleName() != null)
                .map(p -> new SimpleGrantedAuthority(p.getPermission().getRoleName()))
                .collect(Collectors.toSet());
    }
}
