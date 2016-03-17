package org.datavaultplatform.common.event.deposit;

import java.util.HashMap;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavaultplatform.common.event.Event;

@Entity
@Table(name="Events")
public class Complete extends Event {
    
    public String archiveId;
    public long archiveSize;
    
    Complete() {};
    public Complete(String jobId, String depositId, String archiveId, long archiveSize) {
        
        super("Deposit completed");
        
        this.eventClass = Complete.class.getCanonicalName();
        this.archiveId = archiveId;
        this.archiveSize = archiveSize;
        this.depositId = depositId;
        this.jobId = jobId;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public long getArchiveSize() {
        return archiveSize;
    }

    public void setArchiveSize(long archiveSize) {
        this.archiveSize = archiveSize;
    }
}
