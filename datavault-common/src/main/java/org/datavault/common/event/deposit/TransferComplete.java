package org.datavault.common.event.deposit;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavault.common.event.Event;

@Entity
@Table(name="Events")
public class TransferComplete extends Event {
    
    TransferComplete() {};
    public TransferComplete(String depositId) {
        super(depositId, "File transfer completed");
        this.eventClass = TransferComplete.class.getCanonicalName();
    }
}
