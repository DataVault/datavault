package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.DepositChunk;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class DepositChunkDAOImpl implements DepositChunkDAO {

    private final SessionFactory sessionFactory;

    public DepositChunkDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public void save(DepositChunk chunk) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(chunk);
        tx.commit();
        session.close();
    }
    
    @Override
    public void update(DepositChunk chunk) {
        Session session = null;
        Transaction tx = null;
        try {
            session = this.sessionFactory.openSession();
            tx = session.beginTransaction();
            session.update(chunk);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
                System.out.println("DepositChunk update - ROLLBACK");
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
    public List<DepositChunk> list(String sort) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(DepositChunk.class);
        // See if there is a valid sort option
        if ("id".equals(sort)) {
            criteria.addOrder(Order.asc("id"));
        } else if ("status".equals(sort)) {
            criteria.addOrder(Order.asc("status"));
        } else {
            criteria.addOrder(Order.asc("creationTime"));
        }

        List<DepositChunk> chunks = criteria.list();
        session.close();
        return chunks;
    }
    
    @Override
    public DepositChunk findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(DepositChunk.class);
        criteria.add(Restrictions.eq("id",Id));
        DepositChunk chunk = (DepositChunk)criteria.uniqueResult();
        session.close();
        return chunk;
    }
}
