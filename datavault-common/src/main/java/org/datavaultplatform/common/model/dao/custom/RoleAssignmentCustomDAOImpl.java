package org.datavaultplatform.common.model.dao.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleAssignment_;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleModel_;
import org.datavaultplatform.common.util.RoleUtils;

public class RoleAssignmentCustomDAOImpl extends BaseCustomDAOImpl implements
    RoleAssignmentCustomDAO {

  public RoleAssignmentCustomDAOImpl(EntityManager em) {
    super(em);
  }

    @Override
    public boolean roleAssignmentExists(RoleAssignment roleAssignment) {
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<RoleAssignment> cr = cb.createQuery(RoleAssignment.class).distinct(true);
      Root<RoleAssignment> rt = cr.from(RoleAssignment.class);
      List<Predicate> clauses = new ArrayList<>();
      clauses.add(cb.equal(rt.get(RoleAssignment_.ROLE), roleAssignment.getRole()));
      clauses.add(cb.equal(rt.get(RoleAssignment_.USER_ID), roleAssignment.getUserId()));
      if (roleAssignment.getSchoolId() != null) {

        clauses.add(cb.equal(rt.get(RoleAssignment_.SCHOOL_ID), roleAssignment.getSchoolId()));
      }
      if (roleAssignment.getVaultId() != null) {
        clauses.add(cb.equal(rt.get(RoleAssignment_.VAULT_ID), roleAssignment.getVaultId()));
      }
      if (roleAssignment.getPendingVaultId() != null) {
        clauses.add(cb.equal(rt.get(RoleAssignment_.PENDING_VAULT_ID), roleAssignment.getPendingVaultId()));
      }
      Predicate[] clauseArr = clauses.toArray(new Predicate[]{});
      return getSingleResult(cr.where(clauseArr)) != null;
    }

    @Override
    public Set<Permission> findUserPermissions(String userId) {

      Query query = em.createQuery("SELECT DISTINCT role.permissions \n" +
        "FROM org.datavaultplatform.common.model.RoleAssignment ra\n" +
        "INNER JOIN ra.role as role\n" +
        "WHERE ra.userId = :userId");
      query.setParameter("userId", userId);

      return ((List<PermissionModel>) query.getResultList())
        .stream()
        .map(PermissionModel::getPermission)
        .collect(Collectors.toSet());
    }

    @Override
    public List<RoleAssignment> findBySchoolId(String schoolId) {
      return findBy(RoleAssignment_.SCHOOL_ID, schoolId);
    }

    private <T> T findObjectById(Class<T> type, String idName, Object idValue) {
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<T> cr = cb.createQuery(type).distinct(true);
      Root<T> rt = cr.from(type);
      if (idValue == null) {
        cr.where(cb.isNull(rt.get(idName)));
      } else {
        cr.where(cb.equal(rt.get(idName), idValue));
      }
      return getSingleResult(cr);
    }

    private List<RoleAssignment> findBy(String columnName, Object columnValue) {
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<RoleAssignment> cr = cb.createQuery(RoleAssignment.class).distinct(true);
      Root<RoleAssignment> rt = cr.from(RoleAssignment.class);
      if (columnValue == null) {
        cr.where(cb.isNull(rt.get(columnName)));
      } else {
        cr.where(cb.equal(rt.get(columnName), columnValue));
      }
      return getResults(cr);
    }

    @Override
    public List<RoleAssignment> findByVaultId(String vaultId) {
      return findBy(RoleAssignment_.VAULT_ID, vaultId);
    }

    @Override
    public List<RoleAssignment> findByPendingVaultId(String vaultId) {
        return findBy(RoleAssignment_.PENDING_VAULT_ID, vaultId);
    }

    @Override
    public List<RoleAssignment> findByUserId(String userId) {
        return findBy(RoleAssignment_.USER_ID, userId);
    }

    @Override
    public List<RoleAssignment> findByRoleId(Long roleId) {
      RoleModel role = findObjectById(RoleModel.class, RoleModel_.ID, roleId);
      return findBy(RoleAssignment_.ROLE, role);
    }

    @Override
    public boolean hasPermission(String userId, Permission permission) {
      TypedQuery<Long> query = em.createQuery("SELECT count(*)  " +
              "FROM org.datavaultplatform.common.model.RoleAssignment assignment " +
              "INNER JOIN assignment.role as role " +
              "INNER JOIN role.permissions as permission " +
              "WHERE assignment.userId = :userId " +
              "AND   permission.id = :permissionId",
          Long.class);
      query.setParameter("userId", userId);
      query.setParameter("permissionId", permission.getId());
      return getCount(query) > 0;
    }

    @Override
    public boolean isAdminUser(String userId) {
      TypedQuery<Long> query = em.createQuery("SELECT count(*)  " +
          "FROM org.datavaultplatform.common.model.RoleAssignment assignment " +
          "INNER JOIN assignment.role as role " +
          "WHERE assignment.userId = :userId " +
          "AND   role.name = :roleName",
          Long.class);
      query.setParameter("userId", userId);
      query.setParameter("roleName", RoleUtils.IS_ADMIN_ROLE_NAME);
      return getCount(query) > 0;
  }
}
