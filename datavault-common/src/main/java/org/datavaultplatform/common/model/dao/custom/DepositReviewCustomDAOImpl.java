package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.DepositReview_;


public class DepositReviewCustomDAOImpl extends BaseCustomDAOImpl implements
    DepositReviewCustomDAO {

    public DepositReviewCustomDAOImpl( EntityManager em ) {
        super(em);
    }

    @Override
    public List<DepositReview> search(String query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DepositReview> cr = cb.createQuery(DepositReview.class).distinct(true);
        Root<DepositReview> rt = cr.from(DepositReview.class);
        if (query != null) {
            cr.where(cb.like(cb.lower(rt.get(DepositReview_.ID)), "%" + query.toLowerCase() + "%"));
        }
        List<DepositReview> depositReviews = em.createQuery(cr).getResultList();
        return depositReviews;
    }
}
