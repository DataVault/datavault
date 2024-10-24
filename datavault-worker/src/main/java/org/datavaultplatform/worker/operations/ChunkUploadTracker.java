package org.datavaultplatform.worker.operations;

import java.util.Map;
import java.util.Optional;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.event.UserEventSender;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.StoredChunks;
import org.datavaultplatform.worker.tasks.ArchiveStoreDepositedFiles;
import org.datavaultplatform.worker.tasks.ArchiveStoresDepositedFiles;
import org.springframework.util.Assert;


@Slf4j
public record ChunkUploadTracker(StoredChunks previouslyStoredChunks,
                                 ArchiveStoresDepositedFiles archiveStoresDepositedFiles, int chunkNumber, File chunk,
                                 Map<String, ArchiveStore> archiveStores,
                                 String depositId, UserEventSender userEventSender,
                                 String jobID) implements Callable<HashMap<String, String>> {

    public ChunkUploadTracker {
        Assert.isTrue( archiveStoresDepositedFiles != null, "The archiveStoresDepositedFiles cannot be null");
        Assert.isTrue(chunkNumber > 0, "The chunkNumber must be greater than 0");
        Assert.isTrue(chunk != null, "The chunk File cannot be null");
        Assert.isTrue(archiveStores != null, "The archiveStores cannot be null");
        Assert.isTrue(StringUtils.isNotBlank(depositId), "The depositId cannot be blank");
        Assert.isTrue(userEventSender != null, "The userEventSender cannot be null");
        Assert.isTrue(StringUtils.isNotBlank(jobID), "The jobID cannot be blank");
    }
    
    @Override
    public HashMap<String, String> call() throws Exception {
        log.debug("Copying chunk: [{}]", chunk.getName());

        TaskExecutor<HashMap<String, String>> executor = getTaskExecutor();
        for (String archiveStoreId : archiveStores.keySet()) {
            if(StringUtils.isNotBlank(archiveStoreId)) {
                executeStoreChunk(executor, archiveStoreId);
            } else {
                log.warn("blank archive store id");
            }
        }

        HashMap<String, String> archiveIds = new HashMap<>();
        executor.execute(archiveIds::putAll);
        log.debug("Chunk upload task completed: [{}]", this.chunkNumber);
        return archiveIds;
    }

    protected TaskExecutor<HashMap<String, String>> getTaskExecutor() {
        return new TaskExecutor<>(2, "Device upload failed for[%s]".formatted(chunk.getName()));
    }

    private void executeStoreChunk(TaskExecutor<HashMap<String, String>> executor, String archiveStoreId) {
        if (previouslyStoredChunks.isStored(archiveStoreId, chunkNumber)) {
            log.info("The chunk [{}] has been previously stored in archiveStoreId[{}]", chunkNumber, archiveStoreId);
            return;
        }
        ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
        ArchiveStoreDepositedFiles archiveStoreDepositedFile = archiveStoresDepositedFiles.getForArchiveStoreId(archiveStoreId);
        DeviceTracker dt = new DeviceTracker(archiveStoreDepositedFile, archiveStore, archiveStoreId,
                Optional.of(chunkNumber), depositId,
                jobID, userEventSender,
                chunk);
        log.debug("Creating device task: for[{}/{}] on [{}]", chunk.getName(), chunkNumber, archiveStore.getClass().getSimpleName());
        executor.add(dt);
    }
}
