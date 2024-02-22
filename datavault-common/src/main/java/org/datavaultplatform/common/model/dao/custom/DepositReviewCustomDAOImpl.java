package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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
        CriteriaQuery<DepositReview> cq = cb.createQuery(DepositReview.class).distinct(true);
        Root<DepositReview> rt = cq.from(DepositReview.class);
        if (query != null) {
            cq.where(cb.like(cb.lower(rt.get(DepositReview_.ID)), getQueryLower(query)));
        }
        List<DepositReview> depositReviews = getResults(cq);
        return depositReviews;
    }
}
