package org.datavault.common.event.deposit;

import org.datavault.common.event.Event;

public class TransferProgress extends Event {
    
    public long bytes;

    TransferProgress() {};
    public TransferProgress(String depositId, long bytes) {
        super(depositId, "Bytes transferred: " + bytes + " bytes");
        this.eventClass = TransferProgress.class.getCanonicalName();
        this.bytes = bytes;
        this.persistent = false;
    }
    
    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }
}
