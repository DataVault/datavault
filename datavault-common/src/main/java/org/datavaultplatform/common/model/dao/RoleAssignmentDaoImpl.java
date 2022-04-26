package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.util.RoleUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RoleAssignmentDaoImpl implements RoleAssignmentDAO {

    private final SessionFactory sessionFactory;

    @Autowired
    public RoleAssignmentDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public boolean roleAssignmentExists(RoleAssignment roleAssignment) {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Criteria criteria = session.createCriteria(RoleAssignment.class);
            criteria.add(Restrictions.eq("role", roleAssignment.getRole()));
            criteria.add(Restrictions.eq("userId", roleAssignment.getUserId()));
            if (roleAssignment.getSchoolId() != null) {

                criteria.add(Restrictions.eq("schoolId", roleAssignment.getSchoolId()));
            }
            if (roleAssignment.getVaultId() != null) {
                criteria.add(Restrictions.eq("vaultId", roleAssignment.getVaultId()));
            }
            if (roleAssignment.getPendingVaultId() != null) {
                criteria.add(Restrictions.eq("pendingVaultId", roleAssignment.getPendingVaultId()));
            }
            return criteria.uniqueResult() != null;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public void store(RoleAssignment roleAssignment) {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Transaction tx = session.beginTransaction();
            session.persist(roleAssignment);
            tx.commit();
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public RoleAssignment find(Long id) {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Criteria criteria = session.createCriteria(RoleAssignment.class);
            criteria.add(Restrictions.eq("id", id));
            RoleAssignment roleAssignment = (RoleAssignment) criteria.uniqueResult();
            return roleAssignment;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public Set<Permission> findUserPermissions(String userId) {
        Session session = null;

        try {
            session = sessionFactory.openSession();
            Query query = session.createQuery("SELECT DISTINCT role.permissions \n" +
                    "FROM org.datavaultplatform.common.model.RoleAssignment ra\n" +
                    "INNER JOIN ra.role as role\n" +
                    "WHERE ra.userId = :userId");
            query.setParameter("userId", userId);

            return ((List<PermissionModel>) query.list())
                    .stream()
                    .map(PermissionModel::getPermission)
                    .collect(Collectors.toSet());
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public List<RoleAssignment> findAll() {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Criteria criteria = session.createCriteria(RoleAssignment.class);
            List<RoleAssignment> roleAssignments = criteria.list();
            return roleAssignments;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public List<RoleAssignment> findBySchoolId(String schoolId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            List<RoleAssignment> schoolAssignments = findBy(session, "schoolId", schoolId);
            return schoolAssignments;
        } finally {
            if (session != null) session.close();
        }

    }

    private <T> T findObjectById(Session session, Class<T> type, String idName, Object idValue) {
        Criteria criteria = session.createCriteria(type);
        criteria.add(Restrictions.eq(idName, idValue));
        return (T) criteria.uniqueResult();
    }

    private List<RoleAssignment> findBy(Session session, String columnName, Object columnValue) {
        Criteria criteria = session.createCriteria(RoleAssignment.class);
        criteria.add(Restrictions.eq(columnName, columnValue));
        return (List<RoleAssignment>) criteria.list();
    }

    @Override
    public List<RoleAssignment> findByVaultId(String vaultId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            List<RoleAssignment> vaultAssignments = findBy(session, "vaultId", vaultId);
            return vaultAssignments;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public List<RoleAssignment> findByPendingVaultId(String vaultId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            List<RoleAssignment> vaultAssignments = findBy(session, "pendingVaultId", vaultId);
            return vaultAssignments;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public List<RoleAssignment> findByUserId(String userId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            List<RoleAssignment> schoolAssignments = findBy(session, "userId", userId);
            return schoolAssignments;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public List<RoleAssignment> findByRoleId(Long roleId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            RoleModel role = findObjectById(session, RoleModel.class, "id", roleId);
            List<RoleAssignment> assignments = findBy(session, "role", role);
            return assignments;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public boolean hasPermission(String userId, Permission permission) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Criteria criteria = session.createCriteria(RoleAssignment.class, "assignment");
            criteria.createAlias("assignment.role", "role");
            criteria.createAlias("role.permissions", "permission");
            criteria.add(Restrictions.eq("assignment.userId", userId));
            criteria.add(Restrictions.eq("permission.id", permission.getId()));
            return criteria.list().size() > 0;

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public boolean isAdminUser(String userId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Criteria criteria = session.createCriteria(RoleAssignment.class, "assignment");
            criteria.createAlias("assignment.role", "role");
            criteria.add(Restrictions.eq("assignment.userId", userId));
            criteria.add(Restrictions.eq("role.name", RoleUtils.IS_ADMIN_ROLE_NAME));
            return criteria.list().size() > 0;

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void update(RoleAssignment roleAssignment) {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Transaction tx = session.beginTransaction();
            session.update(roleAssignment);
            tx.commit();
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public void delete(Long id) {
        RoleAssignment roleAssignment = find(id);
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Transaction tx = session.beginTransaction();
            session.delete(roleAssignment);
            tx.commit();
        } finally {
            if (session != null) session.close();
        }
    }
}
