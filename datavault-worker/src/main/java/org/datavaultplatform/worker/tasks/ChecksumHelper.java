package org.datavaultplatform.worker.tasks;
import java.io.File;

public class ChecksumHelper {

    private String chunkHash;
    private int chunkCount;
    private File chunk;

    public String getChunkHash() {
        return chunkHash;
    }

    public void setChunkHash(String chunkHash) {
        this.chunkHash = chunkHash;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    public File getChunk() {
        return this.chunk;
    }

    public void setChunk(File chunk) {
        this.chunk = chunk;
    }
}
