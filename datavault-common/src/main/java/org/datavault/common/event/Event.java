package org.datavault.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    
    // A generic event
    
    public String eventClass;
    public String depositId;
    public String message;
    
    // By default events aren't persisted
    public Boolean persistent = false;
    
    public Event() {};
    public Event(String depositId, String message) {
        this.eventClass = Event.class.getCanonicalName();
        this.depositId = depositId;
        this.message = message;
    }

    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDepositId() {
        return depositId;
    }

    public void setDepositId(String depositId) {
        this.depositId = depositId;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    public void setPersistent(Boolean persistent) {
        this.persistent = persistent;
    }
}
