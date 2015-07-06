package org.datavault.common.model;

public class Restore {
    
    String restorePath;
    
    // Additional properties might go here - e.g. format
    
    public Restore() {};

    public String getRestorePath() {
        return restorePath;
    }

    public void setRestorePath(String restorePath) {
        this.restorePath = restorePath;
    }
}
