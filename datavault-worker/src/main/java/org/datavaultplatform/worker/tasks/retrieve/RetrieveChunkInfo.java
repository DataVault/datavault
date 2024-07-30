package org.datavaultplatform.worker.tasks.retrieve;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.io.DataVaultFileUtils;
import org.datavaultplatform.common.util.Utils;

import java.io.File;

@Slf4j
public record RetrieveChunkInfo(int chunkNumber,
                                File chunkFile,
                                String encChunkDigest,
                                byte[] iv,
                                String chunkDigest) {
    public boolean isRetrieveValid() {
        return isRetrieved() && hasExpectedHash();
    }

    private boolean isRetrieved() {
        try {
            DataVaultFileUtils.checkFileExists(chunkFile, false);
            return true;
        } catch (Exception ex) {
            log.warn("Chunk File does not exist [{}/{}", chunkFile, ex.getMessage());
            return false;
        }
    }

    private boolean hasExpectedHash() {
        try {
            Utils.checkFileHash("chunk-file-check", chunkFile, chunkDigest);
            return true;
        } catch (Exception ex) {
            log.warn("Chunk File does not have expected hash [{}/{}]", chunkFile, ex.getMessage());
            return false;
        }
    }
}
