package org.datavault.common.event.deposit;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavault.common.event.Event;

@Entity
@Table(name="Events")
public class Start extends Event {
    
    Start() {};
    public Start(String depositId) {
        super(depositId, "Deposit started");
        this.eventClass = Start.class.getCanonicalName();
    }
}
