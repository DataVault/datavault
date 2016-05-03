package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavaultplatform.common.event.Event;

@Entity
public class Start extends Event {
    
    Start() {};
    public Start(String jobId, String depositId) {
        super("Deposit started");
        this.eventClass = Start.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
