package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class CompleteCopyUpload extends Event {

	CompleteCopyUpload() {
		
	};

	public CompleteCopyUpload(String jobId, String depositId, String type) {
		super("Copy Upload finished - (" + type + ")");
		this.eventClass = CompleteCopyUpload.class.getCanonicalName();
		this.depositId = depositId;
		this.jobId = jobId;
	}
}
