package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class CompleteTarValidation extends Event {
    CompleteTarValidation() {

    };

    public CompleteTarValidation(String jobId, String depositId) {
        super("Tar validation completed");
        this.eventClass = CompleteTarValidation.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
