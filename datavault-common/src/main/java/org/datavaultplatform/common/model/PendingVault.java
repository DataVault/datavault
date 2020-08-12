package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.response.VaultInfo;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;

import javax.persistence.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "PendingVault")
@Entity
@Table(name="PendingVaults")
public class PendingVault {
    private static final long ZERO = 0l;
    // Vault Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // Hibernate version
    @Version
    private long version;

    @Column(name = "affirmed", nullable = false)
    private Boolean affirmed = false;

    // Name of the vault
    @Column(name = "name", nullable = false, columnDefinition = "TEXT", length=400)
    private String name;

    // Description of the vault
    @Column(columnDefinition = "TEXT", length = 6000)
    private String description;

    @ManyToOne
    private User user;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getAffirmed() {
        return this.affirmed;
    }

    public void setAffirmed(Boolean affirmed) {
        this.affirmed = affirmed;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }

    public VaultInfo convertToResponse() {
        VaultInfo retVal = new VaultInfo();
        retVal.setID(this.id);
        retVal.setName(this.name);
        retVal.setDescription(this.description);
        retVal.setAffirmed(this.affirmed);
        return retVal;
    }
}
