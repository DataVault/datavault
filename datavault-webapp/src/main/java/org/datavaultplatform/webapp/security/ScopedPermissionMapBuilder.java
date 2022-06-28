package org.datavaultplatform.webapp.security;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.datavaultplatform.common.model.Permission;
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

            ScopedGrantedAuthority scopedAuthority = (ScopedGrantedAuthority) authority;
            ScopedPermissionKey key = new ScopedPermissionKey(scopedAuthority.getId(), scopedAuthority.getType());

            permissionMap.putAll(key, scopedAuthority.getPermissions());
        }

        return permissionMap;
    }
}
