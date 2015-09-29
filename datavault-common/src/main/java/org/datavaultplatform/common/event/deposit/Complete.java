package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavaultplatform.common.event.Event;

@Entity
@Table(name="Events")
public class Complete extends Event {
    
    Complete() {};
    public Complete(String jobId, String depositId) {
        super(jobId, depositId, "Deposit completed");
        this.eventClass = Complete.class.getCanonicalName();
    }
}
