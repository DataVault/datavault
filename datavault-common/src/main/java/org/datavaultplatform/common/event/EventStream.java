package org.datavaultplatform.common.event;

public interface EventStream {
    
    void send(Event event);
    
}
