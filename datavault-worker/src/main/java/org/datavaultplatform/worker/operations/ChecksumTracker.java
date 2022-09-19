package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.worker.tasks.ChecksumHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Callable;

public class ChecksumTracker implements Callable {

    private File chunk;
    private int chunkNumber;
    private static final Logger logger = LoggerFactory.getLogger(ChecksumTracker.class);

    @Override
    public ChecksumHelper call() throws Exception {

        ChecksumHelper retVal = new ChecksumHelper();
        retVal.setChunkNumber(this.chunkNumber);
        retVal.setChunkHash(Verify.getDigest(this.chunk));
        retVal.setChunk(this.chunk);
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

    public void setChunk(File chunk) {
        this.chunk = chunk;
    }

    public int getChunkNumber() {
        return this.chunkNumber;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }
}
