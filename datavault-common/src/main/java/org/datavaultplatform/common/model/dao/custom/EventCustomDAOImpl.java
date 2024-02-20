package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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
        CriteriaQuery<Event> cq = cb.createQuery(Event.class).distinct(true);
        Root<Event> rt = cq.from(Event.class);
        // See if there is a valid sort option
        if(Event_.ID.equals(sort)) {
            cq.orderBy(cb.asc(rt.get(Event_.ID)));
        } else {
            cq.orderBy(cb.asc(rt.get(Event_.TIMESTAMP)));
        }

        List<Event> events = getResults(cq);
        return events;
    }

    @Override
    public List<Event> findVaultEvents(Vault vault) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class).distinct(true);
        Root<Event> rt = cq.from(Event.class);
        cq.where(cb.equal(rt.get(Event_.VAULT), vault));
        cq.orderBy(cb.asc(rt.get(Event_.TIMESTAMP)));

        List<Event> events = getResults(cq);
        return events;
    }

}
