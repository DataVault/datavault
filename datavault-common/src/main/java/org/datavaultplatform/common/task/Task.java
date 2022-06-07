package org.datavaultplatform.common.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.event.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// A generic task container

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {

    protected String taskClass;
    protected String jobID;
    Map<String, String> properties;
    protected List<String> fileStorePaths;
    protected List<String> fileUploadPaths;
    protected List<ArchiveStore> archiveFileStores;
    protected Map<String, Map<String, String>> userFileStoreProperties;
    protected Map<String, String> userFileStoreClasses;
    protected Map<Integer, String> chunkFilesDigest;
    protected byte[] tarIV;
    protected Map<Integer, byte[]> chunksIVs;
    String encTarDigest;
    Map<Integer, String> encChunksDigest;
    private Event lastEvent;

    // For Audit
    private List<HashMap<String, String>> chunksToAudit;
    String[] archiveIds;

    // For restart
    private Map<String, String> restartArchiveIds = new HashMap<>();

    private boolean isRedeliver;

    public Task() {};
    public Task(Job job,
                Map<String, String> properties,
                List<ArchiveStore> archiveFileStores,
                Map<String, Map<String, String>> userFileStoreProperties,
                Map<String, String> userFileStoreClasses,
                List<String> fileStorePaths,
                List<String> fileUploadPaths,
                Map<Integer, String> chunkFilesDigest,
                byte[] tarIV,
                Map<Integer, byte[]> chunksIVs,
                String encTarDigest,
                Map<Integer, String> encChunksDigest,
                Event lastEvent) {
        this.jobID = job.getID();
        this.taskClass = job.getTaskClass();
        this.properties = properties;
        this.archiveFileStores = archiveFileStores;
        this.userFileStoreProperties = userFileStoreProperties;
        this.userFileStoreClasses = userFileStoreClasses;
        this.fileStorePaths = fileStorePaths;
        this.fileUploadPaths = fileUploadPaths;
        this.chunkFilesDigest = chunkFilesDigest;
        this.tarIV = tarIV;
        this.chunksIVs = chunksIVs;
        this.encTarDigest = encTarDigest;
        this.encChunksDigest = encChunksDigest;
        this.lastEvent = lastEvent;
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

    public List<String> getFileStorePaths() {
        return fileStorePaths;
    }

    public void setFileStorePaths(List<String> fileStorePaths) {
        this.fileStorePaths = fileStorePaths;
    }

    public List<String> getFileUploadPaths() {
        return fileUploadPaths;
    }

    public void setFileUploadPaths(List<String> fileUploadPaths) {
        this.fileUploadPaths = fileUploadPaths;
    }

    public List<ArchiveStore> getArchiveFileStores() {
        return archiveFileStores;
    }

    public void setArchiveFileStores(List<ArchiveStore> archiveFileStores) {
        this.archiveFileStores = archiveFileStores;
    }

    public Map<String, Map<String, String>> getUserFileStoreProperties() {
        return userFileStoreProperties;
    }

    public void setUserFileStoreProperties(Map<String, Map<String, String>> userFileStoreProperties) {
        this.userFileStoreProperties = userFileStoreProperties;
    }
    
    public Map<String, String> getUserFileStoreClasses() {
        return userFileStoreClasses;
    }

    public void setUserFileStoreClasses(Map<String, String> userFileStoreClasses) {
        this.userFileStoreClasses = userFileStoreClasses;
    }

    public boolean isRedeliver() {
        return isRedeliver;
    }

    public void setIsRedeliver(boolean isRedeliver) {
        this.isRedeliver = isRedeliver;
    }
    
    public Map<Integer, String> getChunkFilesDigest() {
        return chunkFilesDigest;
    }
    
    public void setChunkFilesDigest(Map<Integer, String> chunkFilesDigest) {
        this.chunkFilesDigest = chunkFilesDigest;
    }
    
    public byte[] getTarIV() {
        return tarIV;
    }
    
    public void setTarIV(byte[] tarIV) {
        this.tarIV = tarIV;
    }
    
    public Map<Integer, byte[]> getChunksIVs() {
        return chunksIVs;
    }
    
    public void setChunksIVs(Map<Integer, byte[]> chunksIVs) {
        this.chunksIVs = chunksIVs;
    }
    
    public String getEncTarDigest() {
        return encTarDigest;
    }
    
    public void setEncTarDigest(String encTarDigest) {
        this.encTarDigest = encTarDigest;
    }
    
    public Map<Integer, String> getEncChunksDigest() {
        return encChunksDigest;
    }
    
    public void setEncChunksDigest(Map<Integer, String> encChunksDigest) {
        this.encChunksDigest = encChunksDigest;
    }

    public Event getLastEvent() { return lastEvent; }

    public void setLastEvent(Event lastEvent) { this.lastEvent = lastEvent; }
    
    public void performAction(Context context) {}

    public List<HashMap<String, String>> getChunksToAudit() {
        return chunksToAudit;
    }

    public void setChunksToAudit(List<HashMap<String, String>> chunksToAudit) {
        this.chunksToAudit = chunksToAudit;
    }

    public String[] getArchiveIds() {
        return this.archiveIds;
    }

    public void setArchiveIds(String[] archiveIds) {
        this.archiveIds = archiveIds;
    }

    public Map<String, String> getRestartArchiveIds() {
        return this.restartArchiveIds;
    }

    public void setRestartArchiveIds(Map<String, String> restartArchiveIds) {
        this.restartArchiveIds = restartArchiveIds;
    }
}