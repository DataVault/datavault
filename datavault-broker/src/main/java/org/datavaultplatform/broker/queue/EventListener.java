package org.datavaultplatform.broker.queue;

import static org.datavaultplatform.broker.config.RabbitConfig.QUEUE_EVENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.audit.*;
import org.datavaultplatform.common.event.delete.DeleteComplete;
import org.datavaultplatform.common.event.delete.DeleteStart;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.common.model.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventListener implements MessageListener {

    private final JobsService jobsService;
    private final EventService eventService;
    private final VaultsService vaultsService;
    private final DepositsService depositsService;
    private final ArchiveStoreService archiveStoreService;
    private final ArchivesService archivesService;
    private final RetrievesService retrievesService;
    private UsersService usersService;
    private final EmailService emailService;
    private final AuditsService auditsService;
    private final String homeUrl;
	  private final String helpUrl;
	  private final String helpMail;
    private final String auditAdminEmail;
    private Deposit.Status preDeletionStatus;
    
	private static final Map<String, String> EMAIL_SUBJECTS;
    static {
    	EMAIL_SUBJECTS = new HashMap<String, String>();
    	EMAIL_SUBJECTS.put("user-deposit-start", "Confirmation - a new DataVault deposit is starting.");
    	EMAIL_SUBJECTS.put("user-deposit-complete", "Confirmation - a new DataVault deposit is complete.");
    	EMAIL_SUBJECTS.put("user-deposit-error", "DataVault ERROR - attempted deposit or retrieval of a deposit failed");
    	EMAIL_SUBJECTS.put("admin-deposit-start", "Confirmation - a new DataVault deposit is starting.");
    	EMAIL_SUBJECTS.put("admin-deposit-complete", "Confirmation - a new DataVault deposit is complete.");
    	EMAIL_SUBJECTS.put("admin-deposit-error", "DataVault ERROR - attempted deposit or retrieval of a deposit failed");
    	EMAIL_SUBJECTS.put("user-deposit-retrievestart", "Confirmation - a new DataVault retrieval is starting.");
    	EMAIL_SUBJECTS.put("user-deposit-retrievecomplete", "Confirmation - a new DataVault retrieval is complete.");
    	EMAIL_SUBJECTS.put("admin-deposit-retrievestart", "Confirmation - a new DataVault retrieval is starting.");
    	EMAIL_SUBJECTS.put("admin-deposit-retrievecomplete", "Confirmation - a new DataVault retrieval is complete.");
      EMAIL_SUBJECTS.put("audit-chunk-error", "DataVault ERROR - Chunk failed checksum during audit.");
    }

    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

    @Autowired
    public EventListener(
        JobsService jobsService,
        VaultsService vaultsService,
        DepositsService depositsService,
        ArchiveStoreService archiveStoreService,
        ArchivesService archivesService,
        RetrievesService retrievesService,
        EventService eventService,
        UsersService usersService,
        EmailService emailService,
        AuditsService auditsService,
        @Value("${home.page}") String homeUrl,
        @Value("${help.page}") String helpUrl,
        @Value("${help.mail}") String helpMail,
        @Value("${audit.adminEmail}") String auditAdminEmail) {
        this.jobsService = jobsService;
        this.vaultsService = vaultsService;
        this.depositsService = depositsService;
        this.archiveStoreService = archiveStoreService;
        this.archivesService = archivesService;
        this.retrievesService = retrievesService;
        this.eventService = eventService;
        this.usersService = usersService;
        this.emailService = emailService;
        this.auditsService = auditsService;
        this.homeUrl = homeUrl;
        this.helpUrl = helpUrl;
        this.helpMail = helpMail;
        this.auditAdminEmail = auditAdminEmail;
    }


    @Override
    @RabbitListener(queues = QUEUE_EVENT)
    public void onMessage(Message msg) {
        
        String messageBody = new String(msg.getBody(), StandardCharsets.UTF_8);
        logger.info("Received '" + messageBody + "'");

        try {
            // Decode the event
            ObjectMapper mapper = new ObjectMapper();
            Event commonEvent = mapper.readValue(messageBody, Event.class);

            Class<?> clazz = Class.forName(commonEvent.getEventClass());
            Event concreteEvent = (Event)(mapper.readValue(messageBody, clazz));
            
            // Get the related deposit
            Deposit deposit = depositsService.getDeposit(concreteEvent.getDepositId());
            concreteEvent.setDeposit(deposit);

            // Get the related job
            Job job = jobsService.getJob(concreteEvent.getJobId());
            concreteEvent.setJob(job);
            
            // Get the related User
            User user = usersService.getUser(concreteEvent.getUserId());
            concreteEvent.setUser(user);
            
            if (concreteEvent.getPersistent()) {
                // Persist the event properties in the database ...
                eventService.addEvent(concreteEvent);
            }
            
            // Update with next state information (if present)
            if (concreteEvent.nextState != null) {
                
                Boolean success = false;
                while (!success) {
                    try {
                        if (job.getState() == null || (job.getState() < concreteEvent.nextState)) {
                            job.setState(concreteEvent.nextState);
                            jobsService.updateJob(job);
                        }
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        job = jobsService.getJob(concreteEvent.getJobId());
                    }
                }
            }
            
            // Maybe perform an action based on the event type ...
            
            if (concreteEvent instanceof InitStates) {
                
                // Update the Job state information
                InitStates initStatesEvent = (InitStates)concreteEvent;
                
                Boolean success = false;
                while (!success) {
                    try {
                        job.setStates(initStatesEvent.getStates());
                        jobsService.updateJob(job);
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        job = jobsService.getJob(concreteEvent.getJobId());
                    }
                }
            
            } else if (concreteEvent instanceof UpdateProgress) {
                
                // Update the Job state
                UpdateProgress updateStateEvent = (UpdateProgress)concreteEvent;
                
                Boolean success = false;
                while (!success) {
                    try {
                        job.setProgress(updateStateEvent.getProgress());
                        job.setProgressMax(updateStateEvent.getProgressMax());
                        job.setProgressMessage(updateStateEvent.getProgressMessage());
                        jobsService.updateJob(job);
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        job = jobsService.getJob(concreteEvent.getJobId());
                    }
                }
                
            } else if (concreteEvent instanceof Start) {
                
                // Update the deposit status
                
                Boolean success = false;
                while (!success) {
                    try {
                        if (deposit.getStatus() == null || (deposit.getStatus() != Deposit.Status.COMPLETE)) {
                            deposit.setStatus(Deposit.Status.IN_PROGRESS);
                            depositsService.updateDeposit(deposit);
                        }
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        deposit = depositsService.getDeposit(concreteEvent.getDepositId());
                    }
                }
                
//              // Get related information for emails
              String type = "start";
              this.sendEmails(deposit, concreteEvent, type, EmailTemplate.USER_DEPOSIT_START, EmailTemplate.GROUP_ADMIN_DEPOSIT_START);
                
            } else if (concreteEvent instanceof ComputedSize) {
                
                // Update the deposit with the computed size
                ComputedSize computedSizeEvent = (ComputedSize)concreteEvent;
                
                Boolean success = false;
                while (!success) {
                    try {
                        logger.info("Setting Deposit Size from computedSizeEvent to: " + computedSizeEvent.getBytes());
                        deposit.setSize(computedSizeEvent.getBytes());
                        depositsService.updateDeposit(deposit);
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        logger.info("Refresh from DB and retry");
                        deposit = depositsService.getDeposit(concreteEvent.getDepositId());
                    }
                }

                /*// Add to the cumulative vault size
                Vault vault = deposit.getVault();
                
                success = false;
                while (!success) {
                    try {
                        long vaultSize = vault.getSize();
                        vault.setSize(vaultSize + computedSizeEvent.getBytes());
                        vaultsService.updateVault(vault);
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        vault = vaultsService.getVault(vault.getID());
                    }
                }*/

            } else if (concreteEvent instanceof ComputedChunks) {
                
                // Update the deposit with the computed digest
                ComputedChunks computedChunksEvent = (ComputedChunks)concreteEvent;
                
                Boolean success = false;
                while (!success) {
                    try {
                        HashMap<Integer, String> chunksDigest = computedChunksEvent.getChunksDigest();
                        String algorithm = computedChunksEvent.getDigestAlgorithm();
                        deposit.setNumOfChunks(chunksDigest.size());
                        depositsService.updateDeposit(deposit);
                        
                        deposit.setDepositChunks(new ArrayList<DepositChunk>());
                        
                        for(Integer chunkNum : chunksDigest.keySet()) {
                            String hash = chunksDigest.get(chunkNum);
                            // Create new Deposit chunk
                            DepositChunk depositChunk = new DepositChunk(deposit, chunkNum, hash, algorithm);
                            deposit.getDepositChunks().add(depositChunk);
                        }
                        depositsService.updateDeposit(deposit);
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        deposit = depositsService.getDeposit(concreteEvent.getDepositId());
                    }
                }
                
            } else if (concreteEvent instanceof ComputedEncryption) {
                
                // Update the deposit with the computed digest
                ComputedEncryption computedEncryptionEvent = (ComputedEncryption)concreteEvent;
                
                Boolean success = false;
                while (!success) {
                    try {
                        // If went through chunking will also get the chuncking information
                        HashMap<Integer, String> chunksDigest = computedEncryptionEvent.getChunksDigest();
                        String algorithm = computedEncryptionEvent.getDigestAlgorithm();
                        deposit.setNumOfChunks(chunksDigest.size());
                        depositsService.updateDeposit(deposit);

                        deposit.setDepositChunks(new ArrayList<DepositChunk>());

                        for(Integer chunkNum : chunksDigest.keySet()) {
                            String hash = chunksDigest.get(chunkNum);
                            // Create new Deposit chunk
                            DepositChunk depositChunk = new DepositChunk(deposit, chunkNum, hash, algorithm);
                            deposit.getDepositChunks().add(depositChunk);
                        }

                        HashMap<Integer, byte[]> chunksIVs = computedEncryptionEvent.getChunkIVs();
                        HashMap<Integer, String> encChunksDigests = computedEncryptionEvent.getEncChunkDigests();
                        byte[] tarIV = computedEncryptionEvent.getTarIV();
                        String encTarDigest = computedEncryptionEvent.getEncTarDigest();
                        String aesMode = computedEncryptionEvent.getAesMode();

                        List<DepositChunk> chunks = deposit.getDepositChunks();
                        for(DepositChunk chunk : chunks) {
                            Integer num = chunk.getChunkNum();
                            
                            chunk.setEncIV(chunksIVs.get(num));
                            chunk.setEcnArchiveDigest(encChunksDigests.get(num));
                        }
                        
                        // TODO: we might want to only update this if it's needed
                        deposit.setEncIV(tarIV);
                        deposit.setEncArchiveDigest(encTarDigest);
                        
                        depositsService.updateDeposit(deposit);
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        deposit = depositsService.getDeposit(concreteEvent.getDepositId());
                    }
                }
                
            } else if (concreteEvent instanceof ComputedDigest) {
                
                // Update the deposit with the computed digest
                ComputedDigest computedDigestEvent = (ComputedDigest)concreteEvent;
                
                Boolean success = false;
                while (!success) {
                    try {
                        deposit.setArchiveDigest(computedDigestEvent.getDigest());
                        deposit.setArchiveDigestAlgorithm(computedDigestEvent.getDigestAlgorithm());
                        depositsService.updateDeposit(deposit);
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        deposit = depositsService.getDeposit(concreteEvent.getDepositId());
                    }
                }
                
            } else if (concreteEvent instanceof UploadComplete) {

                // Update the deposit status and add archives
                UploadComplete uploadCompleteEvent = (UploadComplete)concreteEvent;

                Boolean success = false;
                while (!success) {
                    try {
                        //deposit.setStatus(Deposit.Status.COMPLETE);
                        //deposit.setArchiveSize(completeEvent.getArchiveSize());
                        //depositsService.updateDeposit(deposit);

                        // Add the archive objects, one for each archiveStore
                        for (String archiveStoreId : uploadCompleteEvent.getArchiveIds().keySet()) {
                            ArchiveStore archiveStore = archiveStoreService.getArchiveStore(archiveStoreId);
                            archivesService.addArchive(deposit, archiveStore, uploadCompleteEvent.getArchiveIds().get(archiveStoreId));
                        }

                        // Add to the cumulative vault size
                        //Vault vault = deposit.getVault();

                        /*success = false;
                        while (!success) {
                            try {
                                long vaultSize = vault.getSize();
                                vault.setSize(vaultSize + deposit.getSize());
                                vaultsService.updateVault(vault);
                                success = true;
                            } catch (org.hibernate.StaleObjectStateException e) {
                                // Refresh from database and retry
                                vault = vaultsService.getVault(vault.getID());
                            }
                        }*/
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        deposit = depositsService.getDeposit(concreteEvent.getDepositId());
                    }
                }

                // Get related information for emails
                //String type = "complete";
                //this.sendEmails(deposit, completeEvent, type, EmailTemplate.USER_DEPOSIT_COMPLETE, EmailTemplate.GROUP_ADMIN_DEPOSIT_COMPLETE);

            } else if (concreteEvent instanceof Complete) {

                // Update the deposit status and add archives
                Complete completeEvent = (Complete)concreteEvent;
                
                Boolean success = false;
                while (!success) {
                    try {
                        deposit.setStatus(Deposit.Status.COMPLETE);
                        deposit.setArchiveSize(completeEvent.getArchiveSize());
                        depositsService.updateDeposit(deposit);

                        // Add the archive objects, one for each archiveStore
                        //for (String archiveStoreId : completeEvent.getArchiveIds().keySet()) {
                        //    ArchiveStore archiveStore = archiveStoreService.getArchiveStore(archiveStoreId);
                        //    archivesService.addArchive(deposit, archiveStore, completeEvent.getArchiveIds().get(archiveStoreId));
                        //}

                        // Add to the cumulative vault size
                        Vault vault = deposit.getVault();

                        success = false;
                        while (!success) {
                            try {
                                long vaultSize = vault.getSize();
                                vault.setSize(vaultSize + deposit.getSize());
                                vaultsService.updateVault(vault);
                                success = true;
                            } catch (org.hibernate.StaleObjectStateException e) {
                                // Refresh from database and retry
                                vault = vaultsService.getVault(vault.getID());
                            }
                        }
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        deposit = depositsService.getDeposit(concreteEvent.getDepositId());
                    }
                }
                
//                // Get related information for emails
                String type = "complete";
                this.sendEmails(deposit, completeEvent, type, EmailTemplate.USER_DEPOSIT_COMPLETE, EmailTemplate.GROUP_ADMIN_DEPOSIT_COMPLETE);
                
            } else if (concreteEvent instanceof Error) {

                // Update the deposit status and job properties
                
                Error errorEvent = (Error)concreteEvent;
                
                if (deposit != null && deposit.getStatus() != Deposit.Status.COMPLETE) {
                    Boolean success = false;
                    while (!success) {
                        try {
                        	deposit.setStatus(Deposit.Status.FAILED);
                        	if(deposit.getStatus() == Deposit.Status.DELETE_IN_PROGRESS) {
                        		deposit.setStatus(Deposit.Status.DELETE_FAILED);
                        	}
                            depositsService.updateDeposit(deposit);
                            success = true;
                        } catch (org.hibernate.StaleObjectStateException e) {
                            // Refresh from database and retry
                            deposit = depositsService.getDeposit(concreteEvent.getDepositId());
                        }
                    }
                }
                
                Boolean success = false;
                while (!success) {
                    try {
                        job.setError(true);
                        job.setErrorMessage(errorEvent.getMessage());
                        jobsService.updateJob(job);
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        job = jobsService.getJob(concreteEvent.getJobId());
                    }
                }

                if(deposit != null){
                    //TODO - send email by using correct template when deposit delete fails
                    // Get related information for emails
                    String type = "error";
                    this.sendEmails(deposit, errorEvent, type, EmailTemplate.USER_DEPOSIT_ERROR, EmailTemplate.GROUP_ADMIN_DEPOSIT_ERROR);
                }
                else if(errorEvent.getAuditId() != null) {
                    // TODO: what to do with audit eror
                }

            } else if (concreteEvent instanceof RetrieveStart) {
            	RetrieveStart startEvent = (RetrieveStart)concreteEvent;
                // Update the Retrieve status
                Retrieve retrieve = retrievesService.getRetrieve(concreteEvent.getRetrieveId());
                retrieve.setStatus(Retrieve.Status.IN_PROGRESS);
                retrievesService.updateRetrieve(retrieve);
                
                String type = "retrievestart";
                this.sendEmails(deposit, startEvent, type, EmailTemplate.USER_RETRIEVE_START, EmailTemplate.GROUP_ADMIN_RETRIEVE_START);
            } else if (concreteEvent instanceof RetrieveComplete) {

            	RetrieveComplete completeEvent = (RetrieveComplete)concreteEvent;
                // Update the Retrieve status
                Retrieve retrieve = retrievesService.getRetrieve(concreteEvent.getRetrieveId());
                retrieve.setStatus(Retrieve.Status.COMPLETE);
                retrievesService.updateRetrieve(retrieve);
                
                String type = "retrievecomplete";
                this.sendEmails(deposit, completeEvent, type, EmailTemplate.USER_RETRIEVE_COMPLETE, EmailTemplate.GROUP_ADMIN_RETRIEVE_COMPLETE);
            } else if (concreteEvent instanceof DeleteStart) {
                preDeletionStatus = deposit.getStatus();
                logger.info("Pre Deletion Status:" + preDeletionStatus);
            	deposit.setStatus(Deposit.Status.DELETE_IN_PROGRESS);
                depositsService.updateDeposit(deposit);
                
            } else if (concreteEvent instanceof DeleteComplete) {
            	Boolean success = false;
            	long depositSizeBeforeDelete = deposit.getSize();
            	deposit.setStatus(Deposit.Status.DELETED);
            	deposit.setSize(0);
                depositsService.updateDeposit(deposit);

                // if this wasn't an Error deposit remove from the vault size
                if (preDeletionStatus != Deposit.Status.FAILED) {
                    Vault vault = deposit.getVault();

                    success = false;
                    //Update the vault size after deleting the deposit
                    while (!success) {
                        try {
                            long vaultSize = vault.getSize();
                            vault.setSize(vaultSize - depositSizeBeforeDelete);
                            vaultsService.updateVault(vault);
                            success = true;
                        } catch (org.hibernate.StaleObjectStateException e) {
                            // Refresh from database and retry
                            vault = vaultsService.getVault(vault.getID());
                        }
                    }
                }
            } else if (concreteEvent instanceof AuditStart) {
                // Update the Audit
                Audit audit = auditsService.getAudit(concreteEvent.getAuditId());
                audit.setStatus(Audit.Status.IN_PROGRESS);
                auditsService.updateAudit(audit);
            } else if (concreteEvent instanceof ChunkAuditStarted) {
                // Update the Audit status
                Audit audit = auditsService.getAudit(concreteEvent.getAuditId());
                DepositChunk chunk = depositsService.getDepositChunkById(concreteEvent.getChunkId());
                String archiveId = concreteEvent.getArchiveId();
                String location = concreteEvent.getLocation();

                auditsService.addAuditStatus(audit, chunk, archiveId, location);
            } else if (concreteEvent instanceof ChunkAuditComplete) {
                Audit audit = auditsService.getAudit(concreteEvent.getAuditId());
                DepositChunk chunk = depositsService.getDepositChunkById(concreteEvent.getChunkId());
                String archiveId = concreteEvent.getArchiveId();
                String location = concreteEvent.getLocation();

                List<AuditChunkStatus> auditChunkStatus =
                        auditsService.getRunningAuditChunkStatus(audit, chunk, archiveId, location);

                if(auditChunkStatus.size() != 1){
                    // TODO: Make sure it never happen
                    System.err.println("Unexpected number of running audit chunk: "+auditChunkStatus.size());
                }
                AuditChunkStatus auditInfo = auditChunkStatus.get(0);
                auditInfo.setCompleteTime(new Date());
                auditInfo.complete();
                auditsService.updateAuditChunkStatus(auditInfo);
            } else if (concreteEvent instanceof AuditComplete) {
                // Update the Audit status
                Audit audit = auditsService.getAudit(concreteEvent.getAuditId());

                // If one chunk audit failed set status as failed
                List<AuditChunkStatus> auditChunkStatus =
                        auditsService.getAuditChunkStatusFromAudit(audit);
                Optional<AuditChunkStatus> result = auditChunkStatus.stream().filter(c -> c.getStatus().equals(AuditChunkStatus.Status.ERROR)).findAny();

                if(result.isPresent()){
                    audit.setStatus(Audit.Status.FAILED);
                }
                else {
                    audit.setStatus(Audit.Status.COMPLETE);
                }
                auditsService.updateAudit(audit);
            }
            else if (concreteEvent instanceof AuditError) {
                Audit audit = auditsService.getAudit(concreteEvent.getAuditId());
                DepositChunk chunk = depositsService.getDepositChunkById(concreteEvent.getChunkId());
                String archiveId = concreteEvent.getArchiveId();
                String location = concreteEvent.getLocation();

                List<AuditChunkStatus> auditChunkStatus =
                        auditsService.getRunningAuditChunkStatus(audit, chunk, archiveId, location);

                if(auditChunkStatus.size() != 1){
                    // TODO: Make sure it never happen
                    System.err.println("Unexpected number of running audit chunk: "+auditChunkStatus.size());
                }

                AuditChunkStatus auditInfo = auditChunkStatus.get(0);
                auditInfo.setCompleteTime(new Date());
                auditInfo.failed(concreteEvent.getMessage());
                auditsService.updateAuditChunkStatus(auditInfo);

                // At the moment just email
                logger.info("Sending Error email to Admin");
                sendAuditEmails(concreteEvent, "error", EmailTemplate.AUDIT_CHUNK_ERROR);

                // TODO: try to fix chunk
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendEmails(Deposit deposit, Event event, String type, String userTemplate, String adminTemplate) throws Exception {
    	// Get related information for emails
        Vault vault = deposit.getVault();
        Group group = vault.getGroup();
        User depositUser = deposit.getUser();
        User retrieveUser = usersService.getUser(event.getUserId());
        
        logger.info("User title key: " + "user-deposit-" + type);
        String userTitle = EventListener.EMAIL_SUBJECTS.get("user-deposit-" + type);
        logger.info("Admin title key: " + "admin-deposit-" + type);
        String adminTitle = EventListener.EMAIL_SUBJECTS.get("admin-deposit-" + type);
        
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("group-name", group.getName());
        model.put("deposit-name", deposit.getName());
        model.put("deposit-id", deposit.getID());
        model.put("vault-name", vault.getName());
        model.put("vault-id", vault.getID());
//        model.put("vault-retention-expiry", vault.getRetentionPolicyExpiry());
        model.put("vault-review-date", vault.getReviewDate());
        model.put("user-id", depositUser.getID());
        model.put("user-firstname", depositUser.getFirstname());
        model.put("user-lastname", depositUser.getLastname());
        model.put("size-bytes", deposit.getArchiveSize());
        model.put("timestamp", event.getTimestamp());
        model.put("hasPersonalData", deposit.getHasPersonalData());
        if (event instanceof Error) {
            model.put("error-message", event.getMessage());
        }
        if (event instanceof Error || event instanceof RetrieveStart || event instanceof RetrieveComplete) {
            model.put("retriever-firstname", retrieveUser.getFirstname());
            model.put("retriever-lastname", retrieveUser.getLastname());
            model.put("retriever-id", retrieveUser.getID());
        }
        model.put("home-page", this.getHomeUrl());
        model.put("help-page", this.getHelpUrl());
        model.put("help-mail", this.getHelpMail());
        
        // Send email to group owners
        for (User groupAdmin : group.getOwners()) {
        	logger.info("GroupAdmin email is " + groupAdmin.getEmail());
            if (groupAdmin.getEmail() != null) {
                emailService.sendTemplateMail(groupAdmin.getEmail(),
                        adminTitle,
                        adminTemplate,
                        model);
            }
        }
        
        // Send email to the deposit user
        logger.info("DepositUser email is " + depositUser.getEmail());
        if (depositUser.getEmail() != null) {
            emailService.sendTemplateMail(depositUser.getEmail(),
                    userTitle,
                    userTemplate,
                    model);
        }
    }

    private void sendAuditEmails(Event event, String type, String template) throws Exception {

        AuditError auditErrorEven = (AuditError) event;

        logger.info("title key: " + "audit-chunk-" + type);
        String title = EventListener.EMAIL_SUBJECTS.get("audit-chunk-" + type);

        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("audit-id", auditErrorEven.getAuditId());
        model.put("chunk-id", auditErrorEven.getChunkId());
        model.put("archive-id", auditErrorEven.getArchiveId());
        model.put("location", auditErrorEven.getLocation());
        model.put("timestamp", event.getTimestamp());

        String email = auditAdminEmail; // TODO: Who do we email

        logger.info("Audit admin email is " + auditAdminEmail);
        emailService.sendTemplateMail(email, title, template, model);
    }
    
    public String getHomeUrl() {
		return this.homeUrl;
	}
	public String getHelpUrl() {
		return this.helpUrl;
	}

	public String getHelpMail() {
		return this.helpMail;
	}
}
