package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "PendingDataCreator")
@Entity
@Table(name="PendingDataCreators")
@NamedEntityGraph(name = PendingDataCreator.EG_PENDING_DATA_CREATOR, attributeNodes =
@NamedAttributeNode(value = PendingDataCreator_.PENDING_VAULT, subgraph = "subPV"),
    subgraphs = @NamedSubgraph(name = "subPV", attributeNodes = {
        @NamedAttributeNode(PendingVault_.GROUP),
        @NamedAttributeNode(PendingVault_.RETENTION_POLICY),
        @NamedAttributeNode(PendingVault_.USER)
    }))
public class PendingDataCreator {

    public static final String EG_PENDING_DATA_CREATOR = "eg.PendingDataCreator.1";

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

    // A creator is related to a number of pending vaults
    @JsonIgnore
    @ManyToOne
    private PendingVault pendingVault;

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

    public void setPendingVault(PendingVault pendingVault) {
        this.pendingVault = pendingVault;
    }

    public PendingVault getPendingVault() {
        return this.pendingVault;
    }
    @Override
    public boolean equals(Object obj){
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        PendingDataCreator rhs = (PendingDataCreator) obj;
        return new EqualsBuilder()
            .append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
            append(id).toHashCode();
    }
}
