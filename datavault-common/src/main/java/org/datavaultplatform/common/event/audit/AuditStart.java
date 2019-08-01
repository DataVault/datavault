package org.datavaultplatform.common.event.audit;

import org.datavaultplatform.common.event.Event;

import javax.persistence.Entity;

@Entity
public class AuditStart extends Event {

    AuditStart() {};
    public AuditStart(String jobId, String audiId) {
        super(jobId, null, audiId, "Audit started");
        this.eventClass = AuditStart.class.getCanonicalName();
    }
}
