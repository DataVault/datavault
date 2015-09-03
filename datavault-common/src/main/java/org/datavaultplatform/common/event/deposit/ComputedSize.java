package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavaultplatform.common.event.Event;

@Entity
@Table(name="Events")
public class ComputedSize extends Event {
    
    public long bytes;

    ComputedSize() {};
    public ComputedSize(String depositId, long bytes) {
        super(depositId, "Deposit size: " + bytes + " bytes");
        this.eventClass = ComputedSize.class.getCanonicalName();
        this.bytes = bytes;
    }
    
    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }
}
