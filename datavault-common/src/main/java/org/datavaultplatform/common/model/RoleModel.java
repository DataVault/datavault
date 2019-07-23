package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Sets;

import javax.persistence.*;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Roles")
public class RoleModel {

    @Id
    @Column(name = "id", unique = true, length = 36)
    private String id;

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(name = "role_type", nullable = false, columnDefinition = "TEXT")
    private RoleType type;

    @ManyToMany
    @JoinTable(
            name = "Role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Collection<PermissionModel> permissions;

    public RoleModel() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoleType getType() {
        return type;
    }

    public void setType(RoleType type) {
        this.type = type;
    }

    public Collection<PermissionModel> getPermissions() {
        return Sets.newHashSet(permissions);
    }

    public void setPermissions(Collection<PermissionModel> permissions) {
        this.permissions = permissions;
    }

    public boolean addPermission(PermissionModel permissionModel) {
        if (!hasPermission(permissionModel.getPermission())) {
            permissions.add(permissionModel);
            return true;
        }
        return false;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.stream()
                .map(PermissionModel::getPermission)
                .anyMatch(p -> p.equals(permission));
    }

    public int getAssignedUserCount() {
        // TODO we need to provide the count of assigned users with the role but won't have a working impl until the
        // stories to assign users to roles are developed
        return 0;
    }
}
