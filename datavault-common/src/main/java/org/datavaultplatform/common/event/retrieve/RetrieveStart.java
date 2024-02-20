package org.datavaultplatform.common.event.retrieve;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class RetrieveStart extends Event {

    public RetrieveStart() {
    }
    public RetrieveStart(String jobId, String depositId, String retrieveId) {
        super(jobId, depositId, retrieveId, "Deposit retrieve started");
        this.eventClass = RetrieveStart.class.getCanonicalName();
    }
}
