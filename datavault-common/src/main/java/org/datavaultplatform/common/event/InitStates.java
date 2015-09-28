package org.datavaultplatform.common.event;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;

@Entity
@Table(name="Events")
public class InitStates extends Event {

    public ArrayList<String> states;
    
    InitStates() {};
    public InitStates(String jobId, String depositId, ArrayList<String> states) {
        super(jobId, depositId, "Job states: " + states.size());
        this.eventClass = InitStates.class.getCanonicalName();
        this.states = states;
        this.persistent = false;
    }

    public ArrayList<String> getStates() {
        return states;
    }

    public void setStates(ArrayList<String> states) {
        this.states = states;
    }
}
