package org.datavaultplatform.common.event.roles;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.Vault;

import javax.persistence.Entity;

@Entity
public class OrphanVault extends Event {

    OrphanVault(){};
    public OrphanVault(Vault vault, String userId){
        super("Vault made orphan by "+userId);
        this.eventClass = CreateRoleAssignment.class.getCanonicalName();
    }
}
