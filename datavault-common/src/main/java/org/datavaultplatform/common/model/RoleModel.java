

package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Objects;
import org.hibernate.Hibernate;


@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Roles")
@NamedEntityGraph(name=RoleModel.EG_ROLE_MODEL, attributeNodes =
    @NamedAttributeNode(value = RoleModel_.PERMISSIONS, subgraph = "subPermModel"),
    subgraphs = @NamedSubgraph(name="subPermModel", attributeNodes = @NamedAttributeNode(PermissionModel_.PERMISSION))
)
public class RoleModel {
    public static final String EG_ROLE_MODEL = "eg.RoleModel.1";

    @Id
    @SequenceGenerator(name = "hibSEQ", sequenceName = "hibernate_sequence")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "hibSEQ")
    private Long id;

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, columnDefinition = "TEXT")
    private RoleType type;

    @Column(name = "role_status", nullable = false, columnDefinition = "TEXT")
    private String status;


    @ManyToMany
    @JoinTable(
            name = "Role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private final List<PermissionModel> permissions = new ArrayList<>();

    private int assignedUserCount;

    public RoleModel() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdAsString() {
        return MessageFormat.format("{0,number,#}", this.id);
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

    public synchronized Collection<PermissionModel> getPermissions() {
        return Sets.newHashSet(permissions);
    }

    public synchronized void setPermissions(Collection<PermissionModel> permissions) {
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }

    public synchronized boolean addPermission(PermissionModel permissionModel) {
        if (!hasPermission(permissionModel.getPermission())) {
            permissions.add(permissionModel);
            return true;
        }
        return false;
    }

    public synchronized boolean hasPermission(Permission permission) {
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
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        RoleModel roleModel = (RoleModel) o;
        return id != null && Objects.equals(id, roleModel.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}