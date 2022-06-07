package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import javax.persistence.EntityManager;
import org.datavaultplatform.common.model.DepositReview;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


public class DepositReviewCustomDAOImpl extends BaseCustomDAOImpl implements
    DepositReviewCustomDAO {

    public DepositReviewCustomDAOImpl( EntityManager em ) {
        super(em);
    }

    @Override
    public List<DepositReview> search(String query) {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(DepositReview.class);
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<DepositReview> depositReviews = criteria.list();
        return depositReviews;
    }
}
