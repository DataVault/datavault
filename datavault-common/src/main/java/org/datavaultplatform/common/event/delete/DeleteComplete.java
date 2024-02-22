package org.datavaultplatform.common.event.delete;

import jakarta.persistence.Entity;

import org.datavaultplatform.common.event.Event;

@Entity
public class DeleteComplete extends Event {

    public DeleteComplete() {
    }
    public DeleteComplete(String jobId, String depositId) {
    	super("Deposit Delete finished");
        this.eventClass = DeleteComplete.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
