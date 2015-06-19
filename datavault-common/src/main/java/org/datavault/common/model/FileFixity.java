package org.datavault.common.model;

public class FileFixity {

    private String file;
    private String fixity;
    
    public FileFixity() {}
    public FileFixity(String file, String Fixity) {
        this.file = file;
        this.fixity = Fixity;
    }
    
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFixity() {
        return fixity;
    }

    public void setFixity(String Fixity) {
        this.fixity = Fixity;
    }
}
