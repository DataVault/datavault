package org.datavaultplatform.common.event.deposit;

import java.util.Optional;
import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class CompleteCopyUpload extends Event {

	public CompleteCopyUpload() {
	}

	public CompleteCopyUpload(String jobId, String depositId, String type, Optional<Integer> chunkNum) {
		super("Chunk " + chunkNum + " upload finished - (" + type + ")");
		this.eventClass = CompleteCopyUpload.class.getCanonicalName();
		this.depositId = depositId;
		this.jobId = jobId;
	}
}
