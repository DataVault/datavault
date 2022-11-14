package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import java.util.Set;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.RoleAssignment;
import org.springframework.data.jpa.repository.EntityGraph;

public interface RoleAssignmentCustomDAO extends BaseCustomDAO {

    boolean roleAssignmentExists(RoleAssignment roleAssignment);

    Set<Permission> findUserPermissions(String userId);

    @EntityGraph(RoleAssignment.EG_ROLE_ASSIGNMENT)
    List<RoleAssignment> findBySchoolId(String schoolId);

    @EntityGraph(RoleAssignment.EG_ROLE_ASSIGNMENT)
    List<RoleAssignment> findByVaultId(String vaultId);

    @EntityGraph(RoleAssignment.EG_ROLE_ASSIGNMENT)
    List<RoleAssignment> findByPendingVaultId(String vaultId);

    @EntityGraph(RoleAssignment.EG_ROLE_ASSIGNMENT)
    List<RoleAssignment> findByUserId(String userId);

    @EntityGraph(RoleAssignment.EG_ROLE_ASSIGNMENT)
    List<RoleAssignment> findByRoleId(Long roleId);

    boolean hasPermission(String userId, Permission permission);

    boolean isAdminUser(String userId);
}
