package org.datavault.broker.services;

import org.datavault.common.event.Event;
import org.datavault.common.model.dao.EventDAO;

import java.util.List;

public class EventService {

    private EventDAO eventDAO;
    
    public List<Event> getEvents() {
        return eventDAO.list();
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
}

