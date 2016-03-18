package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.dao.EventDAO;

import java.util.List;

public class EventService {

    private EventDAO eventDAO;
    
    public List<Event> getEvents() {
        return eventDAO.list();
    }
    
    public List<Event> getEvents(String sort) {
        return eventDAO.list(sort);
    }
    
    public void addEvent(Event event) {
        eventDAO.save(event);
    }
    
    public Event getEvent(String eventID) {
        return eventDAO.findById(eventID);
    }
    
    public void setEventDAO(EventDAO eventDAO) {
        this.eventDAO = eventDAO;
    }
    
    public int count() { return eventDAO.count(); }
}

