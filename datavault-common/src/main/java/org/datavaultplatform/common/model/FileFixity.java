package org.datavaultplatform.common.model;

public class FileFixity {

    private String file;
    private String fixity;
    private String algorithm;
    
    public FileFixity() {}
    public FileFixity(String file, String fixity, String algorithm) {
        this.file = file;
        this.fixity = fixity;
        this.algorithm = algorithm;
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

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
