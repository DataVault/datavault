package org.datavaultplatform.common.event.deposit;

import java.util.HashMap;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavaultplatform.common.event.Event;

@Entity
@Table(name="Events")
public class Complete extends Event {
    
    public String archiveId;
    
    Complete() {};
    public Complete(String jobId, String depositId, String archiveId) {
        
        super(jobId, depositId, "Deposit completed");
        this.eventClass = Complete.class.getCanonicalName();
        this.archiveId = archiveId;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }
}
