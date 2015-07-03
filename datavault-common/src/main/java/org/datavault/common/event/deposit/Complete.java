package org.datavault.common.event.deposit;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavault.common.event.Event;

@Entity
@Table(name="Events")
public class Complete extends Event {
    
    Complete() {};
    public Complete(String depositId) {
        super(depositId, "Deposit completed");
        this.eventClass = Complete.class.getCanonicalName();
    }
}
