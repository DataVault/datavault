package org.datavaultplatform.common.model;

public enum Permission {

    VAULT_PERMISSION_1(PermissionModel.PermissionType.VAULT, "Vault Permission 1"),
    VAULT_PERMISSION_2(PermissionModel.PermissionType.VAULT, "Vault Permission 2"),
    VAULT_PERMISSION_3(PermissionModel.PermissionType.VAULT, "Vault Permission 3"),
    VAULT_PERMISSION_4(PermissionModel.PermissionType.VAULT, "Vault Permission 4"),
    VAULT_PERMISSION_5(PermissionModel.PermissionType.VAULT, "Vault Permission 5"),

    SCHOOL_PERMISSION_1(PermissionModel.PermissionType.SCHOOL, "School Permission 1"),
    SCHOOL_PERMISSION_2(PermissionModel.PermissionType.SCHOOL, "School Permission 2"),
    SCHOOL_PERMISSION_3(PermissionModel.PermissionType.SCHOOL, "School Permission 3"),
    SCHOOL_PERMISSION_4(PermissionModel.PermissionType.SCHOOL, "School Permission 4"),
    SCHOOL_PERMISSION_5(PermissionModel.PermissionType.SCHOOL, "School Permission 5"),

    ADMIN_PERMISSION_1(PermissionModel.PermissionType.ADMIN, "Admin Permission 1"),
    ADMIN_PERMISSION_2(PermissionModel.PermissionType.ADMIN, "Admin Permission 2"),
    ADMIN_PERMISSION_3(PermissionModel.PermissionType.ADMIN, "Admin Permission 3"),
    ADMIN_PERMISSION_4(PermissionModel.PermissionType.ADMIN, "Admin Permission 4"),
    ADMIN_PERMISSION_5(PermissionModel.PermissionType.ADMIN, "Admin Permission 5");

    private final PermissionModel.PermissionType defaultType;

    private final String defaultLabel;

    Permission(PermissionModel.PermissionType defaultType, String defaultLabel) {
        this.defaultType = defaultType;
        this.defaultLabel = defaultLabel;
    }

    public PermissionModel.PermissionType getDefaultType() {
        return defaultType;
    }

    public String getDefaultLabel() {
        return defaultLabel;
    }
}
