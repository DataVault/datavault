package org.datavaultplatform.worker.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ArchiveStoresDepositedFiles {

    private final Map<String, ArchiveStoreDepositedFiles> depositedFilesPerArchiveStore = new HashMap<>();

    public synchronized ArchiveStoreDepositedFiles getForArchiveStoreId(String archiveStoreId) {
        return depositedFilesPerArchiveStore.computeIfAbsent(archiveStoreId, ArchiveStoreDepositedFiles::new);
    }

    public void recordArchiveStoreChunkDepositedFile(String archiveStoreId, Optional<Integer> optChunkFile, File storedFile, String archiveId) {
        ArchiveStoreDepositedFiles archiveStoreDepositedFiles = this.getForArchiveStoreId(archiveStoreId);
        if(optChunkFile.isPresent()){
            archiveStoreDepositedFiles.recordChunkDepositedFile(optChunkFile.get(), storedFile, archiveId);
        }else{
            archiveStoreDepositedFiles.recordNonChunkDepositedFile(storedFile, archiveId);
        }
    }
}
