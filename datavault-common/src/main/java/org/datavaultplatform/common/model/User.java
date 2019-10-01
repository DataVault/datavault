package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Users")
public class User {
    // User Identifier (not a UUID)
    @Id
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // First name of the user
    @Column(name = "firstname", nullable = false, columnDefinition = "TEXT")
    private String firstname;

    // Last name of the user
    @Column(name = "lastname", nullable = false, columnDefinition = "TEXT")
    private String lastname;

    // Password
    @Column(name = "password", columnDefinition = "TEXT")
    private String password;

    // Email
    @Column(name = "email", columnDefinition = "TEXT")
    private String email;

    // Additional properties associated with the user
    @Lob
    private HashMap<String,String> properties;

    // A user is related to a number of vaults
    @JsonIgnore
    @OneToMany(targetEntity=Vault.class, mappedBy="user", fetch=FetchType.LAZY)
    @OrderBy("creationTime")
    private List<Vault> vaults;

    // A user is related to a number of file storage systems
    @JsonIgnore
    @OneToMany(targetEntity=FileStore.class, mappedBy="user", fetch=FetchType.LAZY)
    private List<FileStore> fileStores;
    
    public User() {}

    public void setID(String id) {
        this.id = id;
    }

    public String getID() { return id; }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    public List<Vault> getVaults() {
        return vaults;
    }

    public List<FileStore> getFileStores() {
        return fileStores;
    }
    
    @Override
    public boolean equals(Object other) {
        
        if (this == other) return true;
        
        if (!(other instanceof User)) {
            return false;
        }
        
        final User user = (User)other;

        return user.getID().equals(getID());
    }
    
    @Override
    public int hashCode() {
        return getID().hashCode();
    }
}
