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
    @Column(name="tarIV", columnDefinition="BLOB")
    public byte[] tarIV;
    @Column(name="AESMode", columnDefinition="TEXT")
    private String aesMode;
    
    ComputedEncryption() {};
    public ComputedEncryption(String jobId, String depositId, HashMap<Integer, byte[]> chunkIVs, byte[] tarIV, String aesMode) {
        super("Chunks ecrypted with AES " + aesMode);
        this.eventClass = ComputedEncryption.class.getCanonicalName();
        this.chunkIVs = chunkIVs;
        this.tarIV = tarIV;
        this.setAesMode(aesMode);
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
}
