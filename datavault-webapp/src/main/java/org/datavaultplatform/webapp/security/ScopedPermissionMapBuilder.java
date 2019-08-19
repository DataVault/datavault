package org.datavaultplatform.webapp.security;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.Vault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class ScopedPermissionMapBuilder {
    public static Multimap<ScopedPermissionKey, Permission> createPermissionMap(Authentication authentication) {
        Multimap<ScopedPermissionKey, Permission> permissionMap = MultimapBuilder
                .hashKeys()
                .enumSetValues(Permission.class)
                .build();

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (!(authority instanceof ScopedGrantedAuthority)) {
                continue;
            }

            RoleAssignment assignment = ((ScopedGrantedAuthority) authority).getRoleAssignment();
            Vault vault = assignment.getVault();
            Group group = assignment.getSchool();

            final ScopedPermissionKey key;
            if (vault != null) {
                key = new ScopedPermissionKey(vault.getID(), Vault.class);
            } else if (group != null) {
                key = new ScopedPermissionKey(group.getID(), Group.class);
            } else {
                continue;
            }

            assignment.getRole().getPermissions()
                    .stream()
                    .map(PermissionModel::getPermission)
                    .forEach(permission -> permissionMap.put(key, permission));

        }

        return permissionMap;
    }
}
