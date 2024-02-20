package org.datavaultplatform.common.event.deposit;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class CompleteTarValidation extends Event {
    public CompleteTarValidation() {
    }

    public CompleteTarValidation(String jobId, String depositId) {
        super("Tar validation completed");
        this.eventClass = CompleteTarValidation.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
