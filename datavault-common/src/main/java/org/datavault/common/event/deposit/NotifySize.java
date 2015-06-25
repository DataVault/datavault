package org.datavault.common.event.deposit;

import org.datavault.common.event.Event;

public class NotifySize extends Event {
    
    public long bytes;

    NotifySize() {};
    public NotifySize(String depositId, long bytes) {
        super(depositId, null);
        this.eventClass = NotifySize.class.getCanonicalName();
        this.bytes = bytes;
    }
    
    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }
}
