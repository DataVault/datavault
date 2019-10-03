package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Sets;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;


@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Roles")
public class RoleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, columnDefinition = "TEXT")
    private RoleType type;

    @ManyToMany
    @JoinTable(
            name = "Role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Collection<PermissionModel> permissions;

    private int assignedUserCount;

    public RoleModel() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        return permissions;
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

    public void setAssignedUserCount(int assignedUserCount) {
        this.assignedUserCount = assignedUserCount;
    }

    public int getAssignedUserCount() {
        return assignedUserCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleModel roleModel = (RoleModel) o;
        return Objects.equals(id, roleModel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
