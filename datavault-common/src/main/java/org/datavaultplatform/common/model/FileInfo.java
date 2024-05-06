package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileInfo {

    private String key;
    private String absolutePath;
    private String name;
    private Boolean isDirectory;

    public FileInfo() {}
    public FileInfo(String key, String absolutePath, String fileName, Boolean isDirectory) {
        this.key = key;
        this.absolutePath = absolutePath;
        this.name = fileName;
        this.isDirectory = isDirectory;
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
}
