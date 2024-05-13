package org.datavaultplatform.webapp.security;

import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.Vault;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScopedGrantedAuthority implements GrantedAuthority {

    private final Class<?> type;
    private final Serializable id;
    private final Collection<Permission> permissions;

    public ScopedGrantedAuthority(Class<?> type, String id, Collection<Permission> permissions) {
        this.type = type;
        this.id = id;
        this.permissions = permissions;
    }

    @Override
    public String getAuthority() {
        return "n/a";
    }

    public Serializable getId() {
        return id;
    }

    public Class<?> getType() {
        return type;
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public static List<ScopedGrantedAuthority> fromRoleAssignments(Collection<RoleAssignment> roleAssignments) {
        List<ScopedGrantedAuthority> authorities = new ArrayList<>();

        for (RoleAssignment assignment : roleAssignments) {
            String vaultId = assignment.getVaultId();
            String groupId = assignment.getSchoolId();

            Collection<Permission> permissions = assignment.getRole().getPermissions()
                    .stream()
                    .map(PermissionModel::getPermission)
                    .toList();

            if (vaultId != null) {
                authorities.add(new ScopedGrantedAuthority(Vault.class, vaultId, permissions));
            } else if (groupId != null) {
                authorities.add(new ScopedGrantedAuthority(Group.class, groupId, permissions));
            }
        }

        return authorities;
    }
}
