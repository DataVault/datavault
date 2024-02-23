package org.datavaultplatform.common.event.roles;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.Vault;

@Entity
public class OrphanVault extends Event {

    public OrphanVault() {
    }
    public OrphanVault(Vault vault, String userId){
        super("Vault made orphan by "+userId);
        this.eventClass = CreateRoleAssignment.class.getCanonicalName();
    }
}
