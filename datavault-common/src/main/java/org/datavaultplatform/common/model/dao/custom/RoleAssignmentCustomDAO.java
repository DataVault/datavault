package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import java.util.Set;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.RoleAssignment;

public interface RoleAssignmentCustomDAO {

    boolean roleAssignmentExists(RoleAssignment roleAssignment);

    void store(RoleAssignment roleAssignment);

    RoleAssignment find(Long id);

    Set<Permission> findUserPermissions(String userId);

    List<RoleAssignment> findAll();

    List<RoleAssignment> findBySchoolId(String schoolId);

    List<RoleAssignment> findByVaultId(String vaultId);

    List<RoleAssignment> findByPendingVaultId(String vaultId);

    List<RoleAssignment> findByUserId(String userId);

    List<RoleAssignment> findByRoleId(Long roleId);

    boolean hasPermission(String userId, Permission permission);

    boolean isAdminUser(String userId);

    void update(RoleAssignment roleAssignment);

    void delete(Long id);
}
