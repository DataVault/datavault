package org.datavaultplatform.worker.tasks;

import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.audit.AuditComplete;
import org.datavaultplatform.common.event.audit.AuditStart;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.worker.operations.SingleChunkAuditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class that extends Task which is used to handle Audits from the vault.
 * The Audit Task specifies a set of 'chunks' to audit - these chunks may be from different deposits.
 * A successful audit is where we can retrieve the specified chunks from the Vault Device.
 * If the Vault Device supports multiple locations, then the chunk will be retrieved from each location.
 */
public class Audit extends Task {

    private static final ArrayList<String> INITIAL_STATES;

    static {
        INITIAL_STATES = new ArrayList<>();
        INITIAL_STATES.add("Audit Data");    // 0
        INITIAL_STATES.add("Data Audit complete");  // 1
    }

    private static final Logger logger = LoggerFactory.getLogger(Audit.class);
    private String[] archiveIds;
    private String auditId;
    private Map<Integer, String> chunksDigest;
    private Map<Integer, String> encChunksDigest;
    private EventSender eventSender;
    private List<HashMap<String, String>> depositChunksToAudit;

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
        logger.info("Audit job - performAction()");
        Map<String, String> properties = getProperties();
        String userID = properties.get(PropNames.USER_ID);
        this.auditId = properties.get(PropNames.AUDIT_ID);
        this.chunksDigest = this.getChunkFilesDigest();
        this.encChunksDigest = this.getEncChunksDigest();

        this.depositChunksToAudit = this.getChunksToAudit();
        this.archiveIds = this.getArchiveIds();

        if (this.isRedeliver()) {
            eventSender.send(new Error(this.jobID, this.auditId,
                    "Audit stopped: the message had been redelivered, please investigate",
                    Error.Type.AUDIT));
            return;
        }

        this.initStates();

        logger.info("Audit id: {}", this.auditId);
        eventSender.send(new AuditStart(this.jobID, this.auditId)
                .withNextState(0));

        Device archiveFs = this.setupArchiveFileStores(context.getStorageClassNameResolver());

