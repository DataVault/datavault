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
    private final ContextVaultInfo vaultInfo;
    private final boolean multipleValidationEnabled;
    private final int noChunkThreads;
    private final StorageClassNameResolver storageClassNameResolver;
    private final boolean oldRecompose;

    private final String recomposeDate;
    private final TaskStageEventListener taskStageEventListener;

    public Context(Path tempDir, Path metaDir, EventSender eventSender,
                   boolean chunkingEnabled, Long chunkingByteSize,
                   boolean encryptionEnabled, AESMode encryptionMode, 
                   ContextVaultInfo vaultInfo, boolean multipleValidationEnabled,
                   int noChunkThreads, StorageClassNameResolver storageClassNameResolver,
                   boolean oldRecompose, String recomposeDate,
                   TaskStageEventListener taskStageEventListener) {
        this.tempDir = tempDir;
        this.metaDir = metaDir;
        this.eventSender = eventSender;
        this.chunkingEnabled = chunkingEnabled;
        this.chunkingByteSize = chunkingByteSize;
        this.encryptionEnabled = encryptionEnabled;
        this.encryptionMode = encryptionMode;
        this.vaultInfo = vaultInfo;
        this.multipleValidationEnabled = multipleValidationEnabled;
        this.noChunkThreads = noChunkThreads;
        this.storageClassNameResolver = storageClassNameResolver;
        this.oldRecompose = oldRecompose;
        this.recomposeDate = recomposeDate;
        this.taskStageEventListener = taskStageEventListener;
    }

    public int getNoChunkThreads() {
        if (noChunkThreads < 1) {
            return DEFAULT_NUMBER_OF_THREADS;
        } else {
            return noChunkThreads;
        }
    }
}