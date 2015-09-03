package org.datavaultplatform.common.event;

public interface EventStream {
    
    public void send(Event event);
    
}
