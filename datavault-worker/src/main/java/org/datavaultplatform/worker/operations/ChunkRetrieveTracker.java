package org.datavaultplatform.worker.operations;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.utils.Utils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
public class ChunkRetrieveTracker implements Callable<File> {
    private final Context context;
    private final String archiveId;
    private final boolean multipleCopies;
    private final List<String> locations;
    private final Device archiveStore;
    private final int chunkNumber;
    private final Map<Integer, byte[]> ivs;
    private final Progress progress;
    private final Map<Integer, String> chunksDigest;
    private final Map<Integer, String> encChunksDigest;
    private final File chunkFile;


    public ChunkRetrieveTracker(String archiveId, Device archiveStore,
                                Context context,
                                int chunkNumber,
                                Map<Integer, byte[]> ivs,
                                List<String> locations, boolean multipleCopies,
                                Progress progress,
                                Map<Integer, String> chunksDigest,
                                Map<Integer, String> encChunksDigest,
                                File chunkFile) {
        this.context = context;
        this.archiveId = archiveId;
        this.multipleCopies = multipleCopies;
        this.locations = locations;
        this.archiveStore = archiveStore;
        this.chunkNumber = chunkNumber;
        this.ivs = ivs;
        this.progress = progress;
        this.chunksDigest = chunksDigest;
        this.encChunksDigest = encChunksDigest;
        this.chunkFile = chunkFile;
    }

    @Override
    public File call() throws Exception {
        String chunkArchiveId = this.getArchiveId() + FileSplitter.CHUNK_SEPARATOR + this.getChunkNumber();

        if (this.getMultipleCopies()) {
            this.getArchiveStore().retrieve(chunkArchiveId, this.getChunkFile(), this.getProgress(), locations);
        } else {
            this.getArchiveStore().retrieve(chunkArchiveId, this.getChunkFile(), this.getProgress());
        }

        byte[] chunkIV = this.ivs.get(this.getChunkNumber());
        if( chunkIV != null ) {
            String archivedEncChunkFileHash = this.getEncChunksDigest().get(this.getChunkNumber());

            // Check encrypted file checksum
            Utils.checkFileHash("ret-enc-chunk", this.getChunkFile(), archivedEncChunkFileHash);
            Encryption.decryptFile(this.getContext(), this.getChunkFile(), chunkIV);
        }

        // Check file
        String archivedChunkFileHash = this.getChunksDigest().get(this.getChunkNumber());

        // TODO: Should we check algorithm each time or assume main tar file algorithm is the same
        // We might also want to move algorithm check before this loop

        Utils.checkFileHash("ret-chunk", this.getChunkFile(), archivedChunkFileHash);

        log.debug("Chunk download task completed: " + this.getChunkNumber());
        return this.getChunkFile();
    }

    public Context getContext() {
        return context;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public Boolean getMultipleCopies() {
        return multipleCopies;
    }

    public List<String> getLocations() {
        return locations;
    }

    public Device getArchiveStore() {
        return archiveStore;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public Map<Integer, byte[]> getIvs() {
        return ivs;
    }

    public Map<Integer, String> getChunksDigest() {
        return chunksDigest;
    }

    public Map<Integer, String> getEncChunksDigest() {
        return encChunksDigest;
    }

    public Progress getProgress() {
        return progress;
    }

    public File getChunkFile() {
        return chunkFile;
    }
}
