package org.datavault.common.model.dao;

import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import org.datavault.common.model.Deposit;

public class DepositDAOImpl implements DepositDAO {

    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
     
    @Override
    public void save(Deposit deposit) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(deposit);
        tx.commit();
        session.close();
    }
    
    @Override
    public void update(Deposit deposit) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(deposit);
        tx.commit();
        session.close();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<Deposit> list() {        
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        List<Deposit> deposits = criteria.list();
        session.close();
        return deposits;
    }
    
    @Override
    public Deposit findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        criteria.add(Restrictions.eq("id",Id));
        Deposit deposit = (Deposit)criteria.uniqueResult();
        session.close();
        return deposit;
    }
}
