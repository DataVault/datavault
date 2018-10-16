package org.datavaultplatform.broker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.common.model.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventListener implements MessageListener {

    private JobsService jobsService;
    private EventService eventService;
    private VaultsService vaultsService;
    private DepositsService depositsService;
    private ArchiveStoreService archiveStoreService;
    private ArchivesService archivesService;
    private RetrievesService retrievesService;
    private UsersService usersService;
    private EmailService emailService;
    private static final Map<String, String> EMAIL_SUBJECTS;
    static {
    	EMAIL_SUBJECTS = new HashMap<String, String>();
    	EMAIL_SUBJECTS.put("user-deposit-start", "Confirmation - your new DataVault deposit is starting.");
    	EMAIL_SUBJECTS.put("user-deposit-complete", "Confirmation - your new DataVault deposit is complete.");
    	EMAIL_SUBJECTS.put("user-deposit-error", "DataVault ERROR - attempted deposit failed");
    	EMAIL_SUBJECTS.put("admin-deposit-start", "Confirmation - a new DataVault deposit is starting.");
    	EMAIL_SUBJECTS.put("admin-deposit-complete", "Confirmation - a new DataVault deposit is complete.");
    	EMAIL_SUBJECTS.put("admin-deposit-error", "DataVault ERROR - attempted deposit failed");
    	EMAIL_SUBJECTS.put("user-deposit-retrievestart", "Confirmation - your new DataVault retrieval is starting.");
    	EMAIL_SUBJECTS.put("user-deposit-retrievecomplete", "Confirmation - your new DataVault retrieval is complete.");
    	EMAIL_SUBJECTS.put("admin-deposit-retrievestart", "Confirmation - a new DataVault retrieval is starting.");
    	EMAIL_SUBJECTS.put("admin-deposit-retrievecomplete", "Confirmation - a new DataVault retrieval is complete.");
    }

    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);
    
    public void setJobsService(JobsService jobsService) { this.jobsService = jobsService; }
    public void setEventService(EventService eventService) { this.eventService = eventService; }
    public void setVaultsService(VaultsService vaultsService) { this.vaultsService = vaultsService; }
    public void setDepositsService(DepositsService depositsService) { this.depositsService = depositsService; }
    public void setArchiveStoreService(ArchiveStoreService archiveStoreService) {this.archiveStoreService = archiveStoreService;}
    public void setArchivesService(ArchivesService archivesService) { this.archivesService = archivesService;}
    public void setRetrievesService(RetrievesService retrievesService) { this.retrievesService = retrievesService; }
    public void setUsersService(UsersService usersService) { this.usersService = usersService; }
    public void setEmailService(EmailService emailService) { this.emailService = emailService; }

    @Override
    public void onMessage(Message msg) {
        
        String messageBody = new String(msg.getBody());
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
              this.sendEmails(deposit, concreteEvent, type, "user-deposit-start.vm", "group-admin-deposit-start.vm");
                
            } else if (concreteEvent instanceof ComputedSize) {
                
                // Update the deposit with the computed size
                ComputedSize computedSizeEvent = (ComputedSize)concreteEvent;
                
                Boolean success = false;
                while (!success) {
                    try {
                        deposit.setSize(computedSizeEvent.getBytes());
                        depositsService.updateDeposit(deposit);
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        deposit = depositsService.getDeposit(concreteEvent.getDepositId());
                    }
                }

                // Add to the cumulative vault size
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
                }

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
                        for (String archiveStoreId : completeEvent.getArchiveIds().keySet()) {
                            ArchiveStore archiveStore = archiveStoreService.getArchiveStore(archiveStoreId);
                            archivesService.addArchive(deposit, archiveStore, completeEvent.getArchiveIds().get(archiveStoreId));
                        }

                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        deposit = depositsService.getDeposit(concreteEvent.getDepositId());
                    }
                }
                
//                // Get related information for emails
                String type = "complete";
                this.sendEmails(deposit, completeEvent, type, "user-deposit-complete.vm", "group-admin-deposit-complete.vm");
                
            } else if (concreteEvent instanceof Error) {

                // Update the deposit status and job properties
                
                Error errorEvent = (Error)concreteEvent;
                
                if (deposit.getStatus() != Deposit.Status.COMPLETE) {
                    Boolean success = false;
                    while (!success) {
                        try {
                            deposit.setStatus(Deposit.Status.FAILED);
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
                
                // Get related information for emails
                String type = "error";
                this.sendEmails(deposit, errorEvent, type, "user-deposit-error.vm", "group-admin-deposit-error.vm");
            } else if (concreteEvent instanceof RetrieveStart) {
            	RetrieveStart startEvent = (RetrieveStart)concreteEvent;
                // Update the Retrieve status
                Retrieve retrieve = retrievesService.getRetrieve(concreteEvent.getRetrieveId());
                retrieve.setStatus(Retrieve.Status.IN_PROGRESS);
                retrievesService.updateRetrieve(retrieve);
                
                String type = "retrievestart";
                this.sendEmails(deposit, startEvent, type, "user-retrieve-start.vm", "group-admin-retrieve-start.vm");
            } else if (concreteEvent instanceof RetrieveComplete) {

            	RetrieveComplete completeEvent = (RetrieveComplete)concreteEvent;
                // Update the Retrieve status
                Retrieve retrieve = retrievesService.getRetrieve(concreteEvent.getRetrieveId());
                retrieve.setStatus(Retrieve.Status.COMPLETE);
                retrievesService.updateRetrieve(retrieve);
                
                String type = "retrievecomplete";
                this.sendEmails(deposit, completeEvent, type, "user-retrieve-complete.vm", "group-admin-retrieve-complete.vm");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendEmails(Deposit deposit, Event event, String type, String userTemplate, String adminTemplate) throws Exception {
    	// Get related information for emails
        Vault vault = deposit.getVault();
        Group group = vault.getGroup();
        User depositUser = usersService.getUser(event.getUserId());
        
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
        model.put("vault-retention-expiry", vault.getRetentionPolicyExpiry());
        model.put("user-id", depositUser.getID());
        model.put("user-firstname", depositUser.getFirstname());
        model.put("user-lastname", depositUser.getLastname());
        model.put("size-bytes", deposit.getArchiveSize());
        model.put("timestamp", event.getTimestamp());
        model.put("hasPersonalData", deposit.getHasPersonalData());
        if (event instanceof Error) {
            model.put("error-message", event.getMessage());
        }
        
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
}
