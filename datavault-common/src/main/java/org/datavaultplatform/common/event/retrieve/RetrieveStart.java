package org.datavaultplatform.common.event.retrieve;

import org.datavaultplatform.common.event.Event;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
public class RetrieveStart extends Event {

    RetrieveStart() {};
    public RetrieveStart(String jobId, String depositId, String retrieveId) {
        super(jobId, depositId, retrieveId, "Deposit retrieve started");
        this.eventClass = RetrieveStart.class.getCanonicalName();
    }
}
