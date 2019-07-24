package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.List;

public class RoleDAOImpl implements RoleDAO {

    private SessionFactory sessionFactory;

    public RoleDAOImpl() {}

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void store(RoleModel role) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(role);
        tx.commit();
        session.close();
    }

    @Override
    public RoleModel find(Long id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("id", id));
        RoleModel role = (RoleModel) criteria.uniqueResult();
        session.close();
        return role;
    }

    @Override
    public Collection<RoleModel> findAll() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        List<RoleModel> roles = criteria.list();
        session.close();
        return roles;
    }

    @Override
    public Collection<RoleModel> findAll(RoleType roleType) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RoleModel.class);
        criteria.add(Restrictions.eq("role_type", roleType.name()));
        List<RoleModel> roles = criteria.list();
        session.close();
        return roles;
    }

    @Override
    public void update(RoleModel role) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(role);
        tx.commit();
        session.close();
    }

    @Override
    public void delete(Long id) {
        RoleModel role = find(id);
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.delete(role);
        tx.commit();
        session.close();
    }
}
