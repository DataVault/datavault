package org.datavaultplatform.common.event.deposit;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonGetter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.custom.HashMapConverter;

@Entity
public class ComputedChunks extends Event implements ChunksDigestEvent {

    @Lob
    @Convert(converter = HashMapConverter.IntegerString.class)
    @Column(name="chunksDigest", columnDefinition="LONGBLOB")
    private HashMap<Integer, String> chunksDigest = new HashMap<>();

    private String digestAlgorithm;
    
    public ComputedChunks() {
    }
    public ComputedChunks(
            String jobId,
            String depositId,
            HashMap<Integer, String> chunksDigest,
            String digestAlgorithm) {
        super("Number of chunks: " + chunksDigest.size());
        this.eventClass = ComputedChunks.class.getCanonicalName();
        this.chunksDigest = chunksDigest;
        this.digestAlgorithm = digestAlgorithm;
        this.depositId = depositId;
        this.setJobId(jobId);
    }
    
    @JsonGetter
    public HashMap<Integer, String> getChunksDigest() {
        return chunksDigest;
    }
    
    public void setChunksDigest(HashMap<Integer, String> chunksDigest) {
        this.chunksDigest = chunksDigest;
    }
    
    @JsonGetter
    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }
    
    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }
}
