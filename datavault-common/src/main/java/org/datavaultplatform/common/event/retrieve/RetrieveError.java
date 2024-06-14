package org.datavaultplatform.common.event.retrieve;

import jakarta.persistence.Entity;

@Entity
public class RetrieveError extends BaseRetrieveEvent {
    public RetrieveError() {
    }

    public RetrieveError(String jobId, String depositId, String retrieveId, String msg) {
        super(jobId, depositId, retrieveId, msg);
    }
}
