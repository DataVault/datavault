package org.datavaultplatform.worker.operations;

import java.util.Optional;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.task.TaskExecutor;


@Slf4j
public class ChunkUploadTracker implements Callable<HashMap<String, String>> {

    private final File chunk;
    private final int chunkNumber;
    private final HashMap<String, ArchiveStore> archiveStores;
    private final String depositId;
    private final String jobID;
    private final EventSender eventSender;
    private final File tarFile;
    private final String userID;

    public ChunkUploadTracker(int chunkNumber, File chunk, HashMap<String, ArchiveStore> archiveStores, String depositId, EventSender eventSender, String jobID, File tarFile,
        String userID) {
        this.chunkNumber = chunkNumber;
        this.chunk = chunk;
        this.archiveStores = archiveStores;
        this.depositId = depositId;
        this.eventSender = eventSender;
        this.jobID = jobID;
        this.tarFile = tarFile;
        this.userID = userID;
    }

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

    public File getChunk() {
        return this.chunk;
    }


    public int getChunkNumber() {
        return this.chunkNumber;
    }

    public HashMap<String, ArchiveStore> getArchiveStores() {
        return this.archiveStores;
    }

    public String getDepositId() {
        return this.depositId;
    }

    public String getJobID() {
        return this.jobID;
    }


    public EventSender getEventSender() {
        return this.eventSender;
    }

    public File getTarFile() {
        return this.tarFile;
    }

    public String getUserID() {
        return userID;
    }
}
