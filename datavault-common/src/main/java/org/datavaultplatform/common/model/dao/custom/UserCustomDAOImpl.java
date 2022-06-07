package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import javax.persistence.EntityManager;
import org.datavaultplatform.common.model.User;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


public class UserCustomDAOImpl extends BaseCustomDAOImpl implements UserCustomDAO {

    public UserCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<User> search(String query) {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%"), Restrictions.ilike("firstname", "%" + query + "%"), Restrictions.ilike("lastname", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<User> users = criteria.list();
        return users;
    }
}
