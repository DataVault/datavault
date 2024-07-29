package org.datavaultplatform.common.event.vault;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class UpdatedName extends Event {

    public UpdatedName() {}
    public UpdatedName(String oldName, String newName) {
            super("Vault name updated from " + oldName + " to " + newName);
            this.eventClass = UpdatedName.class.getCanonicalName();
        }
}
