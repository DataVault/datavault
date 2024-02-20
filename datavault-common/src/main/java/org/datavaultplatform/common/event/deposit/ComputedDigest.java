package org.datavaultplatform.common.event.deposit;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class ComputedDigest extends Event {
    
    private String digest;
    private String digestAlgorithm;
    
    public ComputedDigest() {
    }
    public ComputedDigest(String jobId, String depositId, String digest, String digestAlgorithm) {
        super(digestAlgorithm + ": " + digest);
        this.eventClass = ComputedDigest.class.getCanonicalName();
        this.digest = digest;
        this.digestAlgorithm = digestAlgorithm;
        this.depositId = depositId;
        this.jobId = jobId;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }
}
