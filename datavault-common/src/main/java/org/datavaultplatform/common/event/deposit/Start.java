package org.datavaultplatform.common.event.deposit;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class Start extends Event {
    
    public Start() {
    }
    public Start(String jobId, String depositId) {
        super("Deposit started");
        this.eventClass = Start.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
