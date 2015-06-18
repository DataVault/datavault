package org.datavault.common.job;

// Some common file properties needed for all jobs

public class Context {

    private String archiveDir;
    private String tempDir;
    private String activeDir;
    
    public Context() {};
    public Context(String archiveDir, String tempDir, String activeDir) {
        this.archiveDir = archiveDir;
        this.tempDir = tempDir;
        this.activeDir = activeDir;
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

    public String getActiveDir() {
        return activeDir;
    }

    public void setActiveDir(String activeDir) {
        this.activeDir = activeDir;
    }
}