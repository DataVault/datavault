package org.datavaultplatform.common.event.vault;

import org.datavaultplatform.common.event.Event;

import jakarta.persistence.Entity;

@Entity
public class Review extends Event {

    public Review() {
    }
    public Review(String vaultId) {
        super("Reviewed vault");
        this.eventClass = Review.class.getCanonicalName();
        this.vaultId = vaultId;
    }
}
