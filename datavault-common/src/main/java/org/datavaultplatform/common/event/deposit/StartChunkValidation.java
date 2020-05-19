package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class StartChunkValidation extends Event {
    StartChunkValidation() {

    };

    public StartChunkValidation(String jobId, String depositId, String type, int chunkNum) {
        super("Chunk " + chunkNum + " validation started - (" + type + ")");
        this.eventClass = StartChunkValidation.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
