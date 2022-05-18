package org.datavaultplatform.broker.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.broker.services.AuditsService;
import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.EmailService;
import org.datavaultplatform.broker.services.JobsService;
import org.datavaultplatform.common.model.Archive;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuditDepositsChunks implements ScheduledTask {

    private final ArchiveStoreService archiveStoreService;
    private final DepositsService depositsService;
    private final EmailService emailService;
    private final JobsService jobsService;
    private final AuditsService auditsService;
    private final Sender sender;

    private final String optionsDir;
    private final String tempDir;
    private final String bucketName;
    private final String region;
    private final String awsAccessKey;
    private final String awsSecretKey;
    private final String tsmRetryTime;
    private final String occRetryTime;
    private final String tsmMaxRetries;
    private final String occMaxRetries;
    private final String ociNameSpace;
    private final String ociBucketName;

    /*
        <property name="optionsDir" value="${optionsDir:#{null}}" />
        <property name="tsmRetryTime" value="${tsmRetryTime:#{null}}" />
        <property name="tsmMaxRetries" value="${tsmMaxRetries:#{null}}" />
        <property name="occRetryTime" value="${occRetryTime:#{null}}" />
        <property name="occMaxRetries" value="${occMaxRetries:#{null}}" />
        <property name="ociNameSpace" value="${ociNameSpace:#{null}}" />
        <property name="ociBucketName" value="${ociBucketName:#{null}}" />
        <property name="tempDir" value="${tempDir:#{null}}" />
        <property name="bucketName" value="${s3.bucketName:#{null}}" />
        <property name="region" value="${s3.region:#{null}}" />
        <property name="awsAccessKey" value="${s3.awsAccessKey:#{null}}" />
        <property name="awsSecretKey" value="${s3.awsSecretKey:#{null}}" />
     */
    @Autowired
    public AuditDepositsChunks(ArchiveStoreService archiveStoreService,
        DepositsService depositsService, EmailService emailService, JobsService jobsService,
        AuditsService auditsService, Sender sender,
        @Value("${optionsDir:#{null}}") String optionsDir,
        @Value("${tempDir:#{null}}") String tempDir,
        @Value("${s3.bucketName:#{null}}") String bucketName,
        @Value("${s3.region:#{null}}") String region,
        @Value("${s3.awsAccessKey:#{null}}") String awsAccessKey,
        @Value("${s3.awsSecretKey:#{null}}") String awsSecretKey,
        @Value("${tsmRetryTime:#{null}}") String tsmRetryTime,
        @Value("${occRetryTime:#{null}}") String occRetryTime,
        @Value("${tsmMaxRetries:#{null}}") String tsmMaxRetries,
        @Value("${occMaxRetries:#{null}}") String occMaxRetries,
        @Value("${ociNameSpace:#{null}}") String ociNameSpace,
        @Value("${ociBucketName:#{null}}") String ociBucketName) {
        this.archiveStoreService = archiveStoreService;
        this.depositsService = depositsService;
        this.emailService = emailService;
        this.jobsService = jobsService;
        this.auditsService = auditsService;
        this.sender = sender;
        this.optionsDir = optionsDir;
        this.tempDir = tempDir;
        this.bucketName = bucketName;
        this.region = region;
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        this.tsmRetryTime = tsmRetryTime;
        this.occRetryTime = occRetryTime;
        this.tsmMaxRetries = tsmMaxRetries;
        this.occMaxRetries = occMaxRetries;
        this.ociNameSpace = ociNameSpace;
        this.ociBucketName = ociBucketName;
    }


    @Scheduled(cron = ScheduledUtils.SCHEDULE_1_AUDIT_DEPOSIT)
    public void execute() {
        Date now = new Date();
        System.out.println("Start Audit Job "+ now);

        // TODO: Make sure this is not a Deposit that is still running
        List<DepositChunk> chunks = depositsService.getChunksForAudit();

        System.out.println("auditing "+chunks.size()+" chunks");

        if(chunks.size() > 0) {
            // Fetch the ArchiveStore that is flagged for retrieval. We store it in a list as the Task parameters require a list.
            ArchiveStore archiveStore = archiveStoreService.getForRetrieval();
            List<ArchiveStore> archiveStores = new ArrayList<>();
            archiveStores.add(archiveStore);
            archiveStores = this.addArchiveSpecificOptions(archiveStores);


            // Create a job to track this retrieve
            Job job = new Job("org.datavaultplatform.worker.tasks.Audit");
            jobsService.addJob(null, job);

            // Add the Audit object
            Audit audit = new Audit();
            auditsService.addAudit(audit, chunks);

            // Ask the worker to process the data auditing
            try {
                HashMap<String, String> auditProperties = new HashMap<>();
                auditProperties.put("auditId", audit.getID());
                auditProperties.put("numOfChunks", Integer.toString(chunks.size()));

                // get chunks checksums
                HashMap<Integer, String> chunksDigest = new HashMap<>();
                for (int i = 0; i < chunks.size(); i++) {
                    DepositChunk chunk = chunks.get(i);
                    chunksDigest.put(i, chunk.getArchiveDigest());
                }

                // Get encryption IVs
                HashMap<Integer, byte[]> chunksIVs = new HashMap<>();
                for (int i = 0; i < chunks.size(); i++) {
                    DepositChunk chunk = chunks.get(i);
                    chunksIVs.put(i, chunk.getEncIV());
                }

                // Get encrypted digests
                HashMap<Integer, String> encChunksDigests = new HashMap<>();
                for (int i = 0; i < chunks.size(); i++) {
                    DepositChunk chunk = chunks.get(i);
                    encChunksDigests.put(i, chunk.getEcnArchiveDigest());
                }

                Task auditTask = new Task(
                        job, auditProperties, archiveStores,
                        null, null,
                        null, null,
                        chunksDigest,
                        null, chunksIVs,
                        null, encChunksDigests, null);

                List<HashMap<String, String>> chunksInfo = new ArrayList<>();
                String[] archiveIds = new String[chunks.size()];
                for (int i = 0; i < chunks.size(); i++) {
                    DepositChunk chunk = chunks.get(i);
                    HashMap<String, String> info = new HashMap<>();
                    info.put("id", chunk.getID());
                    info.put("bagId", chunk.getDeposit().getBagId());
                    info.put("chunkNum", String.valueOf(chunk.getChunkNum()));
                    chunksInfo.add(info);

                    // Find the Archive that matches the ArchiveStore.
                    for (Archive archive : chunk.getDeposit().getArchives()) {
                        if (archive.getArchiveStore().getID().equals(archiveStore.getID())) {
                            archiveIds[i] = archive.getArchiveId();
                        }
                    }
                }

                auditTask.setChunksToAudit(chunksInfo);
                auditTask.setArchiveIds(archiveIds);

                ObjectMapper mapper = new ObjectMapper();
                String jsonAudit = mapper.writeValueAsString(auditTask);
                sender.send(jsonAudit);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Audit Job Started...");
        } else {
            System.out.println("No chunk to audit...");
        }
    }

    private List<ArchiveStore> addArchiveSpecificOptions(List<ArchiveStore> archiveStores) {
        if (archiveStores != null && ! archiveStores.isEmpty()) {
            for (ArchiveStore archiveStore : archiveStores) {
                if (archiveStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.TivoliStorageManager")) {
                    HashMap<String, String> asProps = archiveStore.getProperties();
                    if (this.optionsDir != null && ! this.optionsDir.equals("")) {
                        asProps.put("optionsDir", this.optionsDir);
                    }
                    if (this.tempDir != null && ! this.tempDir.equals("")) {
                        asProps.put("tempDir", this.tempDir);
                    }
                    if (this.tsmRetryTime != null && ! this.tsmRetryTime.equals("")) {
                        asProps.put("tsmRetryTime", this.tsmRetryTime);
                    }
                    if (this.tsmMaxRetries != null && ! this.tsmMaxRetries.equals("")) {
                        asProps.put("tsmMaxRetries", this.tsmMaxRetries);
                    }
                    archiveStore.setProperties(asProps);
                }

                if (archiveStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.OracleObjectStorageClassic")) {
                    HashMap<String, String> asProps = archiveStore.getProperties();
                    if (this.occRetryTime != null && ! this.occRetryTime.equals("")) {
                        asProps.put("occRetryTime", this.occRetryTime);
                    }
                    if (this.occMaxRetries != null && ! this.occMaxRetries.equals("")) {
                        asProps.put("occMaxRetries", this.occMaxRetries);
                    }
                    if (this.ociBucketName != null && ! this.ociBucketName.equals("")) {
                        asProps.put("ociBucketName", this.ociBucketName);
                    }
                    if (this.ociNameSpace != null && ! this.ociNameSpace.equals("")) {
                        asProps.put("ociNameSpace", this.ociNameSpace);
                    }
                    archiveStore.setProperties(asProps);
                }

                if (archiveStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.S3Cloud")) {
                    HashMap<String, String> asProps = archiveStore.getProperties();
                    if (this.bucketName != null && ! this.bucketName.equals("")) {
                        asProps.put("s3.bucketName", this.bucketName);
                    }
                    if (this.region != null && ! this.region.equals("")) {
                        asProps.put("s3.region", this.region);
                    }
                    if (this.awsAccessKey != null && ! this.awsAccessKey.equals("")) {
                        asProps.put("s3.awsAccessKey", this.awsAccessKey);
                    }
                    if (this.awsSecretKey != null && ! this.awsSecretKey.equals("")) {
                        asProps.put("s3.awsSecretKey", this.awsSecretKey);
                    }

                    //if (this.authDir != null && ! this.authDir.equals("")) {
                    //	asProps.put("authDir", this.authDir);
                    //}
                    archiveStore.setProperties(asProps);
                }
            }
        }

        return archiveStores;
    }
}
