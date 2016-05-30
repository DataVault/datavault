package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import javax.persistence.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Users")
public class User {
    // User Identifier (not a UUID)
    @Id
    @Column(name = "id", unique = true)
    private String id;

    // First name of the user
    @Column(name = "firstname", nullable = false)
    private String firstname;

    // Last name of the user
    @Column(name = "lastname", nullable = false)
    private String lastname;

    // Password
    @Column(name = "password")
    private String password;

    // Password
    @Column(name = "email")
    private String email;

    // Is this user an administrator?
    @Column(name = "admin", nullable = false)
    private Boolean admin;

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

    public Boolean isAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
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
        
        if (!user.getID().equals(getID())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        return getID().hashCode();
    }
}
