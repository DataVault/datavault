package org.datavaultplatform.worker.tasks;
import java.io.File;

public class ChecksumHelper {

    private final String chunkHash;
    private final int chunkNumber;
    private final File chunk;

    public ChecksumHelper(int chunkNumber, String chunkHash, File chunk) {
        this.chunkNumber = chunkNumber;
        this.chunk = chunk;
        this.chunkHash = chunkHash;
    }

    public String getChunkHash() {
        return chunkHash;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public File getChunk() {
        return this.chunk;
    }

}
