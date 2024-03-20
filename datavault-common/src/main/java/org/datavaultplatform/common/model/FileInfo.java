package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class FileInfo {

    private String key;
    private String absolutePath;
    private String name;
    private Boolean isDirectory;
    private Boolean canRead;
    private Boolean canWrite;


    public FileInfo() {}

    public FileInfo(String key, String absolutePath, String fileName, Boolean isDirectory) {
        this.key = key;
        this.absolutePath = absolutePath;
        this.name = fileName;
        this.isDirectory = isDirectory;
    }

    public FileInfo(String key, String absolutePath, String fileName, Boolean isDirectory, Boolean canRead, Boolean canWrite) {
        this.key = key;
        this.absolutePath = absolutePath;
        this.name = fileName;
        this.isDirectory = isDirectory;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDirectory() {
        return isDirectory;
    }

    public void setIsDirectory(Boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public Boolean getCanRead() {
        return canRead;
    }
    public void setCanRead(Boolean canRead) {
        this.canRead = canRead;
    }
  
    public Boolean getCanWrite() {
        return canWrite;
    }
    public void setCanWrite(Boolean canWrite) {
        this.canWrite = canWrite;
    }

    @Override
    public String toString() {
        return "FileInfo [key=" + key + ", absolutePath=" + absolutePath + ", name=" + name + ", isDirectory="
                + isDirectory + ", canRead=" + canRead + ", canWrite=" + canWrite + "]";
    }
}
