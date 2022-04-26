package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.PendingDataCreator;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PendingDataCreatorDAOImpl implements PendingDataCreatorDAO{

    private static final Logger LOGGER = LoggerFactory.getLogger(PendingDataCreatorDAOImpl.class);

    private final SessionFactory sessionFactory;

    public PendingDataCreatorDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public void save(List<PendingDataCreator> pendingDataCreators) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        for (PendingDataCreator pdc : pendingDataCreators) {
            session.persist(pdc);
        }
        tx.commit();
        session.close();
    }

    @Override
    public PendingDataCreator findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(PendingDataCreator.class);
        criteria.add(Restrictions.eq("id", Id));
        PendingDataCreator creator = (PendingDataCreator) criteria.uniqueResult();
        session.close();
        return creator;
    }

    @Override
    public void update(PendingDataCreator pendingDataCreator) {
        Session session = null;
        Transaction tx = null;
        try {
            session = this.sessionFactory.openSession();
            tx = session.beginTransaction();
            session.update(pendingDataCreator);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
                System.out.println("PendingDataCreator.update - ROLLBACK");
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void delete(String id) {
        PendingDataCreator creator = findById(id);
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Transaction tx = session.beginTransaction();
            session.delete(creator);
            tx.commit();
        } finally {
            if (session != null) session.close();
        }
    }
}
