package org.datavaultplatform.worker.operations;

import java.util.Map;
import java.util.Optional;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.StoredChunks;
import org.datavaultplatform.worker.tasks.ArchiveStoreDepositedFiles;
import org.datavaultplatform.worker.tasks.ArchiveStoresDepositedFiles;


@Slf4j
public record ChunkUploadTracker(StoredChunks previouslyStoredChunks,
                                 ArchiveStoresDepositedFiles archiveStoresDepositedFiles, int chunkNumber, File chunk,
                                 Map<String, ArchiveStore> archiveStores,
                                 String depositId, UserEventSender userEventSender,
                                 String jobID) implements Callable<HashMap<String, String>> {

    @Override
    public HashMap<String, String> call() throws Exception {
        log.debug("Copying chunk: [{}]", chunk.getName());

        TaskExecutor<HashMap<String, String>> executor = new TaskExecutor<>(2, "Device upload failed for[%s]".formatted(chunk.getName()));
        for (String archiveStoreId : archiveStores.keySet()) {
            executeStoreChunk(executor, archiveStoreId);
        }

        HashMap<String, String> archiveIds = new HashMap<>();
        executor.execute(archiveIds::putAll);
        log.debug("Chunk upload task completed: [{}]", this.chunkNumber);
        // send event saying this chunk has been stored on these archives - yuch.
        return archiveIds;
    }

    private void executeStoreChunk(TaskExecutor<HashMap<String, String>> executor, String archiveStoreId) {
        if (previouslyStoredChunks.isStored(archiveStoreId, chunkNumber)) {
            return;
        }
        ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
        ArchiveStoreDepositedFiles archiveStoreDepositedFile = archiveStoresDepositedFiles.getForArchiveStoreId(archiveStoreId);
        DeviceTracker dt = new DeviceTracker(archiveStoreDepositedFile, archiveStore, archiveStoreId,
                Optional.of(chunkNumber), depositId,
                jobID, userEventSender,
                chunk);
        log.debug("Creating device task: for[{}/{}] on [{}]", chunk.getName(), chunkNumber, archiveStore.getClass());
        executor.add(dt);
    }
}
