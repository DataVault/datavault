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
    private int chunkNumber;
    private Context context;
    private static final Logger logger = LoggerFactory.getLogger(EncryptionTracker.class);

    @Override
    public EncryptionHelper call() throws Exception {
        //File chunk = chunkFiles[i];

        // Generating IV
        byte[] chunkIV = Encryption.encryptFile(this.context, this.chunk);

        String encChunksHash = Verify.getDigest(this.chunk);

        logger.info("Chunk file " + this.chunkNumber + ": " + this.chunk.length() + " bytes");
        logger.info("Encrypted chunk checksum: " + encChunksHash);
        EncryptionHelper retVal = new EncryptionHelper();
        retVal.setIv(chunkIV);
        retVal.setEncTarHash(encChunksHash);
        retVal.setChunkNumber(this.chunkNumber);
        logger.debug("Chunk encryption task completed: " + this.chunkNumber);
        return retVal;
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

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
