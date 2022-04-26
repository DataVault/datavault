package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Audit;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AuditDAOImpl implements AuditDAO {

    private final SessionFactory sessionFactory;

    public AuditDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Audit Audit) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(Audit);
        tx.commit();
        session.close();
    }

    @Override
    public void update(Audit Audit) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(Audit);
        tx.commit();
        session.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Audit> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Audit.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("timestamp"));
        List<Audit> Audits = criteria.list();
        session.close();
        return Audits;
    }

    @Override
    public Audit findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Audit.class);
        criteria.add(Restrictions.eq("id", Id));
        Audit Audit = (Audit) criteria.uniqueResult();
        session.close();
        return Audit;
    }

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int) (long) (Long) session.createCriteria(Audit.class).setProjection(Projections.rowCount()).uniqueResult();
    }
}