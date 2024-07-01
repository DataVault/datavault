package org.datavaultplatform.common.event.vault;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class Create extends Event {
    
    public Create() {
    }
    public Create(String vaultId) {
        super("Created vault");
        this.eventClass = Create.class.getCanonicalName();
        this.vaultId = vaultId;
    }
}
