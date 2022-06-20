package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.Event_;
import org.datavaultplatform.common.model.Vault;


public class EventCustomDAOImpl extends BaseCustomDAOImpl implements EventCustomDAO {

    public EventCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<Event> list(String sort) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Event> cr = cb.createQuery(Event.class).distinct(true);
        Root<Event> rt = cr.from(Event.class);
        // See if there is a valid sort option
        if(Event_.ID.equals(sort)) {
            cr.orderBy(cb.asc(rt.get(Event_.ID)));
        } else {
            cr.orderBy(cb.asc(rt.get(Event_.TIMESTAMP)));
        }

        List<Event> events = em.createQuery(cr).getResultList();
        return events;
    }

    @Override
    public List<Event> findVaultEvents(Vault vault) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Event> cr = cb.createQuery(Event.class).distinct(true);
        Root<Event> rt = cr.from(Event.class);
        cr.where(cb.equal(rt.get(Event_.VAULT), vault));
        cr.orderBy(cb.asc(rt.get(Event_.TIMESTAMP)));
        List<Event> events = em.createQuery(cr).getResultList();
        return events;
    }
}
