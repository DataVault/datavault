package org.datavaultplatform.common.event;

import javax.persistence.Entity;

import static org.datavaultplatform.common.event.Error.Type.*;

// A fatal error that caused a job to fail

@Entity
public class Error extends Event {

    public enum Type {DEPOSIT, AUDIT;}

    public Error() { }

    public Error(String jobId, String depositId, String message) {
        super(message);
        this.eventClass = Error.class.getCanonicalName();

        // Optional?
        this.depositId = depositId;
        this.jobId = jobId;
    }

    public Error(String jobId, String sourceId, String message, Type type) {
        super(message);
        this.eventClass = Error.class.getCanonicalName();

        this.jobId = jobId;
        switch (type){
            case DEPOSIT:
                this.depositId = sourceId;
                break;
            case AUDIT:
                this.auditId = sourceId;
                break;
        }
    }
}
