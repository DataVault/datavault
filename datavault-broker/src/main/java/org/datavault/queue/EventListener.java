package org.datavault.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavault.broker.services.DepositsService;
import org.datavault.broker.services.VaultsService;
import org.datavault.common.event.Event;
import org.datavault.common.event.deposit.*;
import org.datavault.common.model.Deposit;
import org.datavault.common.model.Vault;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class EventListener implements MessageListener {

    private VaultsService vaultsService;
    private DepositsService depositsService;
    
    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }
    
    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
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
            
            if (concreteEvent.getPersistent()) {
                // Persist the event properties in the database ...
                System.out.println("Persisting event in DB ...");
            }
            
            // Maybe perform an action based on the event type ...
            
            if (concreteEvent instanceof ComputedSize) {
                
                // Update the deposit with the computed size
                ComputedSize computedSizeEvent = (ComputedSize)concreteEvent;
                Deposit deposit = depositsService.getDeposit(computedSizeEvent.getDepositId());
                deposit.setSize(computedSizeEvent.getBytes());
                depositsService.updateDeposit(deposit);
                
                // Add to the cumulative vault size
                // TODO: locking?
                Vault vault = deposit.getVault();
                long vaultSize = vault.getSize();
                vault.setSize(vaultSize + computedSizeEvent.getBytes());
                vaultsService.updateVault(vault);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
