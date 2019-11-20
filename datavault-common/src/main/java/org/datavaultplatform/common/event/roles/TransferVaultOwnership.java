package org.datavaultplatform.common.event.roles;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.request.TransferVault;

import javax.persistence.Entity;

@Entity
public class TransferVaultOwnership extends Event {

    TransferVaultOwnership(){};
    public TransferVaultOwnership(TransferVault transfer, Vault vault, String userId){
        super("Transfer of ownership to "+transfer.getUserId()+" by "+userId );
        this.eventClass = CreateRoleAssignment.class.getCanonicalName();
    }
}
