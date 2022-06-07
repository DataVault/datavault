package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.dao.PermissionDAO;
import org.datavaultplatform.common.model.dao.RoleAssignmentDAO;
import org.datavaultplatform.common.model.dao.RoleDAO;
import org.datavaultplatform.common.util.RoleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RolesAndPermissionsService {
    private final Logger logger = LoggerFactory.getLogger(RolesAndPermissionsService.class);

    private final RoleDAO roleDao;

    private final PermissionDAO permissionDao;

    private final RoleAssignmentDAO roleAssignmentDao;

    private final UsersService usersService;

    @Autowired
    public RolesAndPermissionsService(RoleDAO roleDao, PermissionDAO permissionDao,
        RoleAssignmentDAO roleAssignmentDao, UsersService usersService) {
        this.roleDao = roleDao;
        this.permissionDao = permissionDao;
        this.roleAssignmentDao = roleAssignmentDao;
        this.usersService = usersService;
    }

    public void initialiseRolesAndPermissions() {
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
        roleDao.save(role);
        return role;
    }

    private boolean roleExists(Long roleId) {
        return roleId != null && roleDao.findById(roleId).isPresent();
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
        logger.debug("Got past the rolesandpermissionsservice validation");
        roleAssignmentDao.save(roleAssignment);
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
        if (RoleType.VAULT == roleAssignment.getRole().getType() && (roleAssignment.getVaultId() == null
                && roleAssignment.getPendingVaultId() == null)) {
            throw new IllegalStateException("Cannot create vault role assignment without a vault");
        }
        if (roleAssignmentDao.roleAssignmentExists(roleAssignment)) {
            throw new IllegalStateException("Role assignment already exists");
        }
    }

    public List<PermissionModel> getSchoolPermissions() {
        return permissionDao.findByType(PermissionModel.PermissionType.SCHOOL);
    }

    public List<PermissionModel> getVaultPermissions() {
        return permissionDao.findByType(PermissionModel.PermissionType.VAULT);
    }

    public RoleModel getRole(long id) {
        return roleDao.findById(id).orElse(null);
    }

    public RoleModel getIsAdmin() {
        return roleDao.getIsAdmin();
    }

    public RoleModel getDataOwner() {
        return roleDao.getDataOwner();
    }

    public RoleModel getDepositor() {
        return roleDao.getDepositor();
    }

    public RoleModel getNominatedDataManager() {
        return roleDao.getNominatedDataManager();
    }

    public RoleModel getVaultCreator() {
        return roleDao.getVaultCreator();
    }

    public List<RoleModel> getEditableRoles() {
        return roleDao.findAllEditableRoles();
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
        return roleAssignmentDao.findById(id).orElse(null);
    }

    public List<RoleAssignment> getRoleAssignmentsForSchool(String schoolId) {
        return roleAssignmentDao.findBySchoolId(schoolId);
    }

    public List<RoleAssignment> getRoleAssignmentsForVault(String vaultId) {
        return roleAssignmentDao.findByVaultId(vaultId);
    }

    public List<RoleAssignment> getRoleAssignmentsForPendingVault(String vaultId) {
        return roleAssignmentDao.findByPendingVaultId(vaultId);
    }

    public User getVaultOwner(String vaultId) {
        List<RoleAssignment> assignments = roleAssignmentDao.findByVaultId(vaultId);
        RoleAssignment ownerAssignment = assignments.stream()
                .filter(RoleUtils::isDataOwner)
                .findAny()
                .orElse(null);
        if(ownerAssignment == null){
            return null;
        }
        return usersService.getUser(ownerAssignment.getUserId());
    }

    public User getPendingVaultOwner(String pendingVaultId) {
        List<RoleAssignment> assignments = roleAssignmentDao.findByPendingVaultId(pendingVaultId);
        RoleAssignment ownerAssignment = assignments.stream()
                .filter(RoleUtils::isDataOwner)
                .findAny()
                .orElse(null);
        if(ownerAssignment == null){
            return null;
        }
        return usersService.getUser(ownerAssignment.getUserId());
    }

    public User getPendingVaultCreator(String pendingVaultId) {
        List<RoleAssignment> assignments = roleAssignmentDao.findByPendingVaultId(pendingVaultId);
        RoleAssignment ownerAssignment = assignments.stream()
                .filter(RoleUtils::isVaultCreator)
                .findAny()
                .orElse(null);
        if(ownerAssignment == null){
            return null;
        }
        return usersService.getUser(ownerAssignment.getUserId());
    }

    public List<User> getPendingVaultNDMs(String pendingVaultId) {
        List<RoleAssignment> assignments = roleAssignmentDao.findByPendingVaultId(pendingVaultId);
        List<RoleAssignment> ndmAssignments = assignments.stream()
                .filter(RoleUtils::isNominatedDataManager)
                .collect(Collectors.toList());
        List<User> retVal = new ArrayList<>();
        if(ndmAssignments == null){
            return null;
        } else {
            for (RoleAssignment ra : ndmAssignments) {
                User u = usersService.getUser(ra.getUserId());
                if (u != null){
                    retVal.add(u);
                }
            }
        }


        return retVal;
    }

    public List<User> getPendingVaultDepositors(String pendingVaultId) {
        List<RoleAssignment> assignments = roleAssignmentDao.findByPendingVaultId(pendingVaultId);
        List<RoleAssignment> ndmAssignments = assignments.stream()
                .filter(RoleUtils::isDepositor)
                .collect(Collectors.toList());
        List<User> retVal = new ArrayList<>();
        if(ndmAssignments == null){
            return null;
        } else {
            for (RoleAssignment ra : ndmAssignments) {
                User u = usersService.getUser(ra.getUserId());
                if (u != null){
                    retVal.add(u);
                }
            }
        }


        return retVal;
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

    public Set<Permission> getUserPermissions(String userId) {
        return roleAssignmentDao.findUserPermissions(userId);
    }

    public boolean hasPermission(String userId, Permission permission) {
        return roleAssignmentDao.hasPermission(userId, permission);
    }

    public boolean isAdminUser(String userId) {
        return roleAssignmentDao.isAdminUser(userId);
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
        RoleAssignment original = roleAssignmentDao.findById(roleAssignment.getId()).orElse(null);
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
        roleDao.deleteById(roleId);
    }

    public void deleteRoleAssignment(Long roleAssignmentId) {
        if (roleAssignmentDao.findById(roleAssignmentId) == null) {
            throw new IllegalStateException("Cannot delete a role assignment that does not exist");
        }
        roleAssignmentDao.deleteById(roleAssignmentId);
    }
}
