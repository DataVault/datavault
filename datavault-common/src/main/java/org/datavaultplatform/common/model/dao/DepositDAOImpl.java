package org.datavaultplatform.common.model.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import org.datavaultplatform.common.model.Deposit;

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
        Session session = null;
        Transaction tx = null;
        try {
            session = this.sessionFactory.openSession();
            tx = session.beginTransaction();
            session.update(deposit);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
                System.out.println("Deposit.update - ROLLBACK");
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<Deposit> list(String sort) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        // See if there is a valid sort option
        if ("id".equals(sort)) {
            criteria.addOrder(Order.asc("id"));
        } else if ("note".equals(sort)) {
            criteria.addOrder(Order.asc("note"));
        } else if ("status".equals(sort)) {
            criteria.addOrder(Order.asc("status"));
        } else if ("filePath".equals(sort)) {
            criteria.addOrder(Order.asc("filePath"));
        } else if ("depositSize".equals(sort)) {
            criteria.addOrder(Order.asc("depositSize"));
        } else {
            criteria.addOrder(Order.asc("creationTime"));
        }

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

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int)(long)(Long)session.createCriteria(Deposit.class).setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public int queueCount() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        criteria.add(Restrictions.eq("status", Deposit.Status.NOT_STARTED));
        criteria.setProjection(Projections.rowCount());
        return (int)(long)(Long)criteria.uniqueResult();
    }

    @Override
    public int inProgressCount() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        criteria.add(Restrictions.and(Restrictions.ne("status", Deposit.Status.NOT_STARTED), Restrictions.ne("status", Deposit.Status.COMPLETE)));
        criteria.setProjection(Projections.rowCount());
        return (int)(long)(Long)criteria.uniqueResult();
    }

    @Override
    public List<Deposit> inProgress() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        criteria.add(Restrictions.and(Restrictions.ne("status", Deposit.Status.NOT_STARTED), Restrictions.ne("status", Deposit.Status.COMPLETE)));
        List<Deposit> deposits = criteria.list();
        session.close();
        return deposits;
    }

    @Override
    public List<Deposit> completed() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        criteria.add(Restrictions.eq("status", Deposit.Status.COMPLETE));
        List<Deposit> deposits = criteria.list();
        session.close();
        return deposits;
    }

    @Override
    public List<Deposit> search(String query, String sort) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%"), Restrictions.ilike("note", "%" + query + "%"), Restrictions.ilike("filePath", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        // See if there is a valid sort option
        if ("id".equals(sort)) {
            criteria.addOrder(Order.asc("id"));
        } else if ("note".equals(sort)) {
            criteria.addOrder(Order.asc("note"));
        } else if ("status".equals(sort)) {
            criteria.addOrder(Order.asc("status"));
        } else if ("filePath".equals(sort)) {
            criteria.addOrder(Order.asc("filePath"));
        } else if ("depositSize".equals(sort)) {
            criteria.addOrder(Order.asc("depositSize"));
        } else {
            criteria.addOrder(Order.asc("creationTime"));
        }

        List<Deposit> deposits = criteria.list();
        session.close();
        return deposits;
    }

    @Override
    public Long size() {
        Session session = this.sessionFactory.openSession();
        return (Long)session.createCriteria(Deposit.class).setProjection(Projections.sum("depositSize")).uniqueResult();
    }

    @Override
    public List<Deposit> getDepositsWaitingForAudit(Date olderThanDate) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Deposit.class);

        criteria.add(Restrictions.le("creationTime", olderThanDate));
        criteria.add(Restrictions.le("status", Deposit.Status.COMPLETE));

        criteria.addOrder(Order.asc("creationTime"));

        List<Deposit> deposits = criteria.list();
        session.close();
        return deposits;
    }
}
