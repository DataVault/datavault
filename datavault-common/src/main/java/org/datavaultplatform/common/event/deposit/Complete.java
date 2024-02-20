package org.datavaultplatform.common.event.deposit;

import java.util.HashMap;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.custom.HashMapConverter;

@Entity
public class Complete extends Event {

    // Maps the model ArchiveStore Id to the generated Archive Id
    @Convert(converter = HashMapConverter.class)
    //@Lob
    @Column(name = "archiveIds", columnDefinition="TINYBLOB")
    private HashMap<String, String> archiveIds = new HashMap<>();

    //public String archiveId;
    public long archiveSize;

    public Complete() {}
    public Complete(String jobId, String depositId, HashMap<String, String> archiveIds, long archiveSize) {

        super("Deposit completed");

        this.eventClass = Complete.class.getCanonicalName();
        this.archiveIds = archiveIds;
        this.archiveSize = archiveSize;
        this.depositId = depositId;
        this.jobId = jobId;
    }

    public HashMap<String, String> getArchiveIds() {
        return archiveIds;
    }

    public void setArchiveIds(HashMap<String, String> archiveIds) {
        this.archiveIds = archiveIds;
    }

    public long getArchiveSize() {
        return archiveSize;
    }

    public void setArchiveSize(long archiveSize) {
        this.archiveSize = archiveSize;
    }

}
