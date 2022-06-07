package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import javax.persistence.EntityManager;
import org.datavaultplatform.common.model.VaultReview;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


public class VaultReviewCustomDAOImpl extends BaseCustomDAOImpl implements
    VaultReviewCustomDAO {

    public VaultReviewCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<VaultReview> search(String query) {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(VaultReview.class);
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<VaultReview> vaultReviews = criteria.list();
        return vaultReviews;
    }
}
