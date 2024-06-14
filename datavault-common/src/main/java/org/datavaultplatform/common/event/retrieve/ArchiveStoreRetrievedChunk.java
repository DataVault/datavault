package org.datavaultplatform.common.event.retrieve;

import jakarta.persistence.Entity;

@Entity
public class ArchiveStoreRetrievedChunk extends BaseRetrieveEvent {
    public static final String MESSAGE = "Archive Store Retrieved Chunk";

    public ArchiveStoreRetrievedChunk() {
    }

    public ArchiveStoreRetrievedChunk(String jobId, String depositId, String retrieveId, int chunkNumber) {
        super(jobId, depositId, retrieveId, MESSAGE);
        this.setChunkNumber(chunkNumber);
    }
}
