package org.datavaultplatform.worker.tasks;
import java.io.File;

public class ChecksumHelper {

    private String chunkHash;
    private int chunkNumber;
    private File chunk;

    public String getChunkHash() {
        return chunkHash;
    }

    public void setChunkHash(String chunkHash) {
        this.chunkHash = chunkHash;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public File getChunk() {
        return this.chunk;
    }

    public void setChunk(File chunk) {
        this.chunk = chunk;
    }
}
