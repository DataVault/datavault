package org.datavaultplatform.common.event.retrieve;

import jakarta.persistence.Entity;

@Entity
public class RetrieveStart extends BaseRetrieveEvent {
    public static final String MESSAGE = "Deposit retrieve started";

    public RetrieveStart() {
    }

    public RetrieveStart(String jobId, String depositId, String retrieveId) {
        super(jobId, depositId, retrieveId, MESSAGE);
        this.eventClass = RetrieveStart.class.getCanonicalName();
    }
}
