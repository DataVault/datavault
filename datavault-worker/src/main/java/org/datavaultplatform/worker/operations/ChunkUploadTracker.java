package org.datavaultplatform.worker.operations;

import java.util.Optional;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.worker.tasks.TaskExecutor;

@Slf4j
public record ChunkUploadTracker(int chunkNumber, File chunk, HashMap<String, ArchiveStore> archiveStores,
                                 String depositId, EventSender eventSender, String jobID, File tarFile,
                                 String userID) implements Callable<HashMap<String, String>> {

    @Override
    public HashMap<String, String> call() throws Exception {
        log.debug("Copying chunk: "+chunk.getName());

        TaskExecutor<HashMap<String,String>> executor = new TaskExecutor<>(2,"Device upload failed.");
        for (String archiveStoreId : archiveStores.keySet() ) {
            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
            DeviceTracker dt = new DeviceTracker(archiveStore, archiveStoreId,
                Optional.of(chunkNumber), depositId,
                jobID, eventSender,
                chunk, userID);
            log.debug("Creating device task:" + archiveStore.getClass());
            executor.add(dt);
        }

        HashMap<String, String> archiveIds = new HashMap<>();
        executor.execute(archiveIds::putAll);
        log.debug("Chunk upload task completed: " + this.chunkNumber);
        return archiveIds;
    }
}
