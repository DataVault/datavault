package org.datavaultplatform.common.event.deposit;

import com.fasterxml.jackson.annotation.JsonGetter;
import jakarta.persistence.Transient;
import org.datavaultplatform.common.event.Event;

public class TransferProgress extends Event {
    
    private long bytes;
    
    @Transient
    private long bytesPerSec;

    public TransferProgress() {
        this.eventClass = TransferProgress.class.getCanonicalName();
    }

    public TransferProgress(String jobId, String depositId, long bytes, long bytesPerSec) {
        super("Bytes transferred: " + bytes + " bytes");
        this.eventClass = TransferProgress.class.getCanonicalName();
        this.bytes = bytes;
        this.bytesPerSec = bytesPerSec;
        this.persistent = false;
        this.depositId = depositId;
        this.setJobId(jobId);
    }
    
    @JsonGetter
    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    @JsonGetter
    public long getBytesPerSec() {
        return bytesPerSec;
    }

    public void setBytesPerSec(long bytesPerSec) {
        this.bytesPerSec = bytesPerSec;
    }
}
