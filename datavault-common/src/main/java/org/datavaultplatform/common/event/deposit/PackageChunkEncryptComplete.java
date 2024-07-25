package org.datavaultplatform.common.event.deposit;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class PackageChunkEncryptComplete extends Event {
    public PackageChunkEncryptComplete() {
    }

    public PackageChunkEncryptComplete(String jobId, String depositId) {
        super("Package Chunk Encrypt Complete");
        this.eventClass = PackageChunkEncryptComplete.class.getCanonicalName();
        this.depositId = depositId;
        this.setJobId(jobId);
    }
}
