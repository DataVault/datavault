package org.datavaultplatform.webapp.security;

import org.datavaultplatform.common.model.RoleAssignment;
import org.springframework.security.core.GrantedAuthority;

public class ScopedGrantedAuthority implements GrantedAuthority {

    private final RoleAssignment roleAssignment;

    public ScopedGrantedAuthority(RoleAssignment roleAssignment) {
        this.roleAssignment = roleAssignment;
    }

    @Override
    public String getAuthority() {
        return null;
    }

    public RoleAssignment getRoleAssignment() {
        return roleAssignment;
    }
}
