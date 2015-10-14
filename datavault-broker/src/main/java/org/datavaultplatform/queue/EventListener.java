package org.datavaultplatform.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateState;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Vault;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class EventListener implements MessageListener {

    private JobsService jobsService;
    private EventService eventService;
    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RestoresService restoresService;

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
            
            if (concreteEvent.getPersistent()) {
                // Persist the event properties in the database ...
                eventService.addEvent(concreteEvent);
            }
            
            // Maybe perform an action based on the event type ...
            
            if (concreteEvent instanceof InitStates) {
                
                // Update the Job state information
                InitStates initStatesEvent = (InitStates)concreteEvent;
                job.setStates(initStatesEvent.getStates());
                jobsService.updateJob(job);
            
            } else if (concreteEvent instanceof UpdateState) {
                
                // Update the Job state
                UpdateState updateStateEvent = (UpdateState)concreteEvent;
                job.setState(updateStateEvent.getState());
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
                depositsService.updateDeposit(deposit);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
