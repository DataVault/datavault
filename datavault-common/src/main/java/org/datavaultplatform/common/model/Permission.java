package org.datavaultplatform.common.model;

public enum Permission {

    // Vault permissions
    VAULT_PERMISSION_1("VAULT_PERMISSION_1", PermissionModel.PermissionType.VAULT, "Vault Permission 1", false),
    VAULT_PERMISSION_2("VAULT_PERMISSION_2", PermissionModel.PermissionType.VAULT, "Vault Permission 2", false),
    VAULT_PERMISSION_3("VAULT_PERMISSION_3", PermissionModel.PermissionType.VAULT, "Vault Permission 3", false),
    VAULT_PERMISSION_4("VAULT_PERMISSION_4", PermissionModel.PermissionType.VAULT, "Vault Permission 4", false),
    VAULT_PERMISSION_5("VAULT_PERMISSION_5", PermissionModel.PermissionType.VAULT, "Vault Permission 5", false),

    // School permissions
    CAN_VIEW_VAULTS_SIZE("CAN_VIEW_VAULTS_SIZE", PermissionModel.PermissionType.SCHOOL, "View school vaults size", true),
    CAN_VIEW_IN_PROGRESS_DEPOSITS("CAN_VIEW_IN_PROGRESS_DEPOSITS", PermissionModel.PermissionType.SCHOOL, "View school deposits/retrieves in progress", true),
    CAN_VIEW_DEPOSIT_QUEUE("CAN_VIEW_DEPOSIT_QUEUE", PermissionModel.PermissionType.SCHOOL, "View school deposit/retrieval queues", true),
    CAN_MANAGE_VAULTS("CAN_MANAGE_VAULTS", PermissionModel.PermissionType.SCHOOL, "Manage school vaults", true),
    CAN_MANAGE_DEPOSITS("CAN_MANAGE_DEPOSITS", PermissionModel.PermissionType.SCHOOL, "Manage school vault deposits", true),
    CAN_VIEW_RETRIEVES("CAN_VIEW_RETRIEVES", PermissionModel.PermissionType.SCHOOL, "View school retrieves", true),
    CAN_MANAGE_SCHOOL_USERS("CAN_MANAGE_SCHOOL_USERS", PermissionModel.PermissionType.SCHOOL, "Manage school role assignments", true),

    // Admin permissions
    CAN_MANAGE_BILLING_DETAILS("CAN_MANAGE_BILLING_DETAILS", PermissionModel.PermissionType.ADMIN, "Manage billing details", true),
    CAN_MANAGE_ROLES("CAN_MANAGE_ROLES", PermissionModel.PermissionType.ADMIN, "Manage roles", true),
    CAN_VIEW_EVENTS("CAN_VIEW_EVENTS", PermissionModel.PermissionType.ADMIN, "View events", true),
    CAN_MANAGE_RETENTION_POLICIES("CAN_MANAGE_RETENTION_POLICIES", PermissionModel.PermissionType.ADMIN, "Manage retention policies", true),
    CAN_MANAGE_ARCHIVE_STORES("CAN_MANAGE_ARCHIVE_STORES", PermissionModel.PermissionType.ADMIN, "Manage archive stores", true);

    private final String id;

    private final PermissionModel.PermissionType defaultType;

    private final String defaultLabel;

    private final boolean dashboardPermission;

    Permission(String id, PermissionModel.PermissionType defaultType, String defaultLabel, boolean dashboardPermission) {
        this.id = id;
        this.defaultType = defaultType;
        this.defaultLabel = defaultLabel;
        this.dashboardPermission = dashboardPermission;
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

    public boolean isDashboardPermission() {
        return dashboardPermission;
    }
}
