package org.datavaultplatform.common.event.retrieve;

import jakarta.persistence.Entity;

@Entity
public class RetrieveComplete extends BaseRetrieveEvent {
    public static final String MESSAGE = "Deposit retrieve completed";

    public RetrieveComplete() {
    }

    public RetrieveComplete(String jobId, String depositId, String retrieveId) {
        super(jobId, depositId, retrieveId, MESSAGE);
    }
}
