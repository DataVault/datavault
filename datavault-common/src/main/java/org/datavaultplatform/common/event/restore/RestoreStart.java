package org.datavaultplatform.common.event.restore;

import org.datavaultplatform.common.event.Event;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="Events")
public class RestoreStart extends Event {

    RestoreStart() {};
    public RestoreStart(String jobId, String depositId, String restoreId) {
        super(jobId, depositId, restoreId, "Deposit restore started");
        this.eventClass = RestoreStart.class.getCanonicalName();
    }
}
