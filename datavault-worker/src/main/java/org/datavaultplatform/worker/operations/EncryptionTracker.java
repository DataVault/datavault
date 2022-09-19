package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.tasks.EncryptionChunkHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.Callable;

@Slf4j
public class EncryptionTracker implements Callable<EncryptionChunkHelper> {


    private final File chunk;
    private final int chunkNumber;
    private final Context context;

    public EncryptionTracker(int chunkNumber, File chunk, Context ctx) {
        this.chunk = chunk;
        this.chunkNumber = chunkNumber;
        this.context = ctx;
    }

    @Override
    public EncryptionChunkHelper call() throws Exception {
        //File chunk = chunkFiles[i];

        // Generating IV
        byte[] chunkIV = Encryption.encryptFile(this.context, this.chunk);

        String encChunksHash = Verify.getDigest(this.chunk);

        log.info("Chunk file " + this.chunkNumber + ": " + this.chunk.length() + " bytes");
        log.info("Encrypted chunk [{}] checksum: {}", this.chunkNumber, encChunksHash);
        EncryptionChunkHelper retVal = new EncryptionChunkHelper(chunkIV, encChunksHash, chunkNumber);
        log.debug("Chunk encryption task completed: " + this.chunkNumber);
        return retVal;
    }

    public File getChunk() {
        return this.chunk;
    }

    public int getChunkNumber() {
        return this.chunkNumber;
    }

    public Context getContext() {
        return this.context;
    }

}
