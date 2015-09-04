package org.datavaultplatform.common.job;

import org.datavaultplatform.common.event.EventStream;

// Some common properties needed for all jobs

public class Context {

    private String archiveDir;
    private String tempDir;
    private String metaDir;
    private EventStream eventStream;
    
    public Context() {};
    public Context(String archiveDir, String tempDir, String metaDir, EventStream eventStream) {
        this.archiveDir = archiveDir;
        this.tempDir = tempDir;
        this.metaDir = metaDir;
        this.eventStream = eventStream;
    }

    public String getArchiveDir() {
        return archiveDir;
    }

    public void setArchiveDir(String archiveDir) {
        this.archiveDir = archiveDir;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getMetaDir() {
        return metaDir;
    }

    public void setMetaDir(String metaDir) {
        this.metaDir = metaDir;
    }

    public EventStream getEventStream() {
        return eventStream;
    }

    public void setEventStream(EventStream eventStream) {
        this.eventStream = eventStream;
    }
}