package org.datavaultplatform.common.model;

public enum Permission {

    // Vault permissions
    VAULT_PERMISSION_1("VAULT_PERMISSION_1", PermissionModel.PermissionType.VAULT, "Vault Permission 1"),
    VAULT_PERMISSION_2("VAULT_PERMISSION_2", PermissionModel.PermissionType.VAULT, "Vault Permission 2"),
    VAULT_PERMISSION_3("VAULT_PERMISSION_3", PermissionModel.PermissionType.VAULT, "Vault Permission 3"),
    VAULT_PERMISSION_4("VAULT_PERMISSION_4", PermissionModel.PermissionType.VAULT, "Vault Permission 4"),
    VAULT_PERMISSION_5("VAULT_PERMISSION_5", PermissionModel.PermissionType.VAULT, "Vault Permission 5"),

    // School permissions
    CAN_VIEW_VAULTS_SIZE("CAN_VIEW_VAULTS_SIZE", PermissionModel.PermissionType.SCHOOL, "View school vaults size"),
    CAN_VIEW_IN_PROGRESS_DEPOSITS("CAN_VIEW_IN_PROGRESS_DEPOSITS", PermissionModel.PermissionType.SCHOOL, "View school deposits/retrieves in progress"),
    CAN_VIEW_DEPOSIT_QUEUE("CAN_VIEW_DEPOSIT_QUEUE", PermissionModel.PermissionType.SCHOOL, "View school deposit/retrieval queues"),
    CAN_MANAGE_VAULTS("CAN_MANAGE_VAULTS", PermissionModel.PermissionType.SCHOOL, "Manage school vaults"),
    CAN_MANAGE_DEPOSITS("CAN_MANAGE_DEPOSITS", PermissionModel.PermissionType.SCHOOL, "Manage school vault deposits"),
    CAN_VIEW_RETRIEVES("CAN_VIEW_RETRIEVES", PermissionModel.PermissionType.SCHOOL, "View school retrieves"),
    CAN_MANAGE_SCHOOL_USERS("CAN_MANAGE_SCHOOL_USERS", PermissionModel.PermissionType.SCHOOL, "Manage school role assignments"),

    // Admin permissions
    CAN_MANAGE_BILLING_DETAILS("CAN_MANAGE_BILLING_DETAILS", PermissionModel.PermissionType.ADMIN, "Manage billing details"),
    CAN_MANAGE_ROLES("CAN_MANAGE_ROLES", PermissionModel.PermissionType.ADMIN, "Manage roles"),
    CAN_VIEW_EVENTS("CAN_VIEW_EVENTS", PermissionModel.PermissionType.ADMIN, "View events"),
    CAN_MANAGE_RETENTION_POLICIES("CAN_MANAGE_RETENTION_POLICIES", PermissionModel.PermissionType.ADMIN, "Manage retention policies"),
    CAN_MANAGE_ARCHIVE_STORES("CAN_MANAGE_ARCHIVE_STORES", PermissionModel.PermissionType.ADMIN, "Manage archive stores");

    private final String id;

    private final PermissionModel.PermissionType defaultType;

    private final String defaultLabel;

    Permission(String id, PermissionModel.PermissionType defaultType, String defaultLabel) {
        this.id = id;
        this.defaultType = defaultType;
        this.defaultLabel = defaultLabel;
    }

    public String getId() {
        return id;
    }

    public PermissionModel.PermissionType getDefaultType() {
        return defaultType;
    }

    public String getDefaultLabel() {
        return defaultLabel;
    }
}
