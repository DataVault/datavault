package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class ChunkUploadTracker implements Callable {

    private File chunk;
    private int chunkNumber;
    private HashMap<String, ArchiveStore> archiveStores = new HashMap<>();
    private String depositId;
    private String jobID;
    private EventSender eventSender;
    private File tarFile;
    private String userID;
    private static final Logger logger = LoggerFactory.getLogger(ChunkUploadTracker.class);

    @Override
    public HashMap<String, String> call() throws Exception {
        logger.debug("Copying chunk: "+chunk.getName());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<HashMap<String, String>>> futures = new ArrayList<>();
        HashMap<String, String> archiveIds = new HashMap<>();
        for (String archiveStoreId : archiveStores.keySet() ) {
            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
            DeviceTracker dt = new DeviceTracker();
            dt.setArchiveStore(archiveStore);
            dt.setArchiveStoreId(archiveStoreId);
            dt.setChunkNumber(chunkNumber);
            dt.setDepositId(depositId);
            dt.setJobID(jobID);
            dt.setEventSender(eventSender);
            dt.setTarFile(chunk);
            dt.setUserID(userID);
            logger.debug("Creating device thread:" + archiveStore.getClass());
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
                    logger.info("Device upload failed. " + cause.getMessage());
                    throw (Exception) cause;
                }
            }
        }
        logger.debug("Chunk upload thread completed: " + this.chunkNumber);
        return archiveIds;
    }

    public File getChunk() {
        return this.chunk;
    }

    public void setChunk(File chunk) {
        this.chunk = chunk;
    }

    public int getChunkNumber() {
        return this.chunkNumber;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public HashMap<String, ArchiveStore> getArchiveStores() {
        return this.archiveStores;
    }

    public void setArchiveStores(HashMap<String, ArchiveStore> archiveStores) {
        this.archiveStores = archiveStores;
    }

    public String getDepositId() {
        return this.depositId;
    }

    public void setDepositId(String depositId) {
        this.depositId = depositId;
    }

    public String getJobID() {
        return this.jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public EventSender getEventSender() {
        return this.eventSender;
    }

    public void setEventSender(EventSender eventSender) {
        this.eventSender = eventSender;
    }

    public File getTarFile() {
        return this.tarFile;
    }

    public void setTarFile(File tarFile) {
        this.tarFile = tarFile;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
