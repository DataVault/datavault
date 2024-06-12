package org.datavaultplatform.worker.tasks.retrieve;

import java.io.File;

public record RetrieveChunkInfo(int chunkNumber,
                                File chunkFile,
                                String encChunkDigest,
                                byte[] iv,
                                String chunkDigest) {
}
