package org.datavaultplatform.common.event;

import javax.persistence.Entity;
import javax.persistence.Table;

// A fatal error that caused a job to fail

@Entity
public class Error extends Event {

    public Error() { }
    public Error(String jobId, String depositId, String message) {
        super(message);
        this.eventClass = Error.class.getCanonicalName();
        
        // Optional?
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
