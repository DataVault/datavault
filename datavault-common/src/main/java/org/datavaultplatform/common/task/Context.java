package org.datavaultplatform.common.task;

import org.datavaultplatform.common.event.EventSender;
import java.nio.file.Path;
import org.datavaultplatform.common.util.StorageClassNameResolver;

// Some common properties needed for all jobs

public class Context {
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
    private final StorageClassNameResolver classNameResolver;
    private final boolean oldRecompose;

    private final String recomposeDate;

    public Context(Path tempDir, Path metaDir, EventSender eventSender,
                   Boolean chunkingEnabled, Long chunkingByteSize,
                   Boolean encryptionEnabled, AESMode encryptionMode, 
                   String vaultAddress, String vaultToken,
                   String vaultKeyPath, String vaultKeyName,
                   String vaultSslPEMPath, Boolean multipleValidationEnabled
                    ,int noChunkThreads, StorageClassNameResolver classNameResolver,
                   Boolean oldRecompose, String recomposeDate) {
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
        this.classNameResolver = classNameResolver;
        this.oldRecompose = oldRecompose;
        this.recomposeDate = recomposeDate;
    }

    public Path getTempDir() {
        return tempDir;
    }

    public Path getMetaDir() {
        return metaDir;
    }

    public EventSender getEventSender() {
        return eventSender;
    }

    public Boolean isChunkingEnabled() {
        return chunkingEnabled;
    }

    public Long getChunkingByteSize() {
        return chunkingByteSize;
    }

    public Boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public AESMode getEncryptionMode() { return encryptionMode; }
    
    public String getVaultAddress() {
        return vaultAddress;
    }

    public String getVaultToken() {
        return vaultToken;
    }

    public String getVaultKeyPath() {
        return vaultKeyPath;
    }

    public String getVaultKeyName() {
        return vaultKeyName;
    }

    public String getVaultSslPEMPath() {
        return vaultSslPEMPath;
    }

    public Boolean isMultipleValidationEnabled() {
        return multipleValidationEnabled;
    }

    public int getNoChunkThreads() {
        return noChunkThreads;
    }

    public StorageClassNameResolver getStorageClassNameResolver(){ return this.classNameResolver; }

    public Boolean isOldRecompose() {
        return oldRecompose;
    }

    public String getRecomposeDate() {
        return recomposeDate;
    }
}