package org.datavaultplatform.common.storage;

// A container for passing credentials

public class Auth {
    
    private String username;
    private String password;
    private String certificate;

    public Auth(String username, String password, String certificate) {
        this.username = username;
        this.password = password;
        this.certificate = certificate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}