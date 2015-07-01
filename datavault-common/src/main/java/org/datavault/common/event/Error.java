package org.datavault.common.event;

import javax.persistence.Entity;
import javax.persistence.Table;

// A fatal error that caused a job to fail

@Entity
@Table(name="Events")
public class Error extends Event {

    public Error() { }
    public Error(String depositId, String message) {
        super(depositId, message);
        this.eventClass = Error.class.getCanonicalName();
    }
}
