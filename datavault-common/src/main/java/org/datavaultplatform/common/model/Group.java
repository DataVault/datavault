package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Groups")
public class Group {
    // User Identifier (not a UUID)
    @Id
    @Column(name = "id", unique = true)
    private String id;

    // Name of the group
    @Column(name = "name", nullable = false)
    private String name;

    // A group may be related to a number of users
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="GroupOwners", joinColumns=@JoinColumn(name="group_id"), inverseJoinColumns=@JoinColumn(name="user_id"))
    private List<User> owners;

    // A group is related to a number of vaults
    @JsonIgnore
    @OneToMany(targetEntity=Vault.class, mappedBy="group", fetch=FetchType.LAZY)
    @OrderBy("creationTime")
    private List<Vault> vaults;

    public Group() {}
    public Group(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public String getID() { return id; }

    public List<User> getOwners() {
        return owners;
    }

    @Override
    public boolean equals(Object other) {
        
        if (this == other) return true;
        
        if (!(other instanceof Group)) {
            return false;
        }
        
        final Group group = (Group)other;
        
        if (!group.getID().equals(getID())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    public boolean hasMember(String userID) {
        for (User user : owners) {
            if (user.getID().equals(userID)) return true;
        }
        return false;
    }
}
