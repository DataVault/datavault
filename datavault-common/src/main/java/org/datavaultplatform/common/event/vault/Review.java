package org.datavaultplatform.common.event.vault;

import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

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
