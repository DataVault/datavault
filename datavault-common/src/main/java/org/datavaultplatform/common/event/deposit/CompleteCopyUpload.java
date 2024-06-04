package org.datavaultplatform.common.event.deposit;

import jakarta.persistence.Entity;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.event.Event;
import org.springframework.util.Assert;

@Entity
public class CompleteCopyUpload extends Event {

	public CompleteCopyUpload() {
	}

	public CompleteCopyUpload(String depositId, String jobId, String type, Integer chunkNumber, String archiveStoreId, String archiveId) {

		super("Chunk " + chunkNumber + " upload finished - (" + type + ")");

		Assert.isTrue(depositId != null, "The depositId cannot be null");
		Assert.isTrue(jobId != null, "The jobId cannot be null");
		Assert.isTrue(type != null, "The type cannot be null");
		Assert.isTrue(StringUtils.isNotBlank(archiveStoreId), "The archiveStoreId cannot be blank");
		Assert.isTrue(StringUtils.isNotBlank(archiveId), "The archiveId cannot be blank");
		
		this.eventClass = CompleteCopyUpload.class.getCanonicalName();
		this.depositId = depositId;
		this.jobId = jobId;
		this.setChunkNumber(chunkNumber);
		this.setArchiveStoreId(archiveStoreId);
		this.archiveId = archiveId;
	}
}
