package org.datavaultplatform.worker.operations;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.tasks.RetrievedChunkFileChecker;
import org.datavaultplatform.worker.tasks.retrieve.ArchiveDeviceInfo;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveChunkInfo;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Getter
@Slf4j
public class ChunkRetrieveTracker implements Callable<File> {
    private final Context context;
    private final String archiveId;
    private final boolean multipleCopies;
    private final List<String> locations;
    private final Device archiveStore;
    private final Progress progress;
    private final Consumer<Integer> chunkStoredEventSender;
    private final RetrievedChunkFileChecker chunkFileChecker;
    private final RetrieveChunkInfo chunkInfo;


    public ChunkRetrieveTracker(String archiveId,
                                ArchiveDeviceInfo archiveDeviceInfo,
                                Context context,
                                Progress progress,
                                RetrieveChunkInfo chunkInfo,
                                Consumer<Integer> chunkStoredEventSender,
                                RetrievedChunkFileChecker chunkFileChecker
  ) {
        this.context = context;
        this.archiveId = archiveId;
        this.multipleCopies = archiveDeviceInfo.multiCopy();
        this.locations = archiveDeviceInfo.locations();
        this.archiveStore = archiveDeviceInfo.archiveFs();
        this.progress = progress;
        
        this.chunkInfo = chunkInfo;
        this.chunkStoredEventSender = chunkStoredEventSender;
        this.chunkFileChecker = chunkFileChecker;
    }

    @Override
    public File call() throws Exception {

        int chunkNumber = chunkInfo.chunkNumber();
        File chunkFile = chunkInfo.chunkFile();
        
        String chunkArchiveId = this.getArchiveId() + FileSplitter.CHUNK_SEPARATOR + this.chunkInfo.chunkNumber();

        if (multipleCopies) {
            archiveStore.retrieve(chunkArchiveId, chunkFile, progress, locations);
        } else {
            archiveStore.retrieve(chunkArchiveId, chunkFile, progress);
        }

        chunkFileChecker.decryptAndCheckTarFile(context, chunkInfo);
 
        chunkStoredEventSender.accept(chunkNumber);
        log.debug("Chunk download task completed: " + chunkNumber);
        return chunkFile;
    }
}
