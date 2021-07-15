package org.datavaultplatform.common.util;

import org.datavaultplatform.common.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleUtils {

    public static final String IS_ADMIN_ROLE_NAME = "IS Admin";
    public static final String IS_ADMIN_ROLE_DESCRIPTION = "An admin of the whole system, with full permissions over the system.";
    public static final String DATA_OWNER_ROLE_NAME = "Data Owner";
    public static final String DATA_OWNER_ROLE_DESCRIPTION = "An admin of a specific vault, with full permissions over that vault.";
    public static final String DEPOSITOR_ROLE_NAME = "Depositor";
    public static final String DEPOSITOR_ROLE_DESCRIPTION = "Acting on behalf of the Data Owner, may view the vault, deposit data and retrieve any deposit in the vault.";
    public static final String NOMINATED_DATA_MANAGER_ROLE_NAME = "Nominated Data Manager";
    public static final String NOMINATED_DATA_MANAGER_ROLE_DESCRIPTION = "Acting on behalf of the Data Owner, may view the vault, edit metadata fields, deposit data and retrieve  any deposit in the vault. Can assign other users to the Depositor role. ";
    public static final String VAULT_CREATOR_ROLE_NAME = "Vault Creator";
    public static final String VAULT_CREATOR_ROLE_DESCRIPTION = "The creator of the vault pending acceptance to full vault status";

    private RoleUtils() {}

    public static boolean isReservedRoleName(String roleName) {
        return IS_ADMIN_ROLE_NAME.equalsIgnoreCase(roleName) || DATA_OWNER_ROLE_NAME.equalsIgnoreCase(roleName);
    }

    public static boolean isDataOwner(RoleAssignment roleAssignment) {
        return DATA_OWNER_ROLE_NAME.equals(roleAssignment.getRole().getName());
    }

    public static boolean isVaultCreator(RoleAssignment roleAssignment) {
        return VAULT_CREATOR_ROLE_NAME.equals(roleAssignment.getRole().getName());
    }

    public static boolean isNominatedDataManager(RoleAssignment roleAssignment) {
        return NOMINATED_DATA_MANAGER_ROLE_NAME.equals(roleAssignment.getRole().getName());
    }

    public static boolean isISAdmin(RoleAssignment roleAssignment) {
        return IS_ADMIN_ROLE_NAME.equals(roleAssignment.getRole().getName());
    }

    public static boolean isRoleOfName(RoleAssignment roleAssignment, String roleName) {
        return roleName.equals(roleAssignment.getRole().getName());
    }

    public static boolean isRoleInVault(RoleAssignment roleAssignment, String vaultId) {
        return roleAssignment.getVaultId() != null && roleAssignment.getVaultId().equals(vaultId);
    }

    public static boolean isRoleInPendingVault(RoleAssignment roleAssignment, String vaultId) {
        return roleAssignment.getPendingVaultId() != null && roleAssignment.getPendingVaultId().equals(vaultId);
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

    public static List<RoleModel> getAssignableRoles(List<RoleAssignment> roleAssignmentsForUser, List<RoleModel> vaultRoles) {
        //get users highest vault role status
        // find out the highest status vault role the user has for this vault
        // lower is highest!
        String highestStatus = null;
        boolean admin = false;
        for (RoleAssignment ra : roleAssignmentsForUser) {
            RoleModel rm = ra.getRole();
            if (rm.getType().equals(RoleType.VAULT)) {
                String roleStatus = rm.getStatus();
                if (highestStatus == null) {
                    highestStatus = roleStatus;
                } else if (Integer.parseInt(highestStatus) > Integer.parseInt(roleStatus)) {
                    highestStatus = roleStatus;
                }
            }
            if (rm.getType().equals(RoleType.ADMIN)) {
                admin = true;
            }
            if (rm.getType().equals(RoleType.SCHOOL)) {
                admin = true;
            }
        }

        // iterate over the roles and remove any with a higher or equal status
        List<RoleModel> validRoles = new ArrayList<>();
        for (RoleModel role : vaultRoles) {
            if (admin || (highestStatus != null && Integer.parseInt(highestStatus) < Integer.parseInt(role.getStatus()))) {
                validRoles.add(role);
            }
        }
        return validRoles;
    }
}
