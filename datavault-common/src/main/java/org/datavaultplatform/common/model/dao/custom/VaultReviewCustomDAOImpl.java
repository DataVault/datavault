package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.VaultReview_;


public class VaultReviewCustomDAOImpl extends BaseCustomDAOImpl implements
    VaultReviewCustomDAO {

    public VaultReviewCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<VaultReview> search(String query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<VaultReview> cq = cb.createQuery(VaultReview.class).distinct(true);
        Root<VaultReview> rt = cq.from(VaultReview.class);
        if(query != null) {
            cq.where(cb.like(cb.lower(rt.get(VaultReview_.ID)), getQueryLower(query)));
        }
        List<VaultReview> vaultReviews = getResults(cq);
        return vaultReviews;
    }
}
