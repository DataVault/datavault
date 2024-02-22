package org.datavaultplatform.common.event.delete;

import jakarta.persistence.Entity;

import org.datavaultplatform.common.event.Event;

@Entity
public class DeleteStart extends Event {

    public DeleteStart() {
    }
    public DeleteStart(String jobId, String depositId) {
        super("Deposit delete started");
        this.eventClass = DeleteStart.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
