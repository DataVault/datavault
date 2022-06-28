package org.datavaultplatform.common.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
    public boolean equals(Object obj){
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        DataManager rhs = (DataManager) obj;
        return new EqualsBuilder()
            .append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
            append(id).toHashCode();
    }
}
