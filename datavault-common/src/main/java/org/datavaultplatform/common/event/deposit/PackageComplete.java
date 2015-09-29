package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavaultplatform.common.event.Event;

@Entity
@Table(name="Events")
public class PackageComplete extends Event {
    
    PackageComplete() {};
    public PackageComplete(String jobId, String depositId) {
        super(jobId, depositId, "Packaging completed");
        this.eventClass = PackageComplete.class.getCanonicalName();
    }
}
