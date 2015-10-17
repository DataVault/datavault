package org.datavaultplatform.common.event.restore;

import org.datavaultplatform.common.event.Event;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="Events")
public class RestoreComplete extends Event {

    RestoreComplete() {};
    public RestoreComplete(String jobId, String depositId, String restoreId) {
        super(jobId, depositId, restoreId, "Deposit restore completed");
        this.eventClass = RestoreComplete.class.getCanonicalName();
    }
}
