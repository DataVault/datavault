package org.datavault.common.model.dao;

import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import org.datavault.common.model.Policy;

public class PolicyDAOImpl implements PolicyDAO {

    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
     
    @Override
    public void save(Policy policy) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(policy);
        tx.commit();
        session.close();
    }
    
    @Override
    public void update(Policy policy) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(policy);
        tx.commit();
        session.close();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<Policy> list() {        
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Policy.class);
        List<Policy> policies = criteria.list();
        session.close();
        return policies;
    }
    
    @Override
    public Policy findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Policy.class);
        criteria.add(Restrictions.eq("id",Id));
        Policy policy = (Policy)criteria.uniqueResult();
        session.close();
        return policy;
    }
}
