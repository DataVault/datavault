package org.datavaultplatform.common.event.deposit;

import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import org.datavaultplatform.common.event.Event;

@Entity
public class ComputedChunks extends Event {
    
    @Column(name="chunksDigest", columnDefinition="LONGBLOB")
    public HashMap<Integer, String> chunksDigest;
    public String digestAlgorithm;
    
    ComputedChunks() {};
    public ComputedChunks(String jobId, String depositId, HashMap<Integer, String> chunksDigest, String digestAlgorithm) {
        super("Number of chunks: " + chunksDigest.size());
        this.eventClass = ComputedChunks.class.getCanonicalName();
        this.chunksDigest = chunksDigest;
        this.digestAlgorithm = digestAlgorithm;
        this.depositId = depositId;
        this.jobId = jobId;
    }
    
    public HashMap<Integer, String> getChunksDigest() {
        return chunksDigest;
    }
    
    public void setChunksDigest(HashMap<Integer, String> chunksDigest) {
        this.chunksDigest = chunksDigest;
    }
    
    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }
    
    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }
}
