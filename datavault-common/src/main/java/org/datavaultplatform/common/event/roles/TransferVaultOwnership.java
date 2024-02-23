package org.datavaultplatform.common.event.roles;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.request.TransferVault;

@Entity
public class TransferVaultOwnership extends Event {

    public TransferVaultOwnership(){
    }
    public TransferVaultOwnership(TransferVault transfer, Vault vault, String userId){
        super("Transfer of ownership to "+transfer.getUserId()+" by "+userId );
        this.eventClass = CreateRoleAssignment.class.getCanonicalName();
    }
}
