package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.storage.ArchiveStore;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

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
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<HashMap<String, String>>> futures = new ArrayList<>();
        HashMap<String, String> archiveIds = new HashMap<>();
        for (String archiveStoreId : archiveStores.keySet() ) {
            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
            DeviceTracker dt = new DeviceTracker(archiveStore, archiveStoreId,
                chunkNumber, depositId,
                jobID, eventSender,
                chunk, userID);
            log.debug("Creating device task:" + archiveStore.getClass());
            Future<HashMap<String, String>> dtFuture = executor.submit(dt);
            futures.add(dtFuture);
        }
        executor.shutdown();

        for (Future<HashMap<String, String>> future : futures) {
            try {
                HashMap<String, String> result = future.get();
                archiveIds.putAll(result);
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                if (cause instanceof Exception) {
                    log.info("Device upload failed. " + cause.getMessage());
                    throw (Exception) cause;
                }
            }
        }
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
