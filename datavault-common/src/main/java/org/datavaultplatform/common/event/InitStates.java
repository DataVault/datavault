package org.datavaultplatform.common.event;

import jakarta.persistence.Entity;
import java.util.ArrayList;
import jakarta.persistence.Transient;

@Entity
public class InitStates extends Event {

    @Transient
    public ArrayList<String> states;
    
    public InitStates() {
    }
    public InitStates(String jobId, String depositId, ArrayList<String> states) {
        super("Job states: " + states.size());
        this.eventClass = InitStates.class.getCanonicalName();
        this.states = states;
        this.persistent = false;
        
        // Optional?
        this.depositId = depositId;
        this.jobId = jobId;
    }

    public ArrayList<String> getStates() {
        return states;
    }

    public void setStates(ArrayList<String> states) {
        this.states = states;
    }
}
