package org.datavaultplatform.common.event.retrieve;

import jakarta.persistence.Entity;

@Entity
public class UserStoreSpaceAvailableChecked extends BaseRetrieveEvent {
    public static final String MESSAGE = "User Store Space Available Checked";

    public UserStoreSpaceAvailableChecked() {
    }

    public UserStoreSpaceAvailableChecked(String jobId, String depositId, String retrieveId) {
        super(jobId, depositId, retrieveId, MESSAGE);
    }
}
