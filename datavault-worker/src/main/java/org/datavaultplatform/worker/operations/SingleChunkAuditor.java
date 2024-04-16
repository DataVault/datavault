package org.datavaultplatform.worker.operations;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.audit.AuditError;
import org.datavaultplatform.common.event.audit.ChunkAuditComplete;
import org.datavaultplatform.common.event.audit.ChunkAuditStarted;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
@Data
public class SingleChunkAuditor implements Callable<Boolean> {
    static final String PROP_ID = "id";
    static final String PROP_CHUNK_NUM = "chunkNum";
    private final String encryptedChunkDigest;
    private final String decryptedChunkDigest;
    private final byte[] chunkIV;
    private final String jobID;
    private final String auditId;
    private final EventSender eventSender;
    private final Device archiveFS;
    private final int chunkIdx;
    private final Context context;

    private final boolean singleCopy;
    private final String location;
    private final String chunkId;
    private final int totalNumberOfChunks;
    private final int chunkNum;
    private final String chunkArchiveId;
    private final Path chunkPath;

    public SingleChunkAuditor(
            Context context,
            EventSender eventSender, Device archiveFS,

            String jobID, String auditId,
            String baseChunkArchiveId,
            int chunkIdx,

            String encryptedChunkDigest,
            String decryptedChunkDigest,
            byte[] chunkIV, Map<String, String> depositChunkProperties, boolean singleCopy, String location, int totalNumberOfChunks) {

        this.context = context;
        this.eventSender = eventSender;
        this.archiveFS = archiveFS;

        this.jobID = jobID;
        this.auditId = auditId;
        this.chunkIdx = chunkIdx;

        this.encryptedChunkDigest = encryptedChunkDigest;
        this.decryptedChunkDigest = decryptedChunkDigest;
        this.chunkIV = chunkIV;

        this.singleCopy = singleCopy;
        this.location = location;

        this.chunkId = depositChunkProperties.get(PROP_ID);
        this.chunkNum = Integer.parseInt(depositChunkProperties.get(PROP_CHUNK_NUM));

        this.totalNumberOfChunks = totalNumberOfChunks;
        this.chunkArchiveId = baseChunkArchiveId + FileSplitter.CHUNK_SEPARATOR + chunkNum;
        if (singleCopy) {
            this.chunkPath = context.getTempDir().resolve(chunkArchiveId);
        } else {
            this.chunkPath = context.getTempDir().resolve(location).resolve(chunkArchiveId);
        }
    }

    @Override
    public Boolean call() {
        try {
            sendChunkAuditStarted();

            Files.deleteIfExists(chunkPath);
            File chunkFile = chunkPath.toFile();

            if (singleCopy) {
                log.info("Retrieving singleCopy: {}", chunkFile.getAbsolutePath());
                archiveFS.retrieve(chunkArchiveId, chunkFile, null);
            } else {
                log.info("Retrieving: {} from location[{}]", chunkFile.getAbsolutePath(), location);
                archiveFS.retrieve(chunkArchiveId, chunkFile, null, location);
            }

            sendProgress((chunkIdx + 1) + "chunk(s) retrieved out of " + totalNumberOfChunks + "...");

            if (chunkIV != null) {

                // Check encrypted file checksum
                String encChunkFileHash = Verify.getDigest(chunkFile);

                log.info("Encrypted Checksum: {}", encChunkFileHash);

                if (!encChunkFileHash.equals(encryptedChunkDigest)) {
                    sendAuditError("Encrypted checksum failed: " + encChunkFileHash + " != " + encryptedChunkDigest);
                    return false;
                }
                Encryption.decryptFile(context, chunkFile, chunkIV);
            }


            // TODO: Should we check algorithm each time or assume main tar file algorithm is the same
            // We might also want to move algorithm check before this loop
            String chunkFileHash = Verify.getDigest(chunkFile);

            log.info("Checksum: {}", chunkFileHash);

            if (!chunkFileHash.equals(this.decryptedChunkDigest)) {
                sendAuditError("Decrypted checksum failed: " + chunkFileHash + " != " + decryptedChunkDigest);
                return false;
            }

            sendAuditChunkComplete();

            return true;

        } catch (Exception ex) {
            log.error("Audit : unexpected exception ", ex);
            sendAuditError("Audit of chunk failed with Exception: " + ex);
            return false;
        } finally {
            try {
                Files.deleteIfExists(chunkPath);
            } catch (IOException ex) {
                log.debug("Failed to delete chunk file {}", chunkPath, ex);
            }
        }
    }

    private void sendAuditError(String message) {
        eventSender.send(new AuditError(jobID, auditId, chunkId, chunkArchiveId, location, message));
    }

    void sendProgress(String message) {
        eventSender.send(new UpdateProgress(jobID, null, chunkIdx + 1, this.totalNumberOfChunks, message));
    }

    private void sendAuditChunkComplete() {
        eventSender.send(new ChunkAuditComplete(jobID, auditId, chunkId, chunkArchiveId, location));
    }

    private void sendChunkAuditStarted() {
        log.info("Sending ChunkAuditStarted event...");
        eventSender.send(new ChunkAuditStarted(jobID, auditId, chunkId, chunkArchiveId, location));
    }
}
