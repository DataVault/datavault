package org.datavaultplatform.broker.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.task.Task;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

public class AuditDepositsChunks {

    private ArchiveStoreService archiveStoreService;
    private DepositsService depositsService;
    private EmailService emailService;
    private JobsService jobsService;
    private AuditsService auditsService;
    private Sender sender;

    private String optionsDir;
    private String tempDir;
    private String bucketName;
    private String region;
    private String awsAccessKey;
    private String awsSecretKey;

    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
    public void setArchiveStoreService(ArchiveStoreService archiveStoreService) { this.archiveStoreService = archiveStoreService; }
    public void setJobsService(JobsService jobsService) {
        this.jobsService = jobsService;
    }
    public void setAuditsService(AuditsService auditsService) { this.auditsService = auditsService; }
    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
    public void setOptionsDir(String optionsDir) {
        this.optionsDir = optionsDir;
    }
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    public void setRegion(String region) {
        this.region = region;
    }
    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }
    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    public void setSender(Sender sender) { this.sender = sender; }

    @Scheduled(cron = "${cron.expression}")
    public void execute() throws Exception {
        Date now = new Date();
        System.out.println("Start Audit Job "+now.toString());

        // TODO: Make sure this is not a Deposit that is still running
        List<DepositChunk> chunks = depositsService.getChunksForAudit();

        System.out.println("auditing "+chunks.size()+" chunks");

        if(chunks.size() > 0) {
            // Fetch the ArchiveStore that is flagged for retrieval. We store it in a list as the Task parameters require a list.
            ArchiveStore archiveStore = archiveStoreService.getForRetrieval();
            List<ArchiveStore> archiveStores = new ArrayList<ArchiveStore>();
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
                HashMap<Integer, String> chunksDigest = new HashMap<Integer, String>();
                for (int i = 0; i < chunks.size(); i++) {
                    DepositChunk chunk = chunks.get(i);
                    chunksDigest.put(i, chunk.getArchiveDigest());
                }

                // Get encryption IVs
                HashMap<Integer, byte[]> chunksIVs = new HashMap<Integer, byte[]>();
                for (int i = 0; i < chunks.size(); i++) {
                    DepositChunk chunk = chunks.get(i);
                    chunksIVs.put(i, chunk.getEncIV());
                }

                // Get encrypted digests
                HashMap<Integer, String> encChunksDigests = new HashMap<Integer, String>();
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

                List<HashMap<String, String>> chunksInfo = new ArrayList<HashMap<String, String>>();
                String[] archiveIds = new String[chunks.size()];
                for (int i = 0; i < chunks.size(); i++) {
                    DepositChunk chunk = chunks.get(i);
                    HashMap<String, String> info = new HashMap<String, String>();
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
