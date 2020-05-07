package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class StartTarValidation extends Event {
    StartTarValidation() {

    };

    public StartTarValidation(String jobId, String depositId) {
        super("Tar validation started");
        this.eventClass = StartTarValidation.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
