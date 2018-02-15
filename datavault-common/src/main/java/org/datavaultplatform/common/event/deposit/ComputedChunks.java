package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavaultplatform.common.event.Event;

@Entity
public class ComputedChunks extends Event {
    
    public int numOfChunks;
    
    ComputedChunks() {};
    public ComputedChunks(String jobId, String depositId, int numOfChunks) {
        super("Number of chunks: " + numOfChunks);
        this.eventClass = ComputedChunks.class.getCanonicalName();
        this.numOfChunks = numOfChunks;
        this.depositId = depositId;
        this.jobId = jobId;
    }
    
    public int getNumOfChunks() {
        return numOfChunks;
    }
    
    public void setNumOfChunks(int numOfChunks) {
        this.numOfChunks = numOfChunks;
    }
}
