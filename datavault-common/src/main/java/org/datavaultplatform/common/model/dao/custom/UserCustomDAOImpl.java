package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.User_;


public class UserCustomDAOImpl extends BaseCustomDAOImpl implements UserCustomDAO {

    public UserCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<User> search(String query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cr = cb.createQuery(User.class).distinct(true);
        Root<User> rt = cr.from(User.class);

        if(query != null) {
            String queryLower = query.toLowerCase();
            cr.where(cb.or(
                cb.like(cb.lower(rt.get(User_.id)), "%" + queryLower + "%"),
                cb.like(cb.lower(rt.get(User_.firstname)), "%" + queryLower + "%"),
                cb.like(cb.lower(rt.get(User_.lastname)), "%" + queryLower + "%")
            ));
        }

        List<User> users = em.createQuery(cr).getResultList();
        return users;
    }
}