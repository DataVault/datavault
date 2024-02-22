package org.datavaultplatform.common.event.audit;

import org.datavaultplatform.common.event.Event;

import jakarta.persistence.Entity;

@Entity
public class AuditError extends Event {

    public AuditError() {
    }
    public AuditError(String jobId, String auditId, String chunkId, String archiveId, String location) {
        super(jobId, auditId, chunkId, archiveId, location, "Error while Auditing: "+chunkId);
        this.eventClass = AuditError.class.getCanonicalName();
    }

    public AuditError(String jobId, String auditId, String chunkId, String archiveId, String location, String message) {
        super(jobId, auditId, chunkId, archiveId, location, message);
        this.eventClass = AuditError.class.getCanonicalName();
    }
}
