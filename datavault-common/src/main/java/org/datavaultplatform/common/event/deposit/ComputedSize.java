package org.datavaultplatform.common.event.deposit;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class ComputedSize extends Event {
    
    public long bytes;

    public ComputedSize() {
    }
    public ComputedSize(String jobId, String depositId, long bytes) {
        super("Deposit size: " + bytes + " bytes");
        this.eventClass = ComputedSize.class.getCanonicalName();
        this.bytes = bytes;
        this.depositId = depositId;
        this.jobId = jobId;
    }
    
    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }
}
