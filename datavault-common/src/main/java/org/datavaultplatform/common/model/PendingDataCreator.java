package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.response.VaultInfo;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "PendingDataCreator")
@Entity
@Table(name="PendingDataCreators")
public class PendingDataCreator {
    private static final long ZERO = 0l;

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
}
