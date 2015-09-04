package org.datavaultplatform.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.broker.services.EventService;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Vault;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class EventListener implements MessageListener {

    private VaultsService vaultsService;
    private DepositsService depositsService;
    private EventService eventService;
    
    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }
    
    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }
    
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
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
            
            if (concreteEvent.getPersistent()) {
                // Persist the event properties in the database ...
                eventService.addEvent(concreteEvent);
            }
            
            // Maybe perform an action based on the event type ...
            
            if (concreteEvent instanceof Start) {
                
                // Update the deposit status
                deposit.setStatus(Deposit.Status.CALCULATE_SIZE);
                depositsService.updateDeposit(deposit);
                
            } else if (concreteEvent instanceof ComputedSize) {
                
                // Update the deposit with the computed size
                ComputedSize computedSizeEvent = (ComputedSize)concreteEvent;
                deposit.setSize(computedSizeEvent.getBytes());
                deposit.setStatus(Deposit.Status.TRANSFER_FILES);
                depositsService.updateDeposit(deposit);
                
                // Add to the cumulative vault size
                // TODO: locking?
                Vault vault = deposit.getVault();
                long vaultSize = vault.getSize();
                vault.setSize(vaultSize + computedSizeEvent.getBytes());
                vaultsService.updateVault(vault);
                
            } else if (concreteEvent instanceof TransferProgress) {
                
                // Update the deposit transfer progress
                TransferProgress transferProgressEvent = (TransferProgress)concreteEvent;
                deposit.setBytesTransferred(transferProgressEvent.getBytes());
                deposit.setBytesPerSec(transferProgressEvent.getBytesPerSec());
                depositsService.updateDeposit(deposit);
                
            } else if (concreteEvent instanceof TransferComplete) {
                
                // Update the deposit status
                deposit.setStatus(Deposit.Status.CREATE_PACKAGE);
                depositsService.updateDeposit(deposit);
                
            } else if (concreteEvent instanceof PackageComplete) {
                
                // Update the deposit status
                deposit.setStatus(Deposit.Status.STORE_ARCHIVE_PACKAGE);
                depositsService.updateDeposit(deposit);
                
            } else if (concreteEvent instanceof Complete) {
                
                // Update the deposit status
                deposit.setStatus(Deposit.Status.COMPLETE);
                depositsService.updateDeposit(deposit);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
