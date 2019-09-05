package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.webapp.model.AdminDashboardPermissionsModel;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

public class PermissionsService {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public AdminDashboardPermissionsModel getDashboardPermissions(Principal principal) {
        AdminDashboardPermissionsModel permissionsModel = new AdminDashboardPermissionsModel(principal);

        List<RoleAssignment> dashboardRoleAssignments = restService.getRoleAssignmentsForUser(principal.getName()).stream()
                .filter(this::isDashboardRoleAssignment)
                .collect(Collectors.toList());

        for (RoleAssignment roleAssignment : dashboardRoleAssignments) {
            List<PermissionModel> permissions = roleAssignment.getRole().getPermissions().stream()
                    .filter(p -> p.getPermission().isDashboardPermission())
                    .collect(Collectors.toList());
            for (PermissionModel permission : permissions) {
                if (isGlobalPermission(roleAssignment, permission)) {
                    permissionsModel.addGlobalPermission(permission);
                } else if (PermissionModel.PermissionType.SCHOOL == permission.getType()) {
                    permissionsModel.addSchoolScopedPermission(permission, roleAssignment.getSchoolId());
                } else if (PermissionModel.PermissionType.VAULT == permission.getType()) {
                    permissionsModel.addScopedPermission(permission, roleAssignment.getVault());
                }
            }
        }

        return permissionsModel;
    }

    private boolean isDashboardRoleAssignment(RoleAssignment roleAssignment) {
        return roleAssignment.getRole().getPermissions().stream()
                .anyMatch(p -> p.getPermission().isDashboardPermission());
    }

    private boolean isGlobalPermission(RoleAssignment roleAssignment, PermissionModel dashboardPermission) {
        boolean isAdminPermission = PermissionModel.PermissionType.ADMIN == dashboardPermission.getType();
        boolean isSchoolPermissionOnGlobalAdminRole =
                PermissionModel.PermissionType.SCHOOL == dashboardPermission.getType()
                        && RoleType.ADMIN == roleAssignment.getRole().getType()
                        && roleAssignment.getSchoolId() == null;
        boolean isVaultPermissionOnGlobalAdminRole =
                PermissionModel.PermissionType.VAULT == dashboardPermission.getType()
                        && RoleType.ADMIN == roleAssignment.getRole().getType()
                        && roleAssignment.getVault() == null;
        return isAdminPermission || isSchoolPermissionOnGlobalAdminRole || isVaultPermissionOnGlobalAdminRole;
    }
}
