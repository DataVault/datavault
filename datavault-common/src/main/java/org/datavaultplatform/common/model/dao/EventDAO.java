package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.event.Event;

public interface EventDAO {
    
    public void save(Event event);
    
    public List<Event> list();
    
    public List<Event> list(String sort);

    public Event findById(String Id);
    
    public int count();
    
}
