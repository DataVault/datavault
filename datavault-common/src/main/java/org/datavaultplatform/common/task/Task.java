package org.datavaultplatform.common.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.List;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.DepositPath;

// A generic task container

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {

    protected String taskClass;
    protected String jobID;
    Map<String, String> properties;
    protected List<DepositPath> depositPaths;
    protected ArchiveStore archiveFileStore;

    private boolean isRedeliver;

    public Task() {};
    public Task(Job job,
                Map<String, String> properties,
                ArchiveStore archiveFileStore,
                List<DepositPath> depositPaths) {
        this.jobID = job.getID();
        this.taskClass = job.getTaskClass();
        this.properties = properties;
        this.archiveFileStore = archiveFileStore;
        this.depositPaths = depositPaths;
    }

    public String getJobID() { return jobID; }

    public void setJobID(String jobID) { this.jobID = jobID; }

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

    public List<DepositPath> getDepositPaths() {
        return depositPaths;
    }

    public void setDepositPaths(List<DepositPath> depositPaths) {
        this.depositPaths = depositPaths;
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