package org.datavaultplatform.common.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Job;

// A generic task container

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {

    protected String taskClass;
    protected String jobID;
    Map<String, String> properties;
    protected FileStore userFileStore;
    protected ArchiveStore archiveFileStore;

    private boolean isRedeliver;

    public Task() {};
    public Task(Job job,
                Map<String, String> properties,
                FileStore userFileStore,
                ArchiveStore archiveFileStore) {
        this.jobID = job.getID();
        this.taskClass = job.getTaskClass();
        this.properties = properties;
        this.userFileStore = userFileStore;
        this.archiveFileStore = archiveFileStore;
    }

    public String getJobID() { return jobID; }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getTaskClass() {
        return taskClass;
    }

    public void setTaskClass(String taskClass) {
        this.taskClass = taskClass;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public FileStore getUserFileStore() {
        return userFileStore;
    }

    public void setUserFileStore(FileStore userFileStore) {
        this.userFileStore = userFileStore;
    }

    public ArchiveStore getArchiveFileStore() {
        return archiveFileStore;
    }

    public void setArchiveFileStore(ArchiveStore archiveFileStore) {
        this.archiveFileStore = archiveFileStore;
    }

    public boolean isRedeliver() {
        return isRedeliver;
    }

    public void setIsRedeliver(boolean isRedeliver) {
        this.isRedeliver = isRedeliver;
    }
    
    public void performAction(Context context) {}
}