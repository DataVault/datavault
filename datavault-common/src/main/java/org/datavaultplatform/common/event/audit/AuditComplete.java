package org.datavaultplatform.common.event.audit;

import org.datavaultplatform.common.event.Event;

import jakarta.persistence.Entity;

@Entity
public class AuditComplete extends Event{
    public AuditComplete() {
    }
    public AuditComplete(String jobId, String auditId) {
        super(jobId, auditId, null, null, null, "Audit completed");
        this.eventClass = AuditComplete.class.getCanonicalName();
    }
}
