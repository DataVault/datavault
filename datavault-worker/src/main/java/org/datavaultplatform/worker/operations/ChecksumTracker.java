package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.worker.tasks.ChecksumHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.Callable;

@Slf4j
public class ChecksumTracker implements Callable<ChecksumHelper> {

    private final File chunk;
    private final int chunkNumber;

    public ChecksumTracker(File chunk, int chunkNumber) {
        this.chunk = chunk;
        this.chunkNumber = chunkNumber;
    }

    @Override
    public ChecksumHelper call() throws Exception {

        String hash = Verify.getDigest(this.chunk);
        ChecksumHelper retVal = new ChecksumHelper(this.chunkNumber, hash, chunk);
        return retVal;

//        long chunkSize = this.chunk.length();
//        logger.info("Chunk file " + i + ": " + chunkSize + " bytes");
//        logger.info("Chunk file location: " + chunk.getAbsolutePath());
//        logger.info("Checksum algorithm: " + tarHashAlgorithm);
//        logger.info("Checksum: " + chunksHash[i]);
//
//        chunksDigest.put(i+1, chunksHash[i]);
    }

    public File getChunk() {
        return this.chunk;
    }

    public int getChunkNumber() {
        return this.chunkNumber;
    }
}
