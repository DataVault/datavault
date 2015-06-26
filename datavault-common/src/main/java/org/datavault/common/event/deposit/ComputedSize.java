package org.datavault.common.event.deposit;

import org.datavault.common.event.Event;

public class ComputedSize extends Event {
    
    public long bytes;

    ComputedSize() {};
    public ComputedSize(String depositId, long bytes) {
        super(depositId, null);
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
