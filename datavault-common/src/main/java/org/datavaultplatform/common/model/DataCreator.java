package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;

import javax.persistence.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "DataCreator")
@Entity
@Table(name="DataCreators")
@NamedEntityGraph(
    name=DataCreator.EG_DATA_CREATOR,
    attributeNodes = @NamedAttributeNode(value = DataCreator_.VAULT, subgraph = "subVault"),
    subgraphs = @NamedSubgraph(
        name="subVault",
        attributeNodes = {
            @NamedAttributeNode(Vault_.DATASET),
            @NamedAttributeNode(Vault_.GROUP),
            @NamedAttributeNode(Vault_.RETENTION_POLICY),
            @NamedAttributeNode(Vault_.USER)
        }))
public class DataCreator {

    public static final String EG_DATA_CREATOR = "eg.DataCreator.1";
    // PDC Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // Hibernate version
    @Version
    private long version;

    // Name of the creator
    @Column(name = "name", nullable = false, columnDefinition = "TEXT", length=400)
    private String name;

    // A creator is related to a number of vaults
    @JsonIgnore
    @ManyToOne
    private Vault vault;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getVersion() {
        return this.version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setVault(Vault vault) {
        this.vault = vault;
    }

    public Vault getVault() {
        return this.vault;
    }
    @Override
    public boolean equals(Object obj){
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        DataCreator rhs = (DataCreator) obj;
        return new EqualsBuilder()
            .append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
            append(id).toHashCode();
    }
}
