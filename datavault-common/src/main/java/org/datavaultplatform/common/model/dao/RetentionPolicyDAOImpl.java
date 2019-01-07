package org.datavaultplatform.common.model.dao;

import java.util.List;

import org.datavaultplatform.common.model.RetentionPolicy;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class RetentionPolicyDAOImpl implements RetentionPolicyDAO {

    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
     
    @Override
    public void save(RetentionPolicy retentionPolicy) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(retentionPolicy);
        tx.commit();
        session.close();
    }
    
    @Override
    public void update(RetentionPolicy retentionPolicy) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(retentionPolicy);
        tx.commit();
        session.close();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<RetentionPolicy> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RetentionPolicy.class);
        criteria.addOrder(Order.asc("name"));
        List<RetentionPolicy> policies = criteria.list();
        session.close();
        return policies;
    }
    
    @Override
    public RetentionPolicy findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(RetentionPolicy.class);
        criteria.add(Restrictions.eq("id",Integer.parseInt(Id)));
        RetentionPolicy retentionPolicy = (RetentionPolicy)criteria.uniqueResult();
        session.close();
        return retentionPolicy;
    }
}
