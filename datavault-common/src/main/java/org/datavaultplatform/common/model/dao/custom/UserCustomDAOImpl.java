package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.User_;


public class UserCustomDAOImpl extends BaseCustomDAOImpl implements UserCustomDAO {

    public UserCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<User> search(String query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class).distinct(true);
        Root<User> rt = cq.from(User.class);

        if(query != null) {
            String queryLower = getQueryLower(query);
            cq.where(cb.or(
                cb.like(cb.lower(rt.get(User_.id)), queryLower),
                cb.like(cb.lower(rt.get(User_.firstname)), queryLower),
                cb.like(cb.lower(rt.get(User_.lastname)), queryLower)
            ));
        }

        List<User> users = getResults(cq);
        return users;
    }

    @Override
    public List<User> findUsersWithInvalidEmail() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class).distinct(true);
        Root<User> rt = cq.from(User.class);

        // 1. Create the 'IS NULL' predicate
        Predicate emailIsNull = cb.isNull(rt.get("email"));

        // 2. Create the 'NOT LIKE' predicate
        Predicate emailNotLike = cb.notLike(rt.get("email"), "%@%");

        // 3. Combine them with an OR clause and apply to the query
        cq.where(cb.or(emailIsNull, emailNotLike));

        List<User> users = getResults(cq);
        return users;
    }
}
