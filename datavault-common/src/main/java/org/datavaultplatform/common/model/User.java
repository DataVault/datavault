package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Users")
public class User {
    // User Identifier (not a UUID)
    @Id
    @Column(name = "id", unique = true)
    private String id;
    
    // Name of the policy
    @Column(name = "name", nullable = false)
    private String name;
    
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
    public User(String name) {
        this.name = name;
    }

    public String getID() { return id; }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public List<Vault> getVaults() {
        return vaults;
    }

    public List<FileStore> getFileStores() {
        return fileStores;
    }
}
