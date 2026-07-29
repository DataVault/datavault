package org.datavaultplatform.worker.tasks;

import lombok.Getter;
import org.datavaultplatform.common.storage.ArchiveStore;

@Getter
public class DeleteFileException extends Exception {
    private final int chunkNumber;
    private final String archiveStoreId;
    private final String location;

    public DeleteFileException(Class<? extends ArchiveStore> archiveStoreClass, String archiveStoreId, String location, int chunkNumber, Exception ex) {
        super("ArchiveStore[%s/%s]Location[%s]ChunkNum[%d]Cause[%s]".formatted(archiveStoreClass.getSimpleName(), archiveStoreId, location, chunkNumber, getCause(ex)), ex);
        this.chunkNumber = chunkNumber;
        this.archiveStoreId = archiveStoreId;
        this.location = location;
    }

    public DeleteFileException(Delete.ArchiveContext archiveContext, String location, int chunkNumber, Exception ex) {
        this(archiveContext.archiveStore().getClass(), archiveContext.archiveStoreId(), location, chunkNumber, ex);
    }

    private static String  getCause(Exception ex) {
        return "%s/%s".formatted(ex.getClass().getName(), ex.getMessage());
    }
}
