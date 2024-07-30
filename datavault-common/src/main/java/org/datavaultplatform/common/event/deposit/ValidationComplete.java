package org.datavaultplatform.common.event.deposit;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class ValidationComplete extends Event {
    public ValidationComplete() {
    }

    public ValidationComplete(String jobId, String depositId) {
        super("Validation completed");
        this.eventClass = ValidationComplete.class.getCanonicalName();
        this.depositId = depositId;
        this.setJobId(jobId);
    }
}
