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

    public RoleAssignmentDaoImpl() {}

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public boolean roleAssignmentExists(RoleAssignment roleAssignment) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RoleAssignment.class);
        criteria.add(Restrictions.eq("role", roleAssignment.getRole()));
        criteria.add(Restrictions.eq("user", roleAssignment.getUser()));
        if (roleAssignment.getSchool() != null) {
            criteria.add(Restrictions.eq("school", roleAssignment.getSchool()));
        }
        if (roleAssignment.getVault() != null) {
            criteria.add(Restrictions.eq("vault", roleAssignment.getVault()));
        }
        boolean exists = criteria.uniqueResult() != null;
        session.close();
        return exists;
    }

    @Override
    public void store(RoleAssignment roleAssignment) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(roleAssignment);
        tx.commit();
        session.close();
    }

    @Override
    public RoleAssignment find(Long id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RoleAssignment.class);
        criteria.add(Restrictions.eq("id", id));
        RoleAssignment roleAssignment = (RoleAssignment) criteria.uniqueResult();
        session.close();
        return roleAssignment;
    }

    @Override
    public List<RoleAssignment> findAll() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RoleAssignment.class);
        List<RoleAssignment> roleAssignments = criteria.list();
        session.close();
        return roleAssignments;
    }

    @Override
    public List<RoleAssignment> findBySchoolId(String schoolId) {
        Session session = sessionFactory.openSession();
        Group school = findObjectById(session, Group.class, "id", schoolId);
        List<RoleAssignment> schoolAssignments = findBy(session, "school", school);
        session.close();
        return schoolAssignments;
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
        Session session = sessionFactory.openSession();
        Vault vault = findObjectById(session, Vault.class, "id", vaultId);
        List<RoleAssignment> schoolAssignments = findBy(session, "vault", vault);
        session.close();
        return schoolAssignments;
    }

    @Override
    public List<RoleAssignment> findByUserId(String userId) {
        Session session = sessionFactory.openSession();
        User user = findObjectById(session, User.class, "id", userId);
        List<RoleAssignment> schoolAssignments = findBy(session, "user", user);
        session.close();
        return schoolAssignments;
    }

    @Override
    public List<RoleAssignment> findByRoleId(Long roleId) {
        Session session = sessionFactory.openSession();
        RoleModel role = findObjectById(session, RoleModel.class, "id", roleId);
        List<RoleAssignment> assignments = findBy(session, "role", role);
        session.close();
        return assignments;
    }

    @Override
    public void update(RoleAssignment roleAssignment) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(roleAssignment);
        tx.commit();
        session.close();
    }

    @Override
    public void delete(Long id) {
        RoleAssignment roleAssignment = find(id);
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.delete(roleAssignment);
        tx.commit();
        session.close();
    }
}
