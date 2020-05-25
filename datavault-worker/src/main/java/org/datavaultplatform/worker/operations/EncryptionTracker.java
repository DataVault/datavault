package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.tasks.EncryptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Callable;

public class EncryptionTracker implements Callable {

    private File chunk;
    private int chunkCount;
    private Context context;
    private static final Logger logger = LoggerFactory.getLogger(EncryptionTracker.class);

    @Override
    public EncryptionHelper call() throws Exception {
        //File chunk = chunkFiles[i];

        // Generating IV
        byte[] chunkIV = Encryption.encryptFile(this.context, this.chunk);

        String encChunksHash = Verify.getDigest(this.chunk);

        logger.info("Chunk file " + this.chunkCount + ": " + this.chunk.length() + " bytes");
        logger.info("Encrypted chunk checksum: " + encChunksHash);
        EncryptionHelper retVal = new EncryptionHelper();
        retVal.setIv(chunkIV);
        retVal.setEncTarHash(encChunksHash);
        retVal.setChunkCount(this.chunkCount);
        logger.debug("Chunk encryption thread completed: " + this.chunkCount);
        return retVal;
    }

    public File getChunk() {
        return this.chunk;
    }

    public void setChunk(File chunk) {
        this.chunk = chunk;
    }

    public int getChunkCount() {
        return this.chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
