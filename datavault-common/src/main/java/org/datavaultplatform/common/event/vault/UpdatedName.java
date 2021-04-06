package org.datavaultplatform.common.event.vault;

import javax.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class UpdatedName extends Event {

    public UpdatedName() {};
    public UpdatedName(String oldName, String newName) {
            super("Vault name updated from " + oldName + " to " + newName);
            this.eventClass = Create.class.getCanonicalName();
        }
}
