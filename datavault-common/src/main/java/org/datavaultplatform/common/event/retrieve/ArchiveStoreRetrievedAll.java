package org.datavaultplatform.common.event.retrieve;

import jakarta.persistence.Entity;

@Entity
public class ArchiveStoreRetrievedAll extends BaseRetrieveEvent {

    public static final String MESSAGE = "Archive Store Retrieved All";

    public ArchiveStoreRetrievedAll() {
    }

    public ArchiveStoreRetrievedAll(String jobId, String depositId, String retrieveId) {
        super(jobId, depositId, retrieveId, MESSAGE);
    }
}
