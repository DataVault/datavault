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

    public Group() {}
    public Group(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public String getID() { return id; }

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
}
