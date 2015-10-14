package org.datavaultplatform.common.event.deposit;

import java.util.HashMap;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavaultplatform.common.event.Event;

@Entity
@Table(name="Events")
public class Complete extends Event {
    
    public String archiveDevice;
    public String archiveId;
    
    Complete() {};
    public Complete(String jobId,
                    String depositId,
                    String archiveDevice,
                    String archiveId) {
        
        super(jobId, depositId, "Deposit completed");
        this.eventClass = Complete.class.getCanonicalName();
        this.archiveDevice = archiveDevice;
        this.archiveId = archiveId;
    }

    public String getArchiveDevice() {
        return archiveDevice;
    }

    public void setArchiveDevice(String archiveDevice) {
        this.archiveDevice = archiveDevice;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }
}
