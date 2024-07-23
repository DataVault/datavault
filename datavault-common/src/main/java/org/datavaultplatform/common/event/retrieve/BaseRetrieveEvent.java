package org.datavaultplatform.common.event.retrieve;

import org.datavaultplatform.common.event.Event;

public abstract class BaseRetrieveEvent extends Event {

    public BaseRetrieveEvent() {
        this.eventClass = this.getClass().getCanonicalName();
    }

    public BaseRetrieveEvent(String jobId, String depositId, String retrieveId) {
        this();
        this.setJobId(jobId);
        this.depositId = depositId;
        this.retrieveId = retrieveId;
    }

    public BaseRetrieveEvent(String jobId, String depositId, String retrieveId, String message) {
        this(jobId, depositId, retrieveId);
        this.message = message;
    }

}
