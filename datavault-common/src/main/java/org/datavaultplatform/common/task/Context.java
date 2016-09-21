package org.datavaultplatform.common.task;

import org.datavaultplatform.common.event.EventStream;
import java.nio.file.Path;

// Some common properties needed for all jobs

public class Context {

    private Path tempDir;
    private Path metaDir;
    private EventStream eventStream;
    
    public Context() {};
    public Context(Path tempDir, Path metaDir, EventStream eventStream) {
        this.tempDir = tempDir;
        this.metaDir = metaDir;
        this.eventStream = eventStream;
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
}