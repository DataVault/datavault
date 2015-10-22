package org.datavaultplatform.common.event;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="Events")
public class UpdateProgress extends Event {
    
    public long progress;
    public long progressMax;
    public String progressMessage;
    
    UpdateProgress() {};
    public UpdateProgress(String jobId, String depositId) {
        this(jobId, depositId, 0, 0, "");
    }
    public UpdateProgress(String jobId, String depositId, long progress, long progressMax, String progressMessage) {
        super(jobId, depositId, "Job progress update");
        this.eventClass = UpdateProgress.class.getCanonicalName();
        this.progress = progress;
        this.progressMax = progressMax;
        this.progressMessage = progressMessage;
        this.persistent = false;
    }
    
    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public long getProgressMax() {
        return progressMax;
    }

    public void setProgressMax(long progressMax) {
        this.progressMax = progressMax;
    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }
}
