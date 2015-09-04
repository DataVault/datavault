package org.datavaultplatform.common.job;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import org.datavaultplatform.common.model.FileStore;

// A generic job container

@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {

    String jobClass;
    Map<String, String> properties;
    protected FileStore fileStore;

    public Job() {};
    public Job(String jobClass, Map<String, String> properties, FileStore fileStore) {
        this.jobClass = jobClass;
        this.properties = properties;
        this.fileStore = fileStore;
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