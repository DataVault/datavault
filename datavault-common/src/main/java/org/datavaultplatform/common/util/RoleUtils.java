package org.datavaultplatform.common.util;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleAssignment;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleUtils {

    public static final String IS_ADMIN_ROLE_NAME = "IS Admin";
    public static final String IS_ADMIN_ROLE_DESCRIPTION = "An admin of the whole system, with full permissions over the system.";
    public static final String DATA_OWNER_ROLE_NAME = "Data Owner";
    public static final String DATA_OWNER_ROLE_DESCRIPTION = "An admin of a specific vault, with full permissions over that vault.";

    private RoleUtils() {}

    public static boolean isReservedRoleName(String roleName) {
        return IS_ADMIN_ROLE_NAME.equalsIgnoreCase(roleName) || DATA_OWNER_ROLE_NAME.equalsIgnoreCase(roleName);
    }

    public static boolean isDataOwner(RoleAssignment roleAssignment) {
        return DATA_OWNER_ROLE_NAME.equals(roleAssignment.getRole().getName());
    }

    public static boolean isISAdmin(RoleAssignment roleAssignment) {
        return IS_ADMIN_ROLE_NAME.equals(roleAssignment.getRole().getName());
    }

    public static boolean isRoleInVault(RoleAssignment roleAssignment, String vaultId) {
        return roleAssignment.getVaultId() != null && roleAssignment.getVaultId().equals(vaultId);
    }

    public static boolean isRoleInSchool(RoleAssignment roleAssignment, String schoolId) {
        return roleAssignment.getSchoolId() != null && roleAssignment.getSchoolId().equals(schoolId);
    }

    public static boolean hasPermission(RoleAssignment roleAssignment, Permission permission) {
        return roleAssignment.getRole().getPermissions().stream()
                .anyMatch(permissionModel -> permission == permissionModel.getPermission());
    }

    public static boolean hasReducedPermissions(Collection<PermissionModel> originalPermissions,
                                                Collection<PermissionModel> newPermissions) {
        Set<Permission> original = originalPermissions.stream()
                .map(PermissionModel::getPermission)
                .collect(Collectors.toSet());
        Set<Permission> updated = newPermissions.stream()
                .map(PermissionModel::getPermission)
                .collect(Collectors.toSet());
        return !updated.containsAll(original);
    }
}
