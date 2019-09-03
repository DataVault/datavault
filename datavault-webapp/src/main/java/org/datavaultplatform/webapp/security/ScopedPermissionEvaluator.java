package org.datavaultplatform.webapp.security;

import com.google.common.collect.Multimap;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collection;

/**
 * A permission expression evaluator that is able to check existence of {@link Vault} and {@link Group} permissions
 * on a given authentication.
 *
 * <p>
 * For example, it can be used in a pre-authorize filter:
 * <pre>
 *    {@literal @}PreAuthorize("hasPermission(#vault, 'VAULT_PERMISSION_1')")}
 *     public void deleteVault(Vault vault) {
 *         ...
 *     }
 * </pre>
 *
 * <p>
 * Or called imperatively using any {@link Permission} value.
 * <pre>
 *     Vault vault = ...;
 *     if (permissionEvaluator.hasPermission(user, vault, Permission.VAULT_PERMISSION_1)) { ... }
 * </pre>
 */
public class ScopedPermissionEvaluator implements PermissionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(ScopedPermissionEvaluator.class);
    private final RestService rest;

    public ScopedPermissionEvaluator(RestService rest) {
        this.rest = rest;
    }

    /**
     * Real implementation for actual checking of permissions on an authentication.
     *
     * @param auth         The authentication to extract permissions from.
     * @param key          The unique key representing the scope of the requested permission.
     * @param permissionId A string or enum value representing the permission.
     * @return {@code true} iff authorized, {@code false} otherwise.
     */
    boolean hasPermission(Authentication auth, ScopedPermissionKey key, Object permissionId) {
        if (auth == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        if (authorities.contains(new SimpleGrantedAuthority("ROLE_IS_ADMIN"))) {
            return true;
        }

        Permission permission = null;

        if (permissionId instanceof String) {
            try {
                permission = Permission.valueOf(((String) permissionId).toUpperCase());
            } catch (IllegalArgumentException ex) {
                logger.warn("Attempted to check a non-existent permission ({}, {})", key, permissionId);
            }
        } else if (permissionId instanceof Permission) {
            permission = (Permission) permissionId;
        } else {
            throw new IllegalArgumentException("Expected java.lang.String or Permission value, found: " + permissionId);
        }

        Multimap<ScopedPermissionKey, Permission> permissions = ScopedPermissionMapBuilder.createPermissionMap(auth);

        return permissions.containsEntry(key, permission);
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permissionId) {
        final ScopedPermissionKey key;

        if (targetDomainObject instanceof Vault) {
            Vault vault = (Vault) targetDomainObject;

            // TODO: Stop gap, need to imply permissions for data owners either via Vaults.user, or a RoleAssignment
            // relation.
            if (vault.getUser() != null && auth.getName().equals(vault.getUser().getID())) {
                return true;
            }

            key = new ScopedPermissionKey(((Vault) targetDomainObject).getID(), Vault.class);
        } else if (targetDomainObject instanceof Group) {
            key = new ScopedPermissionKey(((Group) targetDomainObject).getID(), Group.class);
        } else {
            return false;
        }

        return hasPermission(auth, key, permissionId);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        final String id = targetId.toString();

        if (GROUP_VAULT.equalsIgnoreCase(targetType)) {
            VaultInfo vaultInfo = rest.getVault(id);
            return hasPermission(authentication, rest.getGroup(vaultInfo.getGroupID()), permission);
        } else if (VAULT_TYPE_SHORT.equalsIgnoreCase(targetType) || VAULT_TYPE.equalsIgnoreCase(targetType)) {
            return hasPermission(authentication, rest.getVaultRecord(id), permission);
        } else if (GROUP_TYPE_SHORT.equalsIgnoreCase(targetType) || GROUP_TYPE.equalsIgnoreCase(targetType)) {
            return hasPermission(authentication, rest.getGroup(id), permission);
        } else {
            return false;
        }
    }

    private static final String VAULT_TYPE_SHORT = Vault.class.getSimpleName();
    private static final String VAULT_TYPE = Vault.class.getName();

    private static final String GROUP_TYPE_SHORT = Group.class.getSimpleName();
    private static final String GROUP_TYPE = Group.class.getName();

    private static final String GROUP_VAULT = "GROUP_VAULT";
}