        try {
            eventSender.send(new UpdateProgress(this.jobID, null, 0,
                    this.depositChunksToAudit.size(), "Starting auditing ...")
                    .withUserId(userID));


            doAudit(context, archiveFs);

        } catch (RuntimeException ex) {
            String msg = "Audit failed: " + ex.getMessage();
            logger.error(msg, ex);
            eventSender.send(new Error(jobID, auditId, msg));
            throw ex;
        }
    }

    /**
     * For every chunk that we are asked to Audit.
     * If the device supports multipleLocations, check that we can retrieve the chunk from each location
     * @param context the context object
     * @param archiveFS the archive storage
     */
    private void doAudit(Context context, Device archiveFS) {
        boolean singleCopy = !archiveFS.hasMultipleCopies();
        List<String> locations = archiveFS.getLocations();
        if (singleCopy) {
            Assert.isNull(locations, "The locations should be null if the audit is singleCopy");
        } else {
            Assert.notNull(locations, "The locations should NOT be null if the audit is not singleCopy");
            boolean hasDuplicates = new HashSet<>(locations).size() < locations.size();
            if (hasDuplicates) {
                throw new IllegalArgumentException(String.format("The locations %s contains duplicates.",locations));
            }
        }
        try {

            String auditFailedMessage = getAuditFailedMessage(singleCopy, locations);

            int noOfThreads = context.getNoChunkThreads();
            logger.debug("Number of threads: {}", noOfThreads);
            TaskExecutor<Boolean> executor = createTaskExecutor(noOfThreads, auditFailedMessage);

            int totalNumberOfChunksPerLocation = this.depositChunksToAudit.size();
            int expectedNumberOfChunks = locations == null ? totalNumberOfChunksPerLocation : totalNumberOfChunksPerLocation * locations.size();
            logger.info("Retrieving {} chunk(s) for audit.", expectedNumberOfChunks);

            AtomicInteger auditedChunks = new AtomicInteger(0);
            List<String> auditLocations = getAuditLocations(locations);
            for (int chunkIdx = 0; chunkIdx < totalNumberOfChunksPerLocation; chunkIdx++) {
                HashMap<String, String> depositChunkToAudit = this.depositChunksToAudit.get(chunkIdx);
                String baseChunkArchiveId = this.archiveIds[chunkIdx];
                String decryptedChunkDigest = this.chunksDigest.get(chunkIdx);
                String encryptedChunkDigest = this.encChunksDigest.get(chunkIdx);
                byte[] chunkIV = this.chunksIVs.get(chunkIdx);
                for (String auditLocation : auditLocations) {
                    SingleChunkAuditor auditor = new SingleChunkAuditor(context, eventSender, archiveFS, jobID,
                            auditId, baseChunkArchiveId, chunkIdx, encryptedChunkDigest,
                            decryptedChunkDigest, chunkIV, depositChunkToAudit, singleCopy,
                            auditLocation, totalNumberOfChunksPerLocation);

                    executor.add(auditor);
                }
            }
            executor.execute(chunkAuditResult -> {
                if (chunkAuditResult) {
                    auditedChunks.incrementAndGet();
                }
            });
            if (auditedChunks.get() == expectedNumberOfChunks) {
                eventSender.send(new AuditComplete(this.jobID, this.auditId).withNextState(2));
            } else {
                String msg = String.format("Audit - managed to successfully audit [%s/%s] chunks", auditedChunks.get(), expectedNumberOfChunks);
                eventSender.send(new Error(this.jobID, this.auditId, msg, Error.Type.AUDIT));
            }
        } catch (Exception ex) {
            String msg = String.format("Audit - unexpected exception class[%s]message[%s]", ex.getClass(), ex.getMessage());
            eventSender.send(new Error(this.jobID, this.auditId, msg, Error.Type.AUDIT));
            logger.error("unexpected error during retrieve ", ex);
            if(ex instanceof  RuntimeException){
                throw (RuntimeException) ex;
            } else {
                throw new RuntimeException(ex);
            }
        }
    }
    private static List<String> getAuditLocations(List<String> locations){
        ArrayList<String> auditLocations = new ArrayList<>();
        if (locations == null) {
            auditLocations.add(null);
        } else {
            auditLocations.addAll(locations);
        }
        return auditLocations;
    }

    private String getAuditFailedMessage(boolean singleCopy, List<String> locations) {
        String base = String.format("Audit Failed : AuditId[%s] : ", auditId);
        if (singleCopy) {
            return base + "Single Location";
        } else {
            return base + String.format("Locations %s", locations);
        }
    }

    //By putting creation of TaskExecutor into a method - we can override it in AuditTest
    protected TaskExecutor<Boolean> createTaskExecutor(int numOfThreads, String auditFailedMessage) {
        return new TaskExecutor<>(numOfThreads, auditFailedMessage);
    }

    private void initStates() {
        eventSender.send(new InitStates(this.jobID, null, INITIAL_STATES));
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private Device setupArchiveFileStores(StorageClassNameResolver resolver) {
        try {
            Assert.isTrue(archiveFileStores.size() == 1, "Expected a single ArchiveFileStore got " + archiveFileStores.size());
            ArchiveStore archiveFileStore = archiveFileStores.get(0);
            Device archiveFs = StorageClassUtils.createStorage(archiveFileStore.getStorageClass(), archiveFileStore.getProperties(), Device.class, resolver);
            return archiveFs;
        } catch (RuntimeException ex) {
            String msg = "Audit failed: could not access archive filesystem";
            logger.error(msg, ex);
            eventSender.send(new Error(this.jobID, this.auditId, msg, Error.Type.AUDIT));
            throw ex;
        }
    }
}