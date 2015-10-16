package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Restore;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class RestoreDAOImpl implements RestoreDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Restore restore) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(restore);
        tx.commit();
        session.close();
    }

    @Override
    public void update(Restore restore) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(restore);
        tx.commit();
        session.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Restore> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Restore.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("timestamp"));
        List<Restore> restores = criteria.list();
        session.close();
        return restores;
    }

    @Override
    public Restore findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Restore.class);
        criteria.add(Restrictions.eq("id", Id));
        Restore restore = (Restore) criteria.uniqueResult();
        session.close();
        return restore;
    }

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int) (long) (Long) session.createCriteria(Restore.class).setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public int queueCount() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Restore.class);
        criteria.add(Restrictions.eq("status", Restore.Status.NOT_STARTED));
        criteria.setProjection(Projections.rowCount());
        return (int)(long)(Long)criteria.uniqueResult();
    }
}