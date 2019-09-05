package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class RoleAssignmentDaoImpl implements RoleAssignmentDAO {

    private SessionFactory sessionFactory;

    public RoleAssignmentDaoImpl() {
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
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
            if (roleAssignment.getVault() != null) {
                criteria.add(Restrictions.eq("vault", roleAssignment.getVault()));
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

            Vault vault = findObjectById(session, Vault.class, "id", vaultId);
            List<RoleAssignment> schoolAssignments = findBy(session, "vault", vault);
            return schoolAssignments;
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
