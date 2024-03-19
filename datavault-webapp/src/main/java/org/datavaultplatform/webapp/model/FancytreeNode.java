package org.datavaultplatform.webapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// A model for Fancytree JSON data

@JsonIgnoreProperties(ignoreUnknown = true)
public class FancytreeNode {

    private String title;
    private String key;
    private Boolean folder;
    private Boolean lazy;
    private Boolean unselectable;
    private Boolean checkbox;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    public Boolean getFolder() {
        return folder;
    }

    public void setFolder(Boolean folder) {
        this.folder = folder;
    }

    public Boolean getLazy() {
        return lazy;
    }

    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }

    public Boolean getUnselectable() {
        return unselectable;
    }

    public void setUnselectable(Boolean unselectable) {
        this.unselectable = unselectable;
    }

    public Boolean getCheckbox() {
        return checkbox;
    }

    public void setCheckbox(Boolean checkbox) {
        this.checkbox = checkbox;
    }
}
