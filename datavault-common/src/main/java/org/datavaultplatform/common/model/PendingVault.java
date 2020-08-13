package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.response.VaultInfo;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;

import javax.persistence.*;
import java.util.Date;

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

    // NOTE: This field is optional. Always remember to check for null when handling it!
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "grantEndDate", nullable = true)
    private Date grantEndDate;

    @ManyToOne
    private RetentionPolicy retentionPolicy;

    @JsonIgnore
    @ManyToOne
    private Group group;

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

    public RetentionPolicy getRetentionPolicy() {
        return this.retentionPolicy;
    }

    public void setRetentionPolicy(RetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
    }

    public void setGrantEndDate(Date grantEndDate) {
        this.grantEndDate = grantEndDate;
    }

    public Date getGrantEndDate() {
        return this.grantEndDate;
    }

    public Group getGroup() { return group; }

    public void setGroup(Group group) {
        this.group = group;
    }

    public VaultInfo convertToResponse() {
        VaultInfo retVal = new VaultInfo();
        retVal.setID(this.id);
        retVal.setName(this.name);
        retVal.setDescription(this.description);
        retVal.setAffirmed(this.affirmed);
        if (this.retentionPolicy != null) {
            retVal.setPolicyID(String.valueOf(this.retentionPolicy.getID()));
        }
        retVal.setGrantEndDate(this.grantEndDate);
        if (this.group != null) {
            retVal.setGroupID(String.valueOf(this.group.getID()));
        }
        return retVal;
    }
}
