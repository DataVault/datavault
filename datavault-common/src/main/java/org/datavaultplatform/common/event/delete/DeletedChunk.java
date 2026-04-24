package org.datavaultplatform.common.event.delete;

import jakarta.persistence.Entity;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.storage.ArchiveStore;

@Entity
public class DeletedChunk extends Event {

    public DeletedChunk() {
    }

    public DeletedChunk(String jobId, String depositId,
                        int chunkNumber, int numberOfChunks,
                         Class<? extends ArchiveStore> archiveType, String archiveStoreId, String location) {
        super("Deleted Chunk [%d/%d] from (%s/%s/%s)"
                .formatted(chunkNumber, numberOfChunks, archiveType.getSimpleName(), archiveStoreId, location));
        this.setEventClass(DeletedChunk.class.getCanonicalName());
        this.setDepositId(depositId);
        this.setJobId(jobId);
        this.setChunkNumber(chunkNumber);
        this.setArchiveStoreId(archiveStoreId);

        // location is not persisted to database - that's why it's also in the message
        this.setLocation(location);
    }
    
    @Override
    public String toString() {
        return getMessage();        
    }
}
