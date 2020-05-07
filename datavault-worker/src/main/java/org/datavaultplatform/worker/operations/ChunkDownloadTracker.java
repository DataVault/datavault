package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ChunkDownloadTracker implements Callable {

    private File chunkFile;
    private String chunkHash;
    private Context context;
    private String archiveId;
    private Boolean multipleCopies;
    private String location;
    private ArchiveStore archiveStore;
    private int count;
    private Boolean doVerification;
    private Map<Integer, byte[]> ivs;
    private  String[] encChunksHash;
    private static final Logger logger = LoggerFactory.getLogger(ChunkDownloadTracker.class);

    @Override
    public Object call() throws Exception {
        // if less that max threads started start new one
        //File chunkFile = chunkFiles[i];
        //String chunkHash = chunksHash[i];
        // Delete the existing temporary file
        int chunkNum = count;
        chunkNum++;
        chunkFile.delete();
        String archiveChunkId = archiveId+FileSplitter.CHUNK_SEPARATOR+(chunkNum);
        // Copy file back from the archive storage
        logger.debug("archiveChunkId: "+archiveChunkId);
        if (multipleCopies && location != null) {
            CopyBackFromArchive.copyBackFromArchive(archiveStore, archiveChunkId, chunkFile, location);
        } else {
            CopyBackFromArchive.copyBackFromArchive(archiveStore, archiveChunkId, chunkFile);
        }

        // Decryption
        if(ivs != null) {

            if(doVerification) {
                Encryption.decryptFile(context, chunkFile, ivs.get(chunkNum));
            } else {
                String encChunkHash = encChunksHash[count];

                // Check hash of encrypted file
                logger.debug("Verifying encrypted chunk file: "+chunkFile.getAbsolutePath());
                verifyChunkFile(context.getTempDir(), chunkFile, encChunkHash);
            }
        }

        //logger.debug("Verifying chunk file: "+chunkFile.getAbsolutePath());
        //verifyChunkFile(context.getTempDir(), chunkFile, chunkHash);
        logger.debug("Chunk download thread completed: " + chunkNum);
        return null;
    }

    private void verifyChunkFile(Path tempPath, File chunkFile, String origChunkHash) throws Exception {

        if (origChunkHash != null) {
            logger.info("Get Digest from: " + chunkFile.getAbsolutePath());
            // Compare the SHA hash
            String chunkHash = Verify.getDigest(chunkFile);
            logger.info("Checksum: " + chunkHash);
            if (!chunkHash.equals(origChunkHash)) {
                throw new Exception("checksum failed: " + chunkHash + " != " + origChunkHash);
            }
        }
    }

    public File getChunkFile() {
        return chunkFile;
    }

    public void setChunkFile(File chunkFile) {
        this.chunkFile = chunkFile;
    }

    public String getChunkHash() {
        return chunkHash;
    }

    public void setChunkHash(String chunkHash) {
        this.chunkHash = chunkHash;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public Boolean getMultipleCopies() {
        return multipleCopies;
    }

    public void setMultipleCopies(Boolean multipleCopies) {
        this.multipleCopies = multipleCopies;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArchiveStore getArchiveStore() {
        return archiveStore;
    }

    public void setArchiveStore(ArchiveStore archiveStore) {
        this.archiveStore = archiveStore;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Boolean getDoVerification() {
        return doVerification;
    }

    public void setDoVerification(Boolean doVerification) {
        this.doVerification = doVerification;
    }

    public Map<Integer, byte[]> getIvs() {
        return ivs;
    }

    public void setIvs(Map<Integer, byte[]> ivs) {
        this.ivs = ivs;
    }

    public String[] getEncChunksHash() {
        return encChunksHash;
    }

    public void setEncChunksHash(String[] encChunksHash) {
        this.encChunksHash = encChunksHash;
    }
}
