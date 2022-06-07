package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import javax.persistence.EntityManager;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.Vault;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;


public class EventCustomDAOImpl extends BaseCustomDAOImpl implements EventCustomDAO {

    public EventCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Event> list(String sort) {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(Event.class);
        // See if there is a valid sort option
        if ("id".equals(sort)) {
            criteria.addOrder(Order.asc("id"));
        } else {
            criteria.addOrder(Order.asc("timestamp"));
        }

        List<Event> events = criteria.list();
        return events;
    }

    @Override
    public List<Event> findVaultEvents(Vault vault) {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(Event.class);
        criteria.add(Restrictions.eq("vault",vault));
        criteria.addOrder(Order.asc("timestamp"));
        List<Event> events = criteria.list();
        return events;
    }
}
