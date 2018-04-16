package org.datavaultplatform.common.task;

import org.datavaultplatform.common.event.EventStream;
import java.nio.file.Path;

// Some common properties needed for all jobs

public class Context {

    public enum AESMode {GCM, CBC};

    private Path tempDir;
    private Path metaDir;
    private EventStream eventStream;
    private Boolean chunkingEnabled;
    private Long chunkingByteSize;
    private Boolean encryptionEnabled;
    private AESMode encryptionMode;
    
    public Context() {};
    public Context(Path tempDir, Path metaDir, EventStream eventStream,
                   Boolean chunkingEnabled, Long chunkingByteSize,
                   Boolean encryptionEnabled, AESMode encryptionMode) {
        this.tempDir = tempDir;
        this.metaDir = metaDir;
        this.eventStream = eventStream;
        this.chunkingEnabled = chunkingEnabled;
        this.chunkingByteSize = chunkingByteSize;
        this.encryptionEnabled = encryptionEnabled;
        this.encryptionMode = encryptionMode;
    }

    public Path getTempDir() {
        return tempDir;
    }

    public void setTempDir(Path tempDir) {
        this.tempDir = tempDir;
    }

    public Path getMetaDir() {
        return metaDir;
    }

    public void setMetaDir(Path metaDir) {
        this.metaDir = metaDir;
    }

    public EventStream getEventStream() {
        return eventStream;
    }

    public void setEventStream(EventStream eventStream) {
        this.eventStream = eventStream;
    }
    
    public Boolean isChunkingEnabled() {
        return chunkingEnabled;
    }
    
    public void setChunkingEnabled(Boolean chunkingEnabled) {
        this.chunkingEnabled = chunkingEnabled;
    }
    
    public Long getChunkingByteSize() {
        return chunkingByteSize;
    }
    
    public void setChunkingByteSize(Long chunkingByteSize) {
    	this.chunkingByteSize = chunkingByteSize;
    }

    public void setEncryptionEnabled(Boolean encryptionEnabled) { this.encryptionEnabled = encryptionEnabled; }

    public Boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public void setEncryptionMode(AESMode encryptionMode) {
        this.encryptionMode = encryptionMode;
    }

    public AESMode getEncryptionMode() { return encryptionMode; }
}