package org.datavaultplatform.common.model;

public enum Permission {

    // Vault permissions
    VIEW_DEPOSITS_AND_RETRIEVES("VIEW_DEPOSITS_AND_RETRIEVES", PermissionModel.PermissionType.VAULT, "View deposits and retrieves", null, false),
    VIEW_VAULT_METADATA("VIEW_VAULT_METADATA", PermissionModel.PermissionType.VAULT, "View vault metadata", null, false),
    VIEW_VAULT_ROLES("VIEW_VAULT_ROLES", PermissionModel.PermissionType.VAULT, "View vault roles", null, false),
    ASSIGN_VAULT_ROLES("ASSIGN_VAULT_ROLES", PermissionModel.PermissionType.VAULT, "Assign vault roles", null, false),
    VIEW_VAULT_HISTORY("VIEW_VAULT_HISTORY", PermissionModel.PermissionType.VAULT, "View vault history", null, false),
    CAN_TRANSFER_VAULT_OWNERSHIP("CAN_TRANSFER_VAULT_OWNERSHIP", PermissionModel.PermissionType.VAULT, "Transfer vault ownership", null, false),

    // School permissions
    CAN_VIEW_VAULTS_SIZE("CAN_VIEW_VAULTS_SIZE", PermissionModel.PermissionType.SCHOOL, "Admin view vault size", "ROLE_ADMIN", true),
    CAN_VIEW_IN_PROGRESS("CAN_VIEW_IN_PROGRESS", PermissionModel.PermissionType.SCHOOL, "Admin view transfers", "ROLE_ADMIN", true),
    CAN_VIEW_QUEUES("CAN_VIEW_QUEUES", PermissionModel.PermissionType.SCHOOL, "Admin view school queues", "ROLE_ADMIN", true),
    CAN_MANAGE_VAULTS("CAN_MANAGE_VAULTS", PermissionModel.PermissionType.SCHOOL, "Administer school vaults", "ROLE_ADMIN_VAULTS", true),
    CAN_MANAGE_DEPOSITS("CAN_MANAGE_DEPOSITS", PermissionModel.PermissionType.SCHOOL, "Administer school deposits", "ROLE_ADMIN_DEPOSITS", true),
    CAN_VIEW_RETRIEVES("CAN_VIEW_RETRIEVES", PermissionModel.PermissionType.SCHOOL, "Admin view school retrievals", "ROLE_ADMIN_RETRIEVES", true),
    CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS("CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS", PermissionModel.PermissionType.SCHOOL, "Admin view school role assignments", "ROLE_ADMIN_SCHOOLS", true),
    CAN_ORPHAN_SCHOOL_VAULTS("CAN_ORPHAN_SCHOOL_VAULTS", PermissionModel.PermissionType.SCHOOL, "Orphan school vaults", null, false),
    MANAGE_SCHOOL_VAULT_DEPOSITS("MANAGE_SCHOOL_VAULT_DEPOSITS", PermissionModel.PermissionType.SCHOOL, "Manage school transfers", null, false),
    VIEW_SCHOOL_VAULT_METADATA("VIEW_SCHOOL_VAULT_METADATA", PermissionModel.PermissionType.SCHOOL, "View school vault metadata", null, false),
    VIEW_SCHOOL_VAULT_ROLES("VIEW_SCHOOL_VAULT_ROLES", PermissionModel.PermissionType.SCHOOL, "View school vault roles", null, false),
    ASSIGN_SCHOOL_VAULT_ROLES("ASSIGN_SCHOOL_VAULT_ROLES", PermissionModel.PermissionType.SCHOOL, "Assign school vault roles", null, false),
    VIEW_SCHOOL_VAULT_HISTORY("VIEW_SCHOOL_VAULT_HISTORY", PermissionModel.PermissionType.SCHOOL, "View school vault history", null, false),
    TRANSFER_SCHOOL_VAULT_OWNERSHIP("TRANSFER_SCHOOL_VAULT_OWNERSHIP", PermissionModel.PermissionType.SCHOOL, "Transfer school vault ownership", null, false),
    EDIT_SCHOOL_VAULT_METADATA("EDIT_SCHOOL_VAULT_METADATA", PermissionModel.PermissionType.SCHOOL, "Edit school vault metadata", null, false),
    DELETE_SCHOOL_VAULT_DEPOSITS("DELETE_SCHOOL_VAULT_DEPOSITS", PermissionModel.PermissionType.SCHOOL, "Delete school vault deposits", null, false),
    CAN_RETRIEVE_DATA("CAN_RETRIEVE_DATA", PermissionModel.PermissionType.SCHOOL, "Retrieve data", null, false),
    
    // Admin permissions
    CAN_MANAGE_BILLING_DETAILS("CAN_MANAGE_BILLING_DETAILS", PermissionModel.PermissionType.ADMIN, "Manage billing details", "ROLE_ADMIN_BILLING", true),
    CAN_MANAGE_ROLES("CAN_MANAGE_ROLES", PermissionModel.PermissionType.ADMIN, "Manage roles", "ROLE_ADMIN_ROLES", true),
    CAN_VIEW_EVENTS("CAN_VIEW_EVENTS", PermissionModel.PermissionType.ADMIN, "View events", "ROLE_ADMIN_EVENTS", true),
    CAN_MANAGE_RETENTION_POLICIES("CAN_MANAGE_RETENTION_POLICIES", PermissionModel.PermissionType.ADMIN, "Manage retention policies", "ROLE_ADMIN_RETENTIONPOLICIES", true),
    CAN_MANAGE_ARCHIVE_STORES("CAN_MANAGE_ARCHIVE_STORES", PermissionModel.PermissionType.ADMIN, "Manage archive stores", "ROLE_ADMIN_ARCHIVESTORES", true),
    CAN_MANAGE_SCHOOL_ROLE_ASSIGNMENTS("CAN_MANAGE_SCHOOL_ROLE_ASSIGNMENTS",PermissionModel.PermissionType.ADMIN, "Manage school role assignments", "ROLE_ADMIN_SCHOOLS", true),
    CAN_MANAGE_REVIEWS("CAN_MANAGE_REVIEWS", PermissionModel.PermissionType.ADMIN, "Manage Reviews", "ROLE_ADMIN_REVIEWS", true);

    private final String id;

    private final PermissionModel.PermissionType defaultType;

    private final String defaultLabel;

    private final String roleName;

    private final boolean dashboardPermission;

    Permission(String id, PermissionModel.PermissionType defaultType, String defaultLabel, String roleName, boolean dashboardPermission) {
        this.id = id;
        this.defaultType = defaultType;
        this.defaultLabel = defaultLabel;
        this.roleName = roleName;
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

    public String getRoleName() {
        return roleName;
    }

    public boolean isDashboardPermission() {
        return dashboardPermission;
    }
}
