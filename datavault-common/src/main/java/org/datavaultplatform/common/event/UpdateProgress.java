package org.datavaultplatform.common.event;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
public class UpdateProgress extends Event {
    
    @Transient
    public long progress;
    
    @Transient
    public long progressMax;
    
    @Transient
    public String progressMessage;
    
    public UpdateProgress() {
    }
    public UpdateProgress(String jobId, String depositId) {
        this(jobId, depositId, 0, 0, "");
    }
    public UpdateProgress(String jobId, String depositId, long progress, long progressMax, String progressMessage) {
        super("Job progress update");
        this.eventClass = UpdateProgress.class.getCanonicalName();
        this.progress = progress;
        this.progressMax = progressMax;
        this.progressMessage = progressMessage;
        this.persistent = false;
        
        // Optional?
        this.depositId = depositId;
        this.jobId = jobId;
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
