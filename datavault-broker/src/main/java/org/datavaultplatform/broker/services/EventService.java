package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.dao.EventDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventService {

    private final EventDAO eventDAO;

    @Autowired
    public EventService(EventDAO eventDAO) {
        this.eventDAO = eventDAO;
    }

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
        return eventDAO.findById(eventID).orElse(null);
    }

    public long count() { return eventDAO.count(); }

    public List<Event> findVaultEvents(Vault vault) {
        return eventDAO.findVaultEvents(vault);
    }

    public Event findById(String id) {
        return eventDAO.findById(id).orElse(null);
    }
}

