package org.datavaultplatform.common.event;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="Events")
public class UpdateState extends Event {
    
    public int state;

    UpdateState() {};
    public UpdateState(String jobId, String depositId, int state) {
        super(jobId, depositId, "Job state: " + state);
        this.eventClass = UpdateState.class.getCanonicalName();
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
