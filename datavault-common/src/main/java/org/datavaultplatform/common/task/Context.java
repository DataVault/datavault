package org.datavaultplatform.common.task;

import lombok.Getter;
import org.datavaultplatform.common.event.EventSender;
import java.nio.file.Path;
import org.datavaultplatform.common.util.StorageClassNameResolver;

// Some common properties needed for all jobs

@Getter
public class Context {

    public static final int DEFAULT_NUMBER_OF_THREADS = 25;

    public enum AESMode {GCM, CBC}

    private final Path tempDir;
    private final Path metaDir;
    private final EventSender eventSender;
    private final boolean chunkingEnabled;
    private final long chunkingByteSize;
    private final boolean encryptionEnabled;
    private final AESMode encryptionMode;
    private final String vaultAddress;
    private final String vaultToken;
    private final String vaultKeyPath;
    private final String vaultKeyName;
    private final String vaultSslPEMPath;
    private final boolean multipleValidationEnabled;
    private final int noChunkThreads;
    private final StorageClassNameResolver storageClassNameResolver;
    private final boolean oldRecompose;

    private final String recomposeDate;

    public Context(Path tempDir, Path metaDir, EventSender eventSender,
                   boolean chunkingEnabled, Long chunkingByteSize,
                   boolean encryptionEnabled, AESMode encryptionMode, 
                   String vaultAddress, String vaultToken,
                   String vaultKeyPath, String vaultKeyName,
                   String vaultSslPEMPath, boolean multipleValidationEnabled
                    ,int noChunkThreads, StorageClassNameResolver storageClassNameResolver,
                   boolean oldRecompose, String recomposeDate) {
        this.tempDir = tempDir;
        this.metaDir = metaDir;
        this.eventSender = eventSender;
        this.chunkingEnabled = chunkingEnabled;
        this.chunkingByteSize = chunkingByteSize;
        this.encryptionEnabled = encryptionEnabled;
        this.encryptionMode = encryptionMode;
        this.vaultAddress = vaultAddress;
        this.vaultToken = vaultToken;
        this.vaultKeyPath = vaultKeyPath;
        this.vaultKeyName = vaultKeyName;
        this.vaultSslPEMPath = vaultSslPEMPath;
        this.multipleValidationEnabled = multipleValidationEnabled;
        this.noChunkThreads = noChunkThreads;
        this.storageClassNameResolver = storageClassNameResolver;
        this.oldRecompose = oldRecompose;
        this.recomposeDate = recomposeDate;
    }

    public int getNoChunkThreads() {
        if (noChunkThreads < 1) {
            return DEFAULT_NUMBER_OF_THREADS;
        } else {
            return noChunkThreads;
        }
    }
}