package org.datavaultplatform.common.event.vault;

import org.datavaultplatform.common.event.Event;

import javax.persistence.Entity;

@Entity
public class Review extends Event {

    Review() {};
    public Review(String vaultId) {
        super("Reviewed vault");
        this.eventClass = Review.class.getCanonicalName();
        this.vaultId = vaultId;
    }
}
