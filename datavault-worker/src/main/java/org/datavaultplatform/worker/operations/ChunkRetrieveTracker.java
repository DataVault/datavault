package org.datavaultplatform.worker.operations;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.utils.Utils;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
public class ChunkRetrieveTracker implements Callable<Object> {
    private final Context context;
    private final String archiveId;
    private final Boolean multipleCopies;
    private final String location;
    private final Device archiveStore;
    private final int chunkNumber;
    private final Map<Integer, byte[]> ivs;
    private final Progress progress;
    private Map<Integer, String> chunksDigest = null;
    private Map<Integer, String> encChunksDigest = null;
    private File chunkFile;


    public ChunkRetrieveTracker(String archiveId, Device archiveStore,
                                Context context,
                                int chunkNumber,
                                Map<Integer, byte[]> ivs,
                                String location, Boolean multipleCopies,
                                Progress progress,
                                Map<Integer, String> chunksDigest,
                                Map<Integer, String> encChunksDigest,
                                File chunkFile) {
        this.context = context;
        this.archiveId = archiveId;
        this.multipleCopies = multipleCopies;
        this.location = location;
        this.archiveStore = archiveStore;
        this.chunkNumber = chunkNumber;
        this.ivs = ivs;
        this.progress = progress;
        this.chunksDigest = chunksDigest;
        this.encChunksDigest = encChunksDigest;
        this.chunkFile = chunkFile;
    }

    @Override
    public Object call() throws Exception {

        //Path chunkPath = this.context.getTempDir().resolve(this.getArchiveId() + FileSplitter.CHUNK_SEPARATOR + this.getChunkNumber());
        //File chunkFile = chunkPath.toFile();
        String chunkArchiveId = this.getArchiveId() + FileSplitter.CHUNK_SEPARATOR + this.getChunkNumber();

        if (! this.getMultipleCopies()) {
            this.getArchiveStore().retrieve(chunkArchiveId, this.getChunkFile(), this.getProgress());
        } else {
            this.getArchiveStore().retrieve(chunkArchiveId, this.getChunkFile(), this.getProgress(), this.getLocation());
        }

        byte[] chunkIV = this.ivs.get(this.getChunkNumber());
        if( chunkIV != null ) {
            String archivedEncChunkFileHash = this.getEncChunksDigest().get(this.getChunkNumber());

            // Check encrypted file checksum
            Utils.checkFileHash("ret-enc-chunk", this.getChunkFile(), archivedEncChunkFileHash);
            Encryption.decryptFile(this.getContext(), this.getChunkFile(), this.getIvs().get(this.getChunkNumber()));
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

    public String getLocation() {
        return location;
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
