package org.datavaultplatform.worker.tasks;

import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Keep this to track progress 
 */
@Getter
public class ArchiveStoreDepositedFiles {

    private final String archiveStoredId;

    private final Map<Integer, DepositedFile> depositedChunkFiles = new HashMap<>();

    private DepositedFile noChunksDepositedFile;

    public ArchiveStoreDepositedFiles(String archiveStoredId) {
        this.archiveStoredId = archiveStoredId;
    }

    record DepositedFile(Integer chunkNumber, File storedFile, String archiveId) {
    }

    protected void recordNonChunkDepositedFile(File nonChunkFile, String archiveId) {
        this.noChunksDepositedFile = new DepositedFile(null, nonChunkFile, archiveId);
    }

    protected void recordChunkDepositedFile(int chunkNumber, File chunkFile, String archiveId) {
        var depositedChunkFile = new DepositedFile(chunkNumber, chunkFile, archiveId);
        this.depositedChunkFiles.put(depositedChunkFile.chunkNumber, depositedChunkFile);
    }

    public Optional<DepositedFile> getDepositedFileForChunk(int chunkNumber) {
        return Optional.ofNullable(this.depositedChunkFiles.get(chunkNumber));
    }
    
    public String getSingleArchiveId() {
        if (this.noChunksDepositedFile != null) {
            return this.noChunksDepositedFile.archiveId;
        } else {
            return this.depositedChunkFiles.values().stream().map(DepositedFile::archiveId).findFirst().get();
        }
    }

    public void recordChunkDepositedFile(Optional<Integer> optChunkFile, File storedFile, String archiveId) {
        if(optChunkFile.isPresent()){
            recordChunkDepositedFile(optChunkFile.get(), storedFile, archiveId);
        }else{
            recordNonChunkDepositedFile(storedFile, archiveId);
        }
    }

}
