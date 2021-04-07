package org.datavaultplatform.common.event.vault;

import javax.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class UpdatedDescription extends Event {
    public UpdatedDescription() {};
    public UpdatedDescription(String oldDesc, String newDesc) {
        super("Vault description updated from " + oldDesc + " to " + newDesc);
        this.eventClass = Create.class.getCanonicalName();
    }
}
