package org.datavaultplatform.common.event.deposit;

import javax.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class StartCopyUpload extends Event {

	StartCopyUpload() {
		
	};

	public StartCopyUpload(String jobId, String depositId, String type, int chunkNum) {
		super("Chunk " + chunkNum + " upload started - (" + type + ")");
		this.eventClass = StartCopyUpload.class.getCanonicalName();
		this.depositId = depositId;
		this.jobId = jobId;
	}
}
