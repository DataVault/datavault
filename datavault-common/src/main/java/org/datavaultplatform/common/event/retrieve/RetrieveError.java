package org.datavaultplatform.common.event.retrieve;

import org.datavaultplatform.common.event.Event;

import jakarta.persistence.Entity;

@Entity
public class RetrieveError extends Event {
    public RetrieveError() {
    }
    public RetrieveError(String jobId, String depositId, String retrieveId, String msg) {
        super(jobId, depositId, retrieveId, msg);
        this.eventClass = RetrieveError.class.getCanonicalName();
    }
}
