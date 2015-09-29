package org.datavaultplatform.common.event.deposit;

import org.datavaultplatform.common.event.Event;

public class TransferProgress extends Event {
    
    public long bytes;
    public long bytesPerSec;

    TransferProgress() {};
    public TransferProgress(String jobId, String depositId, long bytes, long bytesPerSec) {
        super(jobId, depositId, "Bytes transferred: " + bytes + " bytes");
        this.eventClass = TransferProgress.class.getCanonicalName();
        this.bytes = bytes;
        this.bytesPerSec = bytesPerSec;
        this.persistent = false;
    }
    
    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public long getBytesPerSec() {
        return bytesPerSec;
    }

    public void setBytesPerSec(long bytesPerSec) {
        this.bytesPerSec = bytesPerSec;
    }
}
