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
import java.util.HashMap;
import java.util.List;

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
                
                // Get related information for emails
                Vault vault = deposit.getVault();
                Group group = vault.getGroup();
                User depositUser = usersService.getUser(completeEvent.getUserId());
                
                HashMap model = new HashMap();
                model.put("group-name", group.getName());
                model.put("deposit-note", deposit.getNote());
                model.put("deposit-id", deposit.getID());
                model.put("vault-name", vault.getName());
                model.put("vault-id", vault.getID());
                model.put("vault-retention-expiry", vault.getRetentionPolicyExpiry());
                model.put("user-id", depositUser.getID());
                model.put("user-firstname", depositUser.getFirstname());
                model.put("user-lastname", depositUser.getLastname());
                model.put("size-bytes", deposit.getArchiveSize());
                model.put("timestamp", completeEvent.getTimestamp());
                
                // Send email to group owners
                for (User groupAdmin : group.getOwners()) {
                    if (groupAdmin.getEmail() != null) {
                        emailService.sendTemplateMail(groupAdmin.getEmail(),
                                "Data Vault - new deposit for [" + group.getName() + "]",
                                "group-admin-deposit-complete.vm",
                                model);
                    }
                }
                
                // Send email to the deposit user
                if (depositUser.getEmail() != null) {
                    emailService.sendTemplateMail(depositUser.getEmail(),
                            "Data Vault - deposit complete [" + deposit.getNote() + "]",
                            "user-deposit-complete.vm",
                            model);
                }
                
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
                Vault vault = deposit.getVault();
                Group group = vault.getGroup();
                User depositUser = usersService.getUser(errorEvent.getUserId());
                
                HashMap model = new HashMap();
                model.put("group-name", group.getName());
                model.put("deposit-note", deposit.getNote());
                model.put("deposit-id", deposit.getID());
                model.put("vault-name", vault.getName());
                model.put("vault-id", vault.getID());
                model.put("user-id", depositUser.getID());
                model.put("user-firstname", depositUser.getFirstname());
                model.put("user-lastname", depositUser.getLastname());
                model.put("timestamp", errorEvent.getTimestamp());
                model.put("error-message", errorEvent.getMessage());
                
                // Send email to group owners
                for (User groupAdmin : group.getOwners()) {
                    if (groupAdmin.getEmail() != null) {
                        emailService.sendTemplateMail(groupAdmin.getEmail(),
                                "Data Vault - deposit error for [" + group.getName() + "]",
                                "group-admin-deposit-error.vm",
                                model);
                    }
                }
                
                // Send email to the deposit user
                if (depositUser.getEmail() != null) {
                    emailService.sendTemplateMail(depositUser.getEmail(),
                            "Data Vault - deposit error [" + deposit.getNote() + "]",
                            "user-deposit-error.vm",
                            model);
                }
                
            } else if (concreteEvent instanceof RetrieveStart) {

                // Update the Retrieve status
                Retrieve retrieve = retrievesService.getRetrieve(concreteEvent.getRetrieveId());
                retrieve.setStatus(Retrieve.Status.IN_PROGRESS);
                retrievesService.updateRetrieve(retrieve);
            } else if (concreteEvent instanceof RetrieveComplete) {

                // Update the Retrieve status
                Retrieve retrieve = retrievesService.getRetrieve(concreteEvent.getRetrieveId());
                retrieve.setStatus(Retrieve.Status.COMPLETE);
                retrievesService.updateRetrieve(retrieve);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
