package org.datavaultplatform.common.event.audit;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class ChunkAuditComplete extends Event {

    public ChunkAuditComplete(){}

    public ChunkAuditComplete(String jobId, String auditId, String chunkId, String archiveId, String location) {
        super(jobId, auditId, chunkId, archiveId, location, "Chunk Audit Complete: "+chunkId);
        this.chunkId = chunkId;
        this.eventClass = ChunkAuditComplete.class.getCanonicalName();
    }
}
