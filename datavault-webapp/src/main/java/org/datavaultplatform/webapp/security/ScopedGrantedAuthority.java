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
import java.util.stream.Collectors;

public class ScopedGrantedAuthority implements GrantedAuthority {

    private final Class type;
    private final Serializable id;
    private final Collection<Permission> permissions;

    public ScopedGrantedAuthority(Class type, String id, Collection<Permission> permissions) {
        this.type = type;
        this.id = id;
        this.permissions = permissions;
    }

    @Override
    public String getAuthority() {
        return null;
    }

    public Serializable getId() {
        return id;
    }

    public Class getType() {
        return type;
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public static List<ScopedGrantedAuthority> fromRoleAssignments(Collection<RoleAssignment> roleAssignments) {
        List<ScopedGrantedAuthority> authorities = new ArrayList<>();

        for (RoleAssignment assignment : roleAssignments) {
            Vault vault = assignment.getVault();
            Group group = assignment.getSchool();

            Collection<Permission> permissions = assignment.getRole().getPermissions()
                    .stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toList());

            if (vault != null) {
                authorities.add(new ScopedGrantedAuthority(Vault.class, vault.getID(), permissions));
            } else if (group != null) {
                authorities.add(new ScopedGrantedAuthority(Group.class, group.getID(), permissions));
            }
        }

        return authorities;
    }
}
