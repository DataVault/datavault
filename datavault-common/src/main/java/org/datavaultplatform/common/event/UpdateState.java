package org.datavaultplatform.common.event;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="Events")
public class UpdateState extends Event {
    
    public int state;
    public long progress;
    public long progressMax;
    public String progressMessage;
    
    UpdateState() {};
    public UpdateState(String jobId, String depositId, int state) {
        this(jobId, depositId, state, 0, 0, "");
    }
    public UpdateState(String jobId, String depositId, int state, long progress, long progressMax, String progressMessage) {
        super(jobId, depositId, "Job state: " + state);
        this.eventClass = UpdateState.class.getCanonicalName();
        this.state = state;
        this.progress = progress;
        this.progressMax = progressMax;
        this.progressMessage = progressMessage;
        this.persistent = false;
    }
    
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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
