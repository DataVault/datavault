package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import org.datavaultplatform.common.event.Event;

import java.util.HashMap;

@Entity
public class UploadComplete extends Event {

    // Maps the model ArchiveStore Id to the generated Archive Id
    HashMap<String, String> archiveIds = new HashMap<>();

    UploadComplete() {

    };

    public UploadComplete(String jobId, String depositId, HashMap<String, String> archiveIds) {
        super("Upload completed");
        this.eventClass = UploadComplete.class.getCanonicalName();
        this.depositId = depositId;
        this.jobId = jobId;
        this.archiveIds = archiveIds;
    }

    public HashMap<String, String> getArchiveIds() {
        return archiveIds;
    }

    public void setArchiveIds(HashMap<String, String> archiveIds) {
        this.archiveIds = archiveIds;
    }
}
