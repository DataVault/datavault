package org.datavaultplatform.worker.operations;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.tasks.retrieve.ArchiveDeviceInfo;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveChunkInfo;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Getter
@Slf4j
public class ChunkRetrieveTracker implements Callable<File> {
    private final Context context;
    private final String archiveId;
    private final boolean multipleCopies;
    private final List<String> locations;
    private final Device archiveStore;
    private final int chunkNumber;
    private final byte[] iv;
    private final Progress progress;
    private final String chunkDigest;
    private final String encChunkDigest;
    private final File chunkFile;


    public ChunkRetrieveTracker(String archiveId,
                                ArchiveDeviceInfo archiveDeviceInfo,
                                Context context,
                                Progress progress,
                                RetrieveChunkInfo chunkInfo
  ) {
        this.context = context;
        this.archiveId = archiveId;
        this.multipleCopies = archiveDeviceInfo.multiCopy();
        this.locations = archiveDeviceInfo.locations();
        this.archiveStore = archiveDeviceInfo.archiveFs();
        this.progress = progress;
        
        this.chunkNumber = chunkInfo.chunkNumber();
        this.chunkFile = chunkInfo.chunkFile();
        this.encChunkDigest = chunkInfo.encChunkDigest();
        this.iv = chunkInfo.iv();
        this.chunkDigest = chunkInfo.chunkDigest();
    }

    @Override
    public File call() throws Exception {
        String chunkArchiveId = this.getArchiveId() + FileSplitter.CHUNK_SEPARATOR + this.getChunkNumber();

        if (multipleCopies) {
            archiveStore.retrieve(chunkArchiveId, chunkFile, progress, locations);
        } else {
            archiveStore.retrieve(chunkArchiveId, chunkFile, progress);
        }

        RetrieveUtils.decryptAndCheckTarFile("chunk-"+chunkNumber, context, iv, chunkFile, encChunkDigest, chunkDigest);

        log.debug("Chunk download task completed: " + chunkNumber);
        // TODO: SEND EVENT - updating progressed chunk (deposit id,chunkNumber)
        return chunkFile;
    }
}
