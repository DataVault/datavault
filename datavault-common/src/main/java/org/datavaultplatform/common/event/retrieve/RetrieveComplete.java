package org.datavaultplatform.common.event.retrieve;

import org.datavaultplatform.common.event.Event;

import jakarta.persistence.Entity;

@Entity
public class RetrieveComplete extends Event {

    public RetrieveComplete() {
    }
    public RetrieveComplete(String jobId, String depositId, String retrieveId) {
        super(jobId, depositId, retrieveId, "Deposit retrieve completed");
        this.eventClass = RetrieveComplete.class.getCanonicalName();
    }
}
