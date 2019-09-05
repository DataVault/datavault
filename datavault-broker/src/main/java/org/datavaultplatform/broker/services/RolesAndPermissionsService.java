package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.dao.PermissionDAO;
import org.datavaultplatform.common.model.dao.RoleAssignmentDAO;
import org.datavaultplatform.common.model.dao.RoleDAO;
import org.datavaultplatform.common.util.RoleUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RolesAndPermissionsService implements ApplicationListener<ContextRefreshedEvent> {

    private RoleDAO roleDao;

    private PermissionDAO permissionDao;

    private RoleAssignmentDAO roleAssignmentDao;

    public void setRoleDao(RoleDAO roleDao) {
        this.roleDao = roleDao;
    }

    public void setPermissionDao(PermissionDAO permissionDao) {
        this.permissionDao = permissionDao;
    }

    public void setRoleAssignmentDao(RoleAssignmentDAO roleAssignmentDao) {
        this.roleAssignmentDao = roleAssignmentDao;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        permissionDao.synchronisePermissions();
        roleDao.storeSpecialRoles();
    }

    public RoleModel createRole(RoleModel role) {
        if (roleExists(role.getId())) {
            throw new IllegalStateException("Cannot create a role that already exists");
        }
        if (RoleUtils.isReservedRoleName(role.getName())) {
            throw new IllegalStateException("Cannot create a role with reserved role name: " + role.getName());
        }
        validateRolePermissions(role);
        roleDao.store(role);
        return role;
    }

    private boolean roleExists(Long roleId) {
        return roleId != null && roleDao.find(roleId) != null;
    }

    private void validateRolePermissions(RoleModel role) {
        for (PermissionModel appliedPermission : role.getPermissions()) {
            RoleType roleType = role.getType();
            if (!appliedPermission.isAllowedForRoleType(roleType)) {
                String permissionType = appliedPermission.getType().name();
                String msg = "Unable to apply permission with type " + permissionType + " to role of type " + roleType;
                throw new IllegalStateException(msg);
            }
        }
    }

    public RoleAssignment createRoleAssignment(RoleAssignment roleAssignment) {
        validateRoleAssignment(roleAssignment);
        roleAssignmentDao.store(roleAssignment);
        return roleAssignment;
    }

    private void validateRoleAssignment(RoleAssignment roleAssignment) {
        if (roleAssignment.getUserId() == null) {
            throw new IllegalStateException("Cannot create role assignment without user");
        }
        if (roleAssignment.getRole() == null) {
            throw new IllegalStateException("Cannot create role assignment without role");
        }
        if (RoleType.SCHOOL == roleAssignment.getRole().getType() && roleAssignment.getSchoolId() == null) {
            throw new IllegalStateException("Cannot create school role assignment without a school");
        }
        if (RoleType.VAULT == roleAssignment.getRole().getType() && roleAssignment.getVaultId() == null) {
            throw new IllegalStateException("Cannot create vault role assignment without a vault");
        }
        if (roleAssignmentDao.roleAssignmentExists(roleAssignment)) {
            throw new IllegalStateException("Role assignment already exists");
        }
    }

    public List<PermissionModel> getSchoolPermissions() {
        return getFilteredPermissions(PermissionModel::isSchoolPermission);
    }

    public List<PermissionModel> getVaultPermissions() {
        return getFilteredPermissions(PermissionModel::isVaultPermission);
    }

    private List<PermissionModel> getFilteredPermissions(Predicate<PermissionModel> filter) {
        return permissionDao.findAll().stream().filter(filter).collect(Collectors.toList());
    }

    public RoleModel getRole(long id) {
        return roleDao.find(id);
    }

    public RoleModel getIsAdmin() {
        return roleDao.getIsAdmin();
    }

    public RoleModel getDataOwner() {
        return roleDao.getDataOwner();
    }

    public List<RoleModel> getEditableRoles() {
        return roleDao.findAll().stream()
                .filter(role -> role.getType().isCustomCreatable())
                .collect(Collectors.toList());
    }

    public List<RoleModel> getViewableRoles() {
        return Collections.singletonList(roleDao.getDataOwner());
    }

    public List<RoleModel> getSchoolRoles() {
        return new ArrayList<>(roleDao.findAll(RoleType.SCHOOL));
    }

    public List<RoleModel> getVaultRoles() {
        return new ArrayList<>(roleDao.findAll(RoleType.VAULT));
    }

    public RoleAssignment getRoleAssignment(long id) {
        return roleAssignmentDao.find(id);
    }

    public List<RoleAssignment> getRoleAssignmentsForSchool(String schoolId) {
        return roleAssignmentDao.findBySchoolId(schoolId);
    }

    public List<RoleAssignment> getRoleAssignmentsForVault(String vaultId) {
        return roleAssignmentDao.findByVaultId(vaultId);
    }

    public List<RoleAssignment> getRoleAssignmentsForUser(String userId) {
        return roleAssignmentDao.findByUserId(userId);
    }

    public List<RoleAssignment> getRoleAssignmentsForRole(Long roleId) {
        return roleAssignmentDao.findByRoleId(roleId);
    }

    public boolean hasAdminDashboardPermissions(String userId) {
        return roleAssignmentDao.findByUserId(userId).stream()
                .flatMap(roleAssignment -> roleAssignment.getRole().getPermissions().stream())
                .anyMatch(permissionModel -> permissionModel.getPermission().isDashboardPermission());
    }

    public boolean hasPermission(String userId, Permission permission) {
        return roleAssignmentDao.findByUserId(userId).stream()
                .flatMap(roleAssignment -> roleAssignment.getRole().getPermissions().stream())
                .anyMatch(permissionModel -> permissionModel.getPermission() == permission);
    }

    public RoleModel updateRole(RoleModel role) {
        if (!roleExists(role.getId())) {
            throw new IllegalStateException("Cannot update a role that does not exist");
        }
        validateRolePermissions(role);
        roleDao.update(role);
        return role;
    }

    public RoleAssignment updateRoleAssignment(RoleAssignment roleAssignment) {
        RoleAssignment original = roleAssignmentDao.find(roleAssignment.getId());
        if (original == null) {
            throw new IllegalStateException("Cannot update a role assignment that does not exist");
        }
        if (!roleAssignment.equals(original)) {
            validateRoleAssignment(roleAssignment);
            roleAssignmentDao.update(roleAssignment);
        }
        return roleAssignment;
    }

    public void deleteRole(Long roleId) {
        if (!roleExists(roleId)) {
            throw new IllegalStateException("Cannot delete a role that does not exist");
        }
        roleDao.delete(roleId);
    }

    public void deleteRoleAssignment(Long roleAssignmentId) {
        if (roleAssignmentDao.find(roleAssignmentId) == null) {
            throw new IllegalStateException("Cannot delete a role assignment that does not exist");
        }
        roleAssignmentDao.delete(roleAssignmentId);
    }
}
