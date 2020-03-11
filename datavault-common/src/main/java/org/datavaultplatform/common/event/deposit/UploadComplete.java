package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class UploadComplete extends Event {
    UploadComplete() {

    };

    public UploadComplete(String jobId, String depositId) {
        super("Upload completed");
        this.eventClass = UploadComplete.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
    }
}
