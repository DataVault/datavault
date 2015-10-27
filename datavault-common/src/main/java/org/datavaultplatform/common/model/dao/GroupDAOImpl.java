package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Vault;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class GroupDAOImpl implements GroupDAO {

    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
     
    @Override
    public void save(Group group) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(group);
        tx.commit();
        session.close();
    }
    
    @Override
    public void update(Group group) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(group);
        tx.commit();
        session.close();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<Group> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Group.class);
        List<Group> groups = criteria.list();
        session.close();
        return groups;
    }

    @Override
    public Group findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Group.class);
        criteria.add(Restrictions.eq("id", Id));
        Group group = (Group)criteria.uniqueResult();
        session.close();
        return group;
    }

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int)(long)(Long)session.createCriteria(Group.class).setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public int countVaultsById(String Id) {
        Session session = this.sessionFactory.openSession();
        return (int)(long)(Long)session.createCriteria(Vault.class).add(Restrictions.eq("groupID", Id)).setProjection(Projections.rowCount()).uniqueResult();
    }
}
