package org.datavaultplatform.webapp.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminDashboardPermissionsModel {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardPermissionsModel.class);

    private static final String GLOBAL_SCOPE_IDENTIFIER = "*";

    private final Multimap<PermissionModel, String> scopedPermissions;

    private final Principal principal;

    public AdminDashboardPermissionsModel(Multimap<PermissionModel, String> scopedPermissions, Principal principal) {
        this.scopedPermissions = scopedPermissions;
        this.principal = principal;
    }

    public AdminDashboardPermissionsModel(Principal principal) {
        this(ArrayListMultimap.create(), principal);
    }

    public Principal getPrincipal() {
        return principal;
    }

    public Set<PermissionModel> getUnscopedPermissions() {
        return scopedPermissions.keySet();
    }

    public boolean canSeeAdminDashboard() {
        return scopedPermissions.size() > 0;
    }

    public boolean hasPermission(Permission permissionToCheckFor, String resourceId) {
        Set<String> permittedScope = getScope(permissionToCheckFor);
        return permittedScope.contains(GLOBAL_SCOPE_IDENTIFIER) || permittedScope.contains(resourceId);
    }

    public boolean hasPermission(Permission permissionToCheckFor) {
        return getScope(permissionToCheckFor).size() > 0;
    }

    public Set<String> getScope(Permission permission) {
        return scopedPermissions.entries().stream()
                .filter(entry -> entry.getKey().getPermission() == permission)
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    public void addGlobalPermission(PermissionModel permissionModel) {
        validateDashboardPermission(permissionModel);
        scopedPermissions.removeAll(permissionModel);
        scopedPermissions.put(permissionModel, GLOBAL_SCOPE_IDENTIFIER);
    }

    private void validateDashboardPermission(PermissionModel permissionModel) {
        if (!permissionModel.getPermission().isDashboardPermission()) {
            logger.error("Attempted to add non-dashboard permission with ID={} to dashboard permission list",
                    permissionModel.getId());
            throw new IllegalArgumentException("Not a dashboard permission: " + permissionModel.getPermission());
        }
    }

    public void addSchoolScopedPermission(PermissionModel permissionModel, String schoolId) {
        if (schoolId == null) {
            logger.error("Attempted to scope permission with ID={} to school, but no school was provided",
                    permissionModel.getId());
            throw new IllegalArgumentException("Cannot scope permission to school - no school provided");
        }
        addScopedPermission(permissionModel, schoolId, PermissionModel.PermissionType.SCHOOL);
    }

    public void addVaultScopedPermission(PermissionModel permissionModel, String vaultId) {
        if (vaultId == null) {
            logger.error("Attempted to scope permission with ID={} to vault, but no vault was provided",
                    permissionModel.getId());
            throw new IllegalArgumentException("Cannot scope permission to vault - no vault provided");
        }
        addScopedPermission(permissionModel, vaultId, PermissionModel.PermissionType.VAULT);
    }

    private void addScopedPermission(PermissionModel permissionModel,
                                     String scopeId,
                                     PermissionModel.PermissionType expectedPermissionType) {

        validateDashboardPermission(permissionModel);
        if (expectedPermissionType != permissionModel.getType()) {
            logger.error("Attempted to list a non-{} permission with ID={} as scoped to {} with ID={}",
                    expectedPermissionType.name().toLowerCase(),
                    permissionModel.getId(),
                    expectedPermissionType.name().toLowerCase(),
                    scopeId);
            throw new IllegalArgumentException("Cannot scope non-school permission to school");
        }

        if (isGlobalScoped(permissionModel.getPermission())) {
            logger.debug("User already has globally scoped {} permission {} - ignoring {}-scoped duplicate",
                    expectedPermissionType.name().toLowerCase(),
                    permissionModel.getPermission().name(),
                    expectedPermissionType.name().toLowerCase());

        } else {
            scopedPermissions.put(permissionModel, scopeId);
        }
    }

    public boolean isGlobalScoped(Permission permission) {
        Set<String> scope = getScope(permission);
        return scope.contains(GLOBAL_SCOPE_IDENTIFIER);
    }
}
