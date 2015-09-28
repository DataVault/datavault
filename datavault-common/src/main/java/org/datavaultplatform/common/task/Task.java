package org.datavaultplatform.common.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.Job;

// A generic task container

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {

    protected String taskClass;
    protected String jobID;
    Map<String, String> properties;
    protected FileStore fileStore;

    public Task() {};
    public Task(Job job, Map<String, String> properties, FileStore fileStore) {
        this.jobID = job.getID();
        this.taskClass = job.getTaskClass();
        this.properties = properties;
        this.fileStore = fileStore;
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

    public FileStore getFileStore() {
        return fileStore;
    }

    public void setFileStore(FileStore fileStore) {
        this.fileStore = fileStore;
    }
    
    public void performAction(Context context) {}
}