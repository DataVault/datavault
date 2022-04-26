package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.DepositReview;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class DepositReviewDAOImpl implements DepositReviewDAO {

    private final SessionFactory sessionFactory;

    public DepositReviewDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(DepositReview depositReview) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(depositReview);
        tx.commit();
        session.close();
    }
    
    @Override
    public void update(DepositReview depositReview) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(depositReview);
        tx.commit();
        session.close();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<DepositReview> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(DepositReview.class);
        List<DepositReview> depositReviews = criteria.list();
        session.close();
        return depositReviews;
    }

    @Override
    public DepositReview findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(DepositReview.class);
        criteria.add(Restrictions.eq("id",Id));
        DepositReview depositReview = (DepositReview)criteria.uniqueResult();
        session.close();
        return depositReview;
    }

    @Override
    public List<DepositReview> search(String query) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(DepositReview.class);
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<DepositReview> depositReviews = criteria.list();
        session.close();
        return depositReviews;
    }

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int)(long)(Long)session.createCriteria(DepositReview.class).setProjection(Projections.rowCount()).uniqueResult();
    }
}
