package org.datavaultplatform.common.event.deposit;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class PackageComplete extends Event {
    
    public PackageComplete() {
    }
    public PackageComplete(String jobId, String depositId) {
        super("Packaging completed");
        this.eventClass = PackageComplete.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
