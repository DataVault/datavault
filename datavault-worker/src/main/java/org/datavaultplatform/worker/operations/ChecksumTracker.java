package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.worker.tasks.ChecksumHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.Callable;

@Slf4j
public record ChecksumTracker(File chunk, int chunkNumber) implements Callable<ChecksumHelper> {

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
}
