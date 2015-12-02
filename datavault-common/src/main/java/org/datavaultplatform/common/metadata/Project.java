package org.datavaultplatform.common.metadata;

public class Project {
    
    private String id;
    private String url;
    private String name;

    public Project() {}

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}