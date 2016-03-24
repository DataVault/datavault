package org.datavaultplatform.common.event.retrieve;

import org.datavaultplatform.common.event.Event;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="Events")
public class RetrieveComplete extends Event {

    RetrieveComplete() {};
    public RetrieveComplete(String jobId, String depositId, String retrieveId) {
        super(jobId, depositId, retrieveId, "Deposit retrieve completed");
        this.eventClass = RetrieveComplete.class.getCanonicalName();
    }
}
