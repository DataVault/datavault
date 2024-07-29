package org.datavaultplatform.common.event.vault;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class UpdatedDescription extends Event {
    public UpdatedDescription() {}

    public UpdatedDescription(String oldDesc, String newDesc) {
        super("Vault description updated from " + oldDesc + " to " + newDesc);
        this.eventClass = UpdatedDescription.class.getCanonicalName();
    }
}
