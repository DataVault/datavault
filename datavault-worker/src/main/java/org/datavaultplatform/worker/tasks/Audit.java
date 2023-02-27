package org.datavaultplatform.worker.tasks;

import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.audit.*;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.worker.operations.FileSplitter;
import org.datavaultplatform.common.event.EventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * A class that extends Task which is used to handle Retrievals from the vault
 */
public class Audit extends Task {
    
    private static final Logger logger = LoggerFactory.getLogger(Audit.class);
    private String[] archiveIds = null;
    private String auditId = null;
    private Map<Integer, String> chunksDigest = null;
    private Map<Integer, String> encChunksDigest = null;
    private EventSender eventSender = null;
    private List<HashMap<String, String>> depositChunkToAudit = null;

    /* (non-Javadoc)
     * @see org.datavaultplatform.common.task.Task#performAction(org.datavaultplatform.common.task.Context)
     * 
     * Get a list of Chunks to Audit from Broker
     * Retrieve each chunk and run checksum on encrypted and decrypted file.
     *  
     * @param context
     */
    @Override
    public void performAction(Context context) {

        this.eventSender = context.getEventSender();
        logger.info("Retrieve job - performAction()");
        Map<String, String> properties = getProperties();
        String userID = properties.get(PropNames.USER_ID);
        this.auditId = properties.get(PropNames.AUDIT_ID);
        this.chunksDigest = this.getChunkFilesDigest();
        this.encChunksDigest = this.getEncChunksDigest();

        this.depositChunkToAudit = this.getChunksToAudit();
        this.archiveIds = this.getArchiveIds();

        if (this.isRedeliver()) {
            eventSender.send(new Error(this.jobID, this.auditId,
                    "Audit stopped: the message had been redelivered, please investigate",
                    Error.Type.AUDIT));
            return;
        }
        
        this.initStates();

        logger.info("Audit id: "+this.auditId);
        eventSender.send(new AuditStart(this.jobID, this.auditId)
            .withNextState(0));

        Device archiveFs = this.setupArchiveFileStores(context.getStorageClassNameResolver());

        try {
            eventSender.send(new UpdateProgress(this.jobID, null, 0,
                    this.depositChunkToAudit.size(), "Starting auditing ...")
                    .withUserId(userID));

            if (archiveFs.hasMultipleCopies()) {
                this.multipleCopies(context, archiveFs);
            } else {
                this.doAudit(context, archiveFs, true, null);
            }
        } catch (Exception e) {
            String msg = "Data retrieve failed: " + e.getMessage();
            logger.error(msg, e);
            eventSender.send(new Error(jobID, auditId, msg));
            throw new RuntimeException(e);
        }
    }

    private void multipleCopies(Context context, Device archiveFs) {
        logger.info("Device has multiple copies");

        List<String> locations = archiveFs.getLocations();

        for (String location : locations) {
            logger.info("Current " + location);
            try {
                logger.info("Attempting audit on archive from " + location);
                this.doAudit(context, archiveFs, false, location);
                logger.info("Completed audit on archive from " + location);
            } catch (Exception e) {
                logger.info("Current " + location + " has a problem trying next location", e);
            }
        }
    }

    private void doAudit(Context context, Device archiveFs, boolean singleCopy, String location) {
        logger.info("Retrieving " + depositChunkToAudit.size() + " chunk(s) for audit.");

        for(int i = 0; i < depositChunkToAudit.size(); i++) {
            String chunkId = depositChunkToAudit.get(i).get("id");
            String tarFileName = depositChunkToAudit.get(i).get("bagId") + ".tar";
            int chunkNum = Integer.parseInt(depositChunkToAudit.get(i).get("chunkNum"));

            Path chunkPath = context.getTempDir().resolve(tarFileName + FileSplitter.CHUNK_SEPARATOR + chunkNum);
            File chunkFile = chunkPath.toFile();
            String chunkArchiveId = this.archiveIds[i]+FileSplitter.CHUNK_SEPARATOR+chunkNum;

            logger.info("Sending ChunkAuditStarted event...");
            eventSender.send(new ChunkAuditStarted(this.jobID, this.auditId, chunkId, chunkArchiveId, location));

            try {
                if (singleCopy) {
                    logger.info("Retrieving singleCopy: " + chunkFile.getAbsolutePath());
                    archiveFs.retrieve(chunkArchiveId, chunkFile, null);
                } else {
                    logger.info("Retrieving: " + chunkFile.getAbsolutePath());
                    archiveFs.retrieve(chunkArchiveId, chunkFile, null, location);
                }

                eventSender.send(new UpdateProgress(this.jobID, null, i, depositChunkToAudit.size(),
                        i + " chunk(s) retrieved out of " + depositChunkToAudit.size() + "..."));

                if (this.getChunksIVs().get(i) != null) {
                    String archivedEncChunkFileHash = this.encChunksDigest.get(i);

                    // Check encrypted file checksum
                    String encChunkFileHash = Verify.getDigest(chunkFile);

                    logger.info("Encrypted Checksum: " + encChunkFileHash);

                    if (!encChunkFileHash.equals(archivedEncChunkFileHash)) {

                        eventSender.send(new AuditError(this.jobID, this.auditId, chunkId, chunkArchiveId, location,
                                "Encrypted checksum failed: " + encChunkFileHash + " != " + archivedEncChunkFileHash));
                        continue;
                    }

                    Encryption.decryptFile(context, chunkFile, this.getChunksIVs().get(i));
                }

                // Check file
                String archivedChunkFileHash = this.chunksDigest.get(i);

                // TODO: Should we check algorithm each time or assume main tar file algorithm is the same
                // We might also want to move algorithm check before this loop
                String chunkFileHash = Verify.getDigest(chunkFile);

                logger.info("Checksum: " + chunkFileHash);

                if (!chunkFileHash.equals(archivedChunkFileHash)) {
                    eventSender.send(new AuditError(this.jobID, this.auditId, chunkId, chunkArchiveId, location,
                            "Decrypted checksum failed: " + chunkFileHash + " != " + archivedChunkFileHash));
                    //                throw new Exception("checksum failed: " + chunkFileHash + " != " + archivedChunkFileHash);
                }

                eventSender.send(new ChunkAuditComplete(this.jobID, this.auditId, chunkId, chunkArchiveId, location));

            } catch (Exception e){
                eventSender.send(new AuditError(this.jobID, this.auditId, chunkId, chunkArchiveId, location,
                        "Audit of chunk failed with Exception: " + e));
                continue;
            }

            chunkFile.delete();
        }

        eventSender.send(new AuditComplete(this.jobID, this.auditId).withNextState(2));
    }

    private void initStates() {
    	ArrayList<String> states = new ArrayList<>();
        states.add("Audit Data");    // 0
        states.add("Data Audit complete");  // 1
        eventSender.send(new InitStates(this.jobID, null, states));
	}
    
    private Device setupArchiveFileStores(StorageClassNameResolver resolver) {
    	// We get passed a list because it is a parameter common to deposits and retrieves, but for retrieve there should only be one.
        ArchiveStore archiveFileStore = archiveFileStores.get(0);

        // Connect to the archive storage
        try {
            Device archiveFs = StorageClassUtils.createStorage(archiveFileStore.getStorageClass(), archiveFileStore.getProperties(), Device.class, resolver);
            return archiveFs;
        } catch (Exception e) {
            String msg = "Retrieve failed: could not access archive filesystem";
            logger.error(msg, e);
            eventSender.send(new Error(this.jobID, this.auditId, msg, Error.Type.AUDIT));

            throw new RuntimeException(e);
        }
    }

}