package org.datavaultplatform.worker.tasks.deposit;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.event.deposit.UploadComplete;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.worker.operations.ChunkUploadTracker;
import org.datavaultplatform.worker.operations.DeviceTracker;
import org.datavaultplatform.worker.tasks.ArchiveStoresDepositedFiles;
import org.datavaultplatform.worker.tasks.PackageHelper;
import org.springframework.util.Assert;

import java.io.File;
import java.util.*;

import static org.datavaultplatform.worker.tasks.deposit.DepositState.DepositState04Verifying;

@Slf4j
public class DepositArchiveStoresUploader extends DepositSupport {
    
    private final ArchiveStoreContext archiveStoreContext;
    private final ArchiveStoresDepositedFiles archiveStoresDepositedFiles;

    public DepositArchiveStoresUploader(String userID, String jobID, String depositId, UserEventSender userEventSender, String bagID, Context context, Event lastEvent, Map<String, String> properties,
                                        ArchiveStoreContext archiveStoreContext) {
        super(userID, jobID, depositId, userEventSender, bagID, context, lastEvent, properties);
        this.archiveStoreContext = archiveStoreContext;
        this.archiveStoresDepositedFiles = new ArchiveStoresDepositedFiles();
    }

    public ArchiveStoresDepositedFiles uploadToStorage(PackageHelper packageHelper, List<Integer> previouslyStoredChunks) throws Exception {
        Assert.isTrue(packageHelper != null, "The packageHelper cannot be null");
        Assert.isTrue(previouslyStoredChunks != null, "The previouslyStoredChunks cannot be null");

        log.debug("Uploading to storage.");

        if (context.isChunkingEnabled()) {
            TaskExecutor<HashMap<String, String>> executor = getTaskExecutor(getNumberOfChunkThreads(), "Chunk upload failed.");

            packageHelper.getChunkHelpers().forEach((chunkNumber, chunkHelper) -> {
                
                if (!previouslyStoredChunks.contains(chunkNumber)) {
                    File chunkFile = chunkHelper.getChunkFile();
                    ChunkUploadTracker cut = new ChunkUploadTracker(archiveStoresDepositedFiles, chunkNumber, chunkFile,
                            archiveStoreContext.getArchiveStores(), depositId,
                            userEventSender, jobID);
                    log.debug("Creating chunk upload task: [{}]", chunkNumber);
                    executor.add(cut);
                } else {
                    log.info("The chunkNumber[{}] has been uploaded already.", chunkNumber);
                }
            });

            executor.execute(result -> {
                log.debug("returned archiveIds: [{}]", result);
                archiveStoreContext.addArchiveIds(result);
                log.debug("archiveIds: {}", archiveStoreContext.getArchiveIds());
            });

            log.debug("final archiveIds: {}", archiveStoreContext.getArchiveIds());
        } else {
            copyToArchiveStorage(packageHelper.getTarFile());
        }
        // makes more sense to send the UploadComplete AFTER the final UpdateProgress
        sendEvent(new UpdateProgress(jobID, depositId));
        sendEvent(new UploadComplete(jobID, depositId, archiveStoreContext.getArchiveIds()).withNextState(DepositState04Verifying.getStateNumber()));
        return archiveStoresDepositedFiles;
    }

    /**
     * @param tarFile
     * @throws Exception
     */
    private void copyToArchiveStorage(File tarFile) throws Exception {
        copyToArchiveStorage(tarFile, Optional.empty());
    }

    /**
     * @param tarFile
     * @throws Exception
     */
    private void copyToArchiveStorage(File tarFile, Optional<Integer> optChunkNumber) throws Exception {
        TaskExecutor<HashMap<String, String>> executor = getTaskExecutor(2, "Device upload failed.");
        Map<String,ArchiveStore> archiveStores = archiveStoreContext.getArchiveStores();
        for (String archiveStoreId : archiveStores.keySet()) {
            log.debug("ArchiveStoreId: [{}]", archiveStoreId);
            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
            var archiveStoreDepositedFiles = archiveStoresDepositedFiles.getForArchiveStoreId(archiveStoreId);
            DeviceTracker dt = new DeviceTracker(archiveStoreDepositedFiles, archiveStore, archiveStoreId,
                    optChunkNumber, getDepositId(),
                    getJobID(), userEventSender,
                    tarFile);
            log.debug("Creating device task: [{}]", archiveStore.getClass());
            executor.add(dt);
        }
        executor.execute(archiveStoreContext::addArchiveIds);
    }

    /*
     * This method is used to create a TaskExecutor - helps test this class with Mockito Spies.
     */
    protected TaskExecutor<HashMap<String,String>> getTaskExecutor(int numberOfThreads, String errorLabel) {
        return new TaskExecutor<>(numberOfThreads, errorLabel);
    }
}
