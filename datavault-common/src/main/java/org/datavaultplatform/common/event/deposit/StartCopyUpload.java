package org.datavaultplatform.common.event.deposit;

import java.util.Optional;
import jakarta.persistence.Entity;
import org.datavaultplatform.common.event.Event;

@Entity
public class StartCopyUpload extends Event {

	public StartCopyUpload() {
	}

	public StartCopyUpload(String jobId, String depositId, String type, Optional<Integer> optChunkNumber) {
		super("Chunk " + optChunkNumber + " upload started - (" + type + ")");
		this.eventClass = StartCopyUpload.class.getCanonicalName();
		this.depositId = depositId;
		this.jobId = jobId;
	}
}
