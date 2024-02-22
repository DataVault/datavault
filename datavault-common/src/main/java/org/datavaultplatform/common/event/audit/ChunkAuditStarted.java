package org.datavaultplatform.common.event.audit;

import org.datavaultplatform.common.event.Event;

import jakarta.persistence.Entity;

@Entity
public class ChunkAuditStarted extends Event {

    public ChunkAuditStarted(){}

    public ChunkAuditStarted(String jobId, String auditId, String chunkId, String archiveId, String location) {
        super(jobId, auditId, chunkId, archiveId, location, "Audit Chunk Started: "+chunkId);
        this.chunkId = chunkId;
        this.eventClass = ChunkAuditStarted.class.getCanonicalName();
    }
}
