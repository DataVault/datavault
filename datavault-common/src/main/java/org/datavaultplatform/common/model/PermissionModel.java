package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Permissions")
public class PermissionModel {

    public enum PermissionType {
        SCHOOL,
        VAULT,
        ADMIN
    }

    @Id
    @Column(name = "id", unique = true, length = 36)
    private String id;

    @Column(name = "permission", unique = true, nullable = false, columnDefinition = "TEXT")
    private Permission permission;

    @Column(name = "label", nullable = false, columnDefinition = "TEXT")
    private String label;

    @Column(name = "type", nullable = false, columnDefinition = "TEXT")
    private PermissionType type;

    public PermissionModel() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public PermissionType getType() {
        return type;
    }

    public void setType(PermissionType type) {
        this.type = type;
    }

    public boolean isSchoolPermission() {
        return type == PermissionType.SCHOOL;
    }

    public boolean isVaultPermission() {
        return type == PermissionType.VAULT;
    }

    public boolean isAdminPermission() {
        return type == PermissionType.ADMIN;
    }

    public boolean isAllowedForRoleType(RoleType roleType) {
        switch (roleType) {
            case ADMIN:
                return true;
            case SCHOOL:
                return isSchoolPermission();
            case VAULT:
                return isVaultPermission();
            default:
                return false;
        }
    }
}
