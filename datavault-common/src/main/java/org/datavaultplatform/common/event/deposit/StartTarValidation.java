package org.datavaultplatform.common.event.deposit;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class StartTarValidation extends Event {
    public StartTarValidation() {
    }

    public StartTarValidation(String jobId, String depositId) {
        super("Tar validation started");
        this.eventClass = StartTarValidation.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
