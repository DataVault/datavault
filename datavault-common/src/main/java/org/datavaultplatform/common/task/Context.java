package org.datavaultplatform.common.task;

import org.datavaultplatform.common.event.EventStream;
import java.nio.file.Path;

// Some common properties needed for all jobs

public class Context {
    public enum AESMode {GCM, CBC};

    private final Path tempDir;
    private final Path metaDir;
    private final EventStream eventStream;
    private final Boolean chunkingEnabled;
    private final Long chunkingByteSize;
    private final Boolean encryptionEnabled;
    private final AESMode encryptionMode;
    private final String vaultAddress;
    private final String vaultToken;
    private final String vaultKeyPath;
    private final String vaultKeyName;
    private final String vaultSslPEMPath;
    private final Boolean multipleValidationEnabled;
    private final int noChunkThreads;
    
    //ublic Context() {};
    public Context(Path tempDir, Path metaDir, EventStream eventStream,
                   Boolean chunkingEnabled, Long chunkingByteSize,
                   Boolean encryptionEnabled, AESMode encryptionMode, 
                   String vaultAddress, String vaultToken,
                   String vaultKeyPath, String vaultKeyName,
                   String vaultSslPEMPath, Boolean multipleValidationEnabled
                    ,int noChunkThreads) {
        this.tempDir = tempDir;
        this.metaDir = metaDir;
        this.eventStream = eventStream;
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
    }

    public Path getTempDir() {
        return tempDir;
    }

    public Path getMetaDir() {
        return metaDir;
    }

    public EventStream getEventStream() {
        return eventStream;
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
}