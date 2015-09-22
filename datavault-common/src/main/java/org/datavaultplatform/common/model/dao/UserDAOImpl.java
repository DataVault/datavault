package org.datavaultplatform.common.model.dao;

import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import org.datavaultplatform.common.model.User;

public class UserDAOImpl implements UserDAO {

    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
     
    @Override
    public void save(User user) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(user);
        tx.commit();
        session.close();
    }
    
    @Override
    public void update(User user) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(user);
        tx.commit();
        session.close();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<User> list() {        
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(User.class);
        List<User> users = criteria.list();
        session.close();
        return users;
    }
    
    @Override
    public User findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("id",Id));
        User user = (User)criteria.uniqueResult();
        session.close();
        return user;
    }

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int)(long)(Long)session.createCriteria(User.class).setProjection(Projections.rowCount()).uniqueResult();
    }
}
