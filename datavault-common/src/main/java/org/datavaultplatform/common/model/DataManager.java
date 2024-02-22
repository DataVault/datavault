package org.datavaultplatform.common.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;

import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "DataManager")
@Entity
@Table(name="DataManagers")
@NamedEntityGraph(
    name=DataManager.EG_DATA_MANAGER,
    attributeNodes = @NamedAttributeNode(value = DataManager_.VAULT, subgraph = "subVault"),
    subgraphs = @NamedSubgraph(
        name="subVault",
        attributeNodes = {
            @NamedAttributeNode(Vault_.DATASET),
            @NamedAttributeNode(Vault_.GROUP),
            @NamedAttributeNode(Vault_.RETENTION_POLICY),
            @NamedAttributeNode(Vault_.USER)
        }))
public class DataManager {

    public static final String EG_DATA_MANAGER = "eg.DataManager.1";

    // Data Manager Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;
    
    @ApiObjectField(description = "Data Manager UUN")
    @Column(name = "uun", columnDefinition = "TEXT", length = 36)
    private String uun;
    
    @ManyToOne
    private Vault vault;

    public DataManager() {}
    
    public DataManager(String uun) {
        this.uun = uun;
    }

    public String getID() {
        return id;
    }
    
    public Vault getVault() { return vault; }
    
    public void setVault(Vault vault) {
        this.vault = vault;
    }
    
    public String getUUN() {
        return uun;
    }
    
    public void setUUN(String uun) {
        this.uun = uun;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        DataManager that = (DataManager) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
