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
@Table(name="Policies")
public class Policy {
    // Policy Identifier (not a UUID)
    @Id
    @Column(name = "id", unique = true)
    private String id;

    // Name of the policy
    @Column(name = "name", nullable = false)
    private String name;

    // Sort order of the policy
    @Column(name = "sort", nullable = false)
    private int sort;

    // A policy is related to a number of vaults
    @JsonIgnore
    @OneToMany(targetEntity=Vault.class, mappedBy="policy", fetch=FetchType.LAZY)
    @OrderBy("creationTime")
    private List<Vault> vaults;
    
    public Policy() {}
    public Policy(String name) {
        this.name = name;
    }

    public String getID() { return id; }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setSort(int sort) { this.sort = sort; }

    public int getSort() { return sort; }
}
