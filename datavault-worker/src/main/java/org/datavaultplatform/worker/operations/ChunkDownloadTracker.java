package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.task.Context;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import org.datavaultplatform.worker.utils.Utils;

@Slf4j
public class ChunkDownloadTracker implements Callable<Object> {


    private final File chunkFile;
    private final Context context;
    private final String archiveId;
    private final Boolean multipleCopies;
    private final String location;
    private final ArchiveStore archiveStore;
    private final int chunkNumber;
    private final Boolean doVerification;
    private final Map<Integer, byte[]> ivs;
    private final String[] encChunksHash;

    public ChunkDownloadTracker(String archiveId, ArchiveStore archiveStore,
        File chunkFile, Context context,
        int chunkNumber, Boolean doVerification,
        String[] encChunksHash, Map<Integer, byte[]> ivs,
        String location, Boolean multipleCopies ) {
        this.chunkFile = chunkFile;
        this.context = context;
        this.archiveId = archiveId;
        this.multipleCopies = multipleCopies;
        this.location = location;
        this.archiveStore = archiveStore;
        this.chunkNumber = chunkNumber;
        this.doVerification = doVerification;
        this.ivs = ivs;
        this.encChunksHash = encChunksHash;
    }

    @Override
    public Object call() throws Exception {
        // if less that max threads started, start new one
        //File chunkFile = chunkFiles[i];
        //String chunkHash = chunksHash[i];
        // Delete the existing temporary file
        chunkFile.delete();
        String archiveChunkId = archiveId+FileSplitter.CHUNK_SEPARATOR+chunkNumber;
        // Copy file back from the archive storage
        log.debug("archiveChunkId: "+archiveChunkId);
        if (multipleCopies && location != null) {
            CopyBackFromArchive.copyBackFromArchive(archiveStore, archiveChunkId, chunkFile, location);
        } else {
            CopyBackFromArchive.copyBackFromArchive(archiveStore, archiveChunkId, chunkFile);
        }

        // Decryption
        if(ivs != null) {

            if(doVerification) {
                Encryption.decryptFile(context, chunkFile, ivs.get(chunkNumber));
            } else {
                String encChunkHash = encChunksHash[chunkNumber-1];

                // Check hash of encrypted file
                log.debug("Verifying encrypted chunk file: "+chunkFile.getAbsolutePath());
                verifyChunkFile(context.getTempDir(), chunkFile, encChunkHash);
            }
        }

        //logger.debug("Verifying chunk file: "+chunkFile.getAbsolutePath());
        //verifyChunkFile(context.getTempDir(), chunkFile, chunkHash);
        log.debug("Chunk download task completed: " + chunkNumber);
        return null;
    }

    private void verifyChunkFile(Path tempPath, File chunkFile, String origChunkHash) throws Exception {
        Utils.checkFileHash("chunk-file", chunkFile, origChunkHash);
    }

    public File getChunkFile() {
        return chunkFile;
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

    public ArchiveStore getArchiveStore() {
        return archiveStore;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public Boolean getDoVerification() {
        return doVerification;
    }

    public Map<Integer, byte[]> getIvs() {
        return ivs;
    }

    public String[] getEncChunksHash() {
        return encChunksHash;
    }

}
