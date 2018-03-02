package org.datavaultplatform.common.task;

import org.datavaultplatform.common.event.EventStream;
import java.nio.file.Path;

// Some common properties needed for all jobs

public class Context {
    
    private Path tempDir;
    private Path metaDir;
    private EventStream eventStream;
    private Boolean chunkingEnabled;
    private Long chunkingByteSize;
    
    public Context() {};
    public Context(Path tempDir, Path metaDir, EventStream eventStream, Boolean chunkingEnabled, Long chunkingByteSize) {
        this.tempDir = tempDir;
        this.metaDir = metaDir;
        this.eventStream = eventStream;
        this.chunkingEnabled = chunkingEnabled;
        this.chunkingByteSize = chunkingByteSize;
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
}