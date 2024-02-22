package org.datavaultplatform.common.event.vault;

import org.datavaultplatform.common.event.Event;

import jakarta.persistence.Entity;

@Entity
public class Pending extends Event {

    public Pending() {}

    public Pending(String vaultId) {
        super("Created pending vault");
        this.eventClass = Pending.class.getCanonicalName();
        this.vaultId = vaultId;
    }
}
