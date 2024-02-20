package org.datavaultplatform.common.event.deposit;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.custom.HashMapConverter;

import java.util.HashMap;

@Entity
public class ComputedEncryption extends Event implements ChunksDigestEvent {

    @Convert(converter = HashMapConverter.class)
    @Column(name="chunkIVs", columnDefinition="LONGBLOB")
    private HashMap<Integer, byte[]> chunkIVs = new HashMap<>();

    @Convert(converter = HashMapConverter.class)
    @Column(name="encChunkDigests", columnDefinition="LONGBLOB")
    private HashMap<Integer, String> encChunkDigests = new HashMap<>();

    @Lob
    @Column(name="tarIV", columnDefinition="TINYBLOB")
    private byte[] tarIV;
    private String encTarDigest;
    private String aesMode;

    @Convert(converter = HashMapConverter.class)
    @Column(name="chunksDigest", columnDefinition="LONGBLOB")
    private HashMap<Integer, String> chunksDigest = new HashMap<>();

    private String digestAlgorithm;
    
    public ComputedEncryption() {
    }
    public ComputedEncryption(
            String jobId,
            String depositId,
            HashMap<Integer, byte[]> chunkIVs,
            byte[] tarIV,
            String aesMode,
            String digest,
            HashMap<Integer, String> digests,
            HashMap<Integer, String> chunksDigest,
            String digestAlgorithm) {
        super("Chunks encrypted with AES/" + aesMode);
        this.eventClass = ComputedEncryption.class.getCanonicalName();
        this.chunkIVs = chunkIVs;
        this.tarIV = tarIV;
        this.encTarDigest = digest;
        this.setAesMode(aesMode);
        this.encChunkDigests = digests;
        this.depositId = depositId;
        this.jobId = jobId;
        this.chunksDigest = chunksDigest;
        this.digestAlgorithm = digestAlgorithm;
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
