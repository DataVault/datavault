package org.datavaultplatform.worker.operations;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.common.event.deposit.StartCopyUpload;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.worker.tasks.ArchiveStoreDepositedFiles;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Callable;

@Slf4j
@Getter
public class DeviceTracker implements Callable<HashMap<String, String>> {

    private final String jobID;
    private final String depositId;
    private final File tarFile;
    private final UserEventSender userEventSender;
    private final Optional<Integer> optChunkNumber;
    private final String archiveStoreId;
    private final ArchiveStore archiveStore;
    private final ArchiveStoreDepositedFiles archiveStoreDepositedFiles;

    public DeviceTracker(ArchiveStoreDepositedFiles archiveStoreDepositedFiles, ArchiveStore archiveStore, String archiveStoreId,
                         Optional<Integer> optChunkNumber, String depositId,
                         String jobID, UserEventSender userEventSender,
                         File tarFile) {
        this.archiveStore = archiveStore;
        this.archiveStoreId = archiveStoreId;
        this.optChunkNumber = optChunkNumber;
        this.depositId = depositId;
        this.jobID = jobID;
        this.userEventSender = userEventSender;
        this.tarFile = tarFile;
        this.archiveStoreDepositedFiles = archiveStoreDepositedFiles;
    }

    @Override
    public HashMap<String, String> call() throws Exception {
        HashMap<String, String> archiveIds = new HashMap<>();
        // Progress tracking (threaded)
        Progress progress = new Progress();
        ProgressTracker tracker = new ProgressTracker(progress, jobID, depositId, tarFile.length(), userEventSender);
        Thread trackerThread = new Thread(tracker);
        trackerThread.start();
        final String depositIdWithOptionalChunkNumber = optChunkNumber
                .map(chunkNum -> depositId + "." + chunkNum)
                .orElse(depositId);
        String archiveId;
        // kick off new task for each device ( we may already have kicked off x threads for chunks)
        try {
            Device device = (Device) archiveStore;
            userEventSender.send(new StartCopyUpload(jobID, depositId, device.name, optChunkNumber));
            if (device.hasDepositIdStorageKey()) {
                archiveId = device.store(depositIdWithOptionalChunkNumber, tarFile, progress);
            } else {
                archiveId = device.store("/", tarFile, progress);
            }
            // TODO : ArchiveStoreDepositedFiles will probably end up being INPUT and OUTPUT - might use read only interface
            archiveStoreDepositedFiles.recordChunkDepositedFile(optChunkNumber, tarFile, archiveId);
            Integer chunkNum = optChunkNumber.orElse(null);
            String eventType = device.name;
            userEventSender.send(new CompleteCopyUpload(depositId, jobID, eventType, chunkNum, archiveStoreId, archiveId));
        } finally {
            // Stop the tracking thread
            tracker.stop();
            trackerThread.join();
        }


        log.info("Copied: " + progress.getDirCount() + " directories, " + progress.getFileCount() + " files, " + progress.getByteCount() + " bytes");
        // wait for all 3 to finish

        if (optChunkNumber.isPresent() ) {
            if(!archiveIds.containsKey(archiveStoreId)) {
                archiveId = formatArchiveId(archiveId);
                
                archiveIds.put(archiveStoreId, archiveId);
                log.debug("Add to archiveIds: key: " + archiveStoreId + " ,value:" + archiveId);
            }
        } else {
            log.debug("Add to archiveIds: key: " + archiveStoreId + " ,value:" + archiveId);
            archiveIds.put(archiveStoreId, archiveId);
        }
        log.debug("archiveIds: " + archiveIds);
        log.debug("DeviceTracker task completed: " + archiveId);
        return archiveIds;
    }

    public static String formatArchiveId(String archiveId) {
        log.info("ArchiveId is: " + archiveId);
        String separator = FileSplitter.CHUNK_SEPARATOR;
        log.info("Separator is: " + separator);
        int beginIndex = archiveId.lastIndexOf(separator);
        log.info("BeginIndex is: " + beginIndex);
        String result = archiveId.substring(0, beginIndex);
        return result;
    }
}
