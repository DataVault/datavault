package org.datavaultplatform.broker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.*;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.restore.*;
import org.datavaultplatform.common.model.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class EventListener implements MessageListener {

    private JobsService jobsService;
    private EventService eventService;
    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RestoresService restoresService;
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

    public void setRestoresService(RestoresService restoresService) {
        this.restoresService = restoresService;
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
                job.setState(concreteEvent.nextState);
                jobsService.updateJob(job);
            }
            
            // Maybe perform an action based on the event type ...
            
            if (concreteEvent instanceof InitStates) {
                
                // Update the Job state information
                InitStates initStatesEvent = (InitStates)concreteEvent;
                job.setStates(initStatesEvent.getStates());
                jobsService.updateJob(job);
            
            } else if (concreteEvent instanceof UpdateProgress) {
                
                // Update the Job state
                UpdateProgress updateStateEvent = (UpdateProgress)concreteEvent;
                job.setProgress(updateStateEvent.getProgress());
                job.setProgressMax(updateStateEvent.getProgressMax());
                job.setProgressMessage(updateStateEvent.getProgressMessage());
                jobsService.updateJob(job);
                
            } else if (concreteEvent instanceof Start) {
                
                // Update the deposit status
                deposit.setStatus(Deposit.Status.IN_PROGRESS);
                depositsService.updateDeposit(deposit);
                
            } else if (concreteEvent instanceof ComputedSize) {
                
                // Update the deposit with the computed size
                ComputedSize computedSizeEvent = (ComputedSize)concreteEvent;
                deposit.setSize(computedSizeEvent.getBytes());
                depositsService.updateDeposit(deposit);
                
                // Add to the cumulative vault size
                // TODO: locking?
                Vault vault = deposit.getVault();
                long vaultSize = vault.getSize();
                vault.setSize(vaultSize + computedSizeEvent.getBytes());
                vaultsService.updateVault(vault);

            } else if (concreteEvent instanceof Complete) {

                // Update the deposit status and archive properties
                Complete completeEvent = (Complete)concreteEvent;
                deposit.setStatus(Deposit.Status.COMPLETE);
                deposit.setArchiveId(completeEvent.getArchiveId());
                deposit.setArchiveSize(completeEvent.getArchiveSize());
                depositsService.updateDeposit(deposit);
            } else if (concreteEvent instanceof RestoreStart) {

                // Update the Restore status
                Restore restore = restoresService.getRestore(concreteEvent.getRestoreId());
                restore.setStatus(Restore.Status.IN_PROGRESS);
                restoresService.updateRestore(restore);
            } else if (concreteEvent instanceof RestoreComplete) {

                // Update the Restore status
                Restore restore = restoresService.getRestore(concreteEvent.getRestoreId());
                restore.setStatus(Restore.Status.COMPLETE);
                restoresService.updateRestore(restore);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
