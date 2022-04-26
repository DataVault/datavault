package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.VaultReview;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class VaultReviewDAOImpl implements VaultReviewDAO {

    private final SessionFactory sessionFactory;

    public VaultReviewDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(VaultReview vaultReview) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(vaultReview);
        tx.commit();
        session.close();
    }
    
    @Override
    public void update(VaultReview vaultReview) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(vaultReview);
        tx.commit();
        session.close();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<VaultReview> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(VaultReview.class);
        List<VaultReview> vaultReviews = criteria.list();
        session.close();
        return vaultReviews;
    }

    @Override
    public VaultReview findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(VaultReview.class);
        criteria.add(Restrictions.eq("id",Id));
        VaultReview vaultReview = (VaultReview)criteria.uniqueResult();
        session.close();
        return vaultReview;
    }

    @Override
    public List<VaultReview> search(String query) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(VaultReview.class);
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<VaultReview> vaultReviews = criteria.list();
        session.close();
        return vaultReviews;
    }

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int)(long)(Long)session.createCriteria(VaultReview.class).setProjection(Projections.rowCount()).uniqueResult();
    }
}
