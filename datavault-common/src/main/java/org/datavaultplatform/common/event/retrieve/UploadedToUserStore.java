package org.datavaultplatform.common.event.retrieve;

import jakarta.persistence.Entity;

@Entity
public class UploadedToUserStore extends BaseRetrieveEvent {
    public static final String MESSAGE = "Uploaded To User Store";

    public UploadedToUserStore() {
    }

    public UploadedToUserStore(String jobId, String depositId, String retrieveId) {
        super(jobId, depositId, retrieveId, MESSAGE);
    }
}
