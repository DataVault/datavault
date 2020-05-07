package org.datavaultplatform.worker.tasks;

import java.io.File;
import java.util.Map;

public class PackageHelper {
    private byte[] iv;
    private String encTarHash;
    private Map<Integer, byte[]> chunksIVs;
    private Long archiveSize;
    private File tarFile;
    private String tarHash;


    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public String getEncTarHash() {
        return encTarHash;
    }

    public void setEncTarHash(String encTarHash) {
        this.encTarHash = encTarHash;
    }

    public Map<Integer, byte[]> getChunksIVs() {
        return chunksIVs;
    }

    public void setChunksIVs(Map<Integer, byte[]> chunksIVs) {
        this.chunksIVs = chunksIVs;
    }

    public Long getArchiveSize() {
        return archiveSize;
    }

    public void setArchiveSize(Long archiveSize) {
        this.archiveSize = archiveSize;
    }

    public File getTarFile() {
        return tarFile;
    }

    public void setTarFile(File tarFile) {
        this.tarFile = tarFile;
    }

    public String getTarHash() {
        return tarHash;
    }

    public void setTarHash(String tarHash) {
        this.tarHash = tarHash;
    }
}
