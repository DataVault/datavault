package org.datavaultplatform.common.event.deposit;

import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import org.datavaultplatform.common.event.Event;

@Entity
public class ComputedEncryption extends Event {
    
    @Column(name="chunkIVs", columnDefinition="LONGBLOB")
    public HashMap<Integer, byte[]> chunkIVs;
    @Column(name="encChunkDigests", columnDefinition="LONGBLOB")
    public HashMap<Integer, String> encChunkDigests;
    public byte[] tarIV;
    private String encTarDigest;
    private String aesMode;
    
    ComputedEncryption() {};
    public ComputedEncryption(String jobId, String depositId, HashMap<Integer, byte[]> chunkIVs, byte[] tarIV, String aesMode, String digest, HashMap<Integer, String> digests) {
        super("Chunks encrypted with AES/" + aesMode);
        this.eventClass = ComputedEncryption.class.getCanonicalName();
        this.chunkIVs = chunkIVs;
        this.tarIV = tarIV;
        this.encTarDigest = digest;
        this.setAesMode(aesMode);
        this.encChunkDigests = digests;
        this.depositId = depositId;
        this.jobId = jobId;
    }
    
    public HashMap<Integer, byte[]> getChunkIVs() {
        return chunkIVs;
    }
    
    public void setChunkIVs(HashMap<Integer, byte[]> chunkIVs) {
        this.chunkIVs = chunkIVs;
    }
    
    public byte[] getTarIV() {
        return tarIV;
    }
    
    public void setTarIV(byte[] tarIV) {
        this.tarIV = tarIV;
    }
    
    public String getAesMode() {
        return aesMode;
    }
    
    public void setAesMode(String aesMode) {
        this.aesMode = aesMode;
    }
    
    public String getEncTarDigest() {
        return encTarDigest;
    }
    
    public void setEncTarDigest(String encTarDigest) {
        this.encTarDigest = encTarDigest;
    }
    
    public HashMap<Integer, String> getEncChunkDigests() {
        return encChunkDigests;
    }
    
    public void setEncChunkDigests(HashMap<Integer, String> encChunkDigests) {
        this.encChunkDigests = encChunkDigests;
    }
    
    
}
