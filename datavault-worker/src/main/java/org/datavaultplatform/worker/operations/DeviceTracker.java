package org.datavaultplatform.worker.operations;

import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.common.event.deposit.StartCopyUpload;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;

@Slf4j
public class DeviceTracker implements Callable<HashMap<String, String>> {

    private final String jobID;
    private final String depositId;
    private final File tarFile;
    private final EventSender eventSender;
    private final int chunkNumber;
    private final String archiveStoreId;
    private final ArchiveStore archiveStore;
    private final String userID;

    public DeviceTracker(ArchiveStore archiveStore, String archiveStoreId,
        int chunkNumber, String depositId,
        String jobID, EventSender eventSender,
        File tarFile, String userID) {
        this.archiveStore = archiveStore;
        this.archiveStoreId = archiveStoreId;
        this.chunkNumber = chunkNumber;
        this.depositId = depositId;
        this.jobID = jobID;
        this.eventSender = eventSender;
        this.tarFile = tarFile;
        this.userID = userID;
    }

    @Override
    public HashMap<String, String> call() throws Exception {
        HashMap<String, String> archiveIds = new HashMap<>();
        // Progress tracking (threaded)
        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, this.jobID, this.depositId, this.tarFile.length(), this.eventSender);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();
        String depId = this.depositId;
        if (this.chunkNumber > 0) {
            depId = depId + "." + this.chunkNumber;
        }
        String archiveId;
        // kick off new task for each device ( we may already have kicked off x threads for chunks)
        try {
            this.eventSender.send(new StartCopyUpload(this.jobID, this.depositId, ((Device) this.archiveStore).name, this.chunkNumber).withUserId(this.userID));
            if (((Device)this.archiveStore).hasDepositIdStorageKey()) {
                archiveId = ((Device) this.archiveStore).store(depId, this.tarFile, progress);
            } else {
                archiveId = ((Device) this.archiveStore).store("/", this.tarFile, progress);
            }
            this.eventSender.send(new CompleteCopyUpload(this.jobID, this.depositId, ((Device) this.archiveStore).name, this.chunkNumber).withUserId(this.userID));
        } finally {
            // Stop the tracking thread
            tracker.stop();
            trackerThread.join();
        }

        log.info("Copied: " + progress.dirCount + " directories, " + progress.fileCount + " files, " + progress.byteCount + " bytes");
        // wait for all 3 to finish

        if (this.chunkNumber > 0 && archiveIds.get(this.archiveStoreId) == null) {
            log.info("ArchiveId is: " + archiveId);
            String separator = FileSplitter.CHUNK_SEPARATOR;
            log.info("Separator is: " + separator);
            int beginIndex = archiveId.lastIndexOf(separator);
            log.info("BeginIndex is: " + beginIndex);
            archiveId = archiveId.substring(0, beginIndex);
            log.debug("Add to archiveIds: key: "+this.archiveStoreId+" ,value:"+archiveId);
            archiveIds.put(archiveStoreId, archiveId);
            log.debug("archiveIds: "+archiveIds);
        } else if(this.chunkNumber == 0) {
            archiveIds.put(archiveStoreId, archiveId);
        }
        log.debug("DeviceTracker task completed: " + archiveId);
        return archiveIds;
    }

    public String getJobID() {
        return this.jobID;
    }

    public String getDepositId() {
        return this.depositId;
    }

    public File getTarFile() {
        return this.tarFile;
    }

    public EventSender getEventSender() {
        return this.eventSender;
    }

    public int getChunkNumber() {
        return this.chunkNumber;
    }

    public String getArchiveStoreId() {
        return this.archiveStoreId;
    }

    public ArchiveStore getArchiveStore() {
        return this.archiveStore;
    }

    public String getUserID() {
        return this.userID;
    }

}
