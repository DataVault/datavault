package org.datavaultplatform.common.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.Job;

// A generic task container

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {

    protected String jobClass;
    protected String jobID;
    Map<String, String> properties;
    protected FileStore fileStore;

    public Task() {};
    public Task(Job job, Map<String, String> properties, FileStore fileStore) {
        this.jobID = job.getID();
        this.jobClass = job.getJobClass();
        this.properties = properties;
        this.fileStore = fileStore;
    }

    public String getJobID() { return jobID; }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }
    
    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
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