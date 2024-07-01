package org.datavaultplatform.common.event.deposit;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class CompleteChunkValidation extends Event {
    public CompleteChunkValidation() {
    }

    public CompleteChunkValidation(String jobId, String depositId, String type, int chunkNum) {
        super("Chunk " + chunkNum + " validation finished - (" + type + ")");
        this.eventClass = CompleteChunkValidation.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
