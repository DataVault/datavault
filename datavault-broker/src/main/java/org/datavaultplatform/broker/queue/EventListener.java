package org.datavaultplatform.broker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.common.model.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class EventListener implements MessageListener {

    private JobsService jobsService;
    private EventService eventService;
    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RetrievesService retrievesService;
    private UsersService usersService;

    public void setJobsService(JobsService jobsService) {
        this.jobsService = jobsService;
    }
    
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
    
    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }
    
    public void setDepositsService(DepositsService depositsService) { this.depositsService = depositsService; }

    public void setRetrievesService(RetrievesService retrievesService) {
        this.retrievesService = retrievesService;
    }
    
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public void onMessage(Message msg) {
        
        String messageBody = new String(msg.getBody());
        System.out.println("[x] Received '" + messageBody + "'");
        
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
                

            } else if (concreteEvent instanceof Complete) {

                // Update the deposit status and archive properties
                Complete completeEvent = (Complete)concreteEvent;
                
                Boolean success = false;
                while (!success) {
                    try {
                        deposit.setStatus(Deposit.Status.COMPLETE);
                        deposit.setArchiveId(completeEvent.getArchiveId());
                        deposit.setArchiveSize(completeEvent.getArchiveSize());
                        depositsService.updateDeposit(deposit);
                        success = true;
                    } catch (org.hibernate.StaleObjectStateException e) {
                        // Refresh from database and retry
                        deposit = depositsService.getDeposit(concreteEvent.getDepositId());
                    }
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
