package org.datavaultplatform.common.event.vault;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.datavaultplatform.common.event.Event;

@Entity
@Table(name="Events")
public class Create extends Event {
    
    Create() {};
    public Create(String vaultId) {
        super("Created vault");
        this.eventClass = Create.class.getCanonicalName();
        this.vaultId = vaultId;
    }
}
