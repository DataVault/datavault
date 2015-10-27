package org.datavaultplatform.common.model;

public class FileFixity {

    private String file;
    private String fixity;
    private String algorithm;
    private String fileType;
    
    public FileFixity() {}
    public FileFixity(String file, String fixity, String algorithm, String fileType) {
        this.file = file;
        this.fixity = fixity;
        this.algorithm = algorithm;
        this.fileType = fileType;
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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
