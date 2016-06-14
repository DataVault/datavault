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
@Table(name="RetentionPolicies")
public class RetentionPolicy {
    // RetentionPolicy Identifier (not a UUID)
    @Id
    @Column(name = "id", unique = true)
    private String id;

    // Name of the policy
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    // Description of the policy
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // Implementation class of the policy
    @Column(name = "engine", nullable = false, columnDefinition = "TEXT")
    private String engine;

    // Sort order of the policy
    @Column(name = "sort", nullable = false)
    private int sort;

    // A policy is related to a number of vaults
    @JsonIgnore
    @OneToMany(targetEntity=Vault.class, mappedBy="retentionPolicy", fetch=FetchType.LAZY)
    @OrderBy("creationTime")
    private List<Vault> vaults;
    
    public RetentionPolicy() {}
    public RetentionPolicy(String name) {
        this.name = name;
    }

    public String getID() { return id; }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setDescription(String description) { this.description = description; }

    public String getDescription() { return description; }

    public void setEngine(String engine) { this.engine = engine; }

    public String getEngine() { return engine; }

    public void setSort(int sort) { this.sort = sort; }

    public int getSort() { return sort; }
}
