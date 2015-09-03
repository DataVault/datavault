package org.datavaultplatform.common.model.dao;

import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import org.datavaultplatform.common.event.Event;

public class EventDAOImpl implements EventDAO {
    
    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public void save(Event event) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(event);
        tx.commit();
        session.close();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Event> list() {        
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Event.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<Event> events = criteria.list();
        session.close();
        return events;
    }
    
    @Override
    public Event findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Event.class);
        criteria.add(Restrictions.eq("id",Id));
        Event event = (Event)criteria.uniqueResult();
        session.close();
        return event;
    }
}
