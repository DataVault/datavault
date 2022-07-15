package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.response.VaultInfo;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "PendingVault")
@Entity
@Table(name="PendingVaults")
@NamedEntityGraph(name=PendingVault.EG_PENDING_VAULT, attributeNodes =
    {
        @NamedAttributeNode(PendingVault_.GROUP),
        @NamedAttributeNode(PendingVault_.USER),
        @NamedAttributeNode(PendingVault_.RETENTION_POLICY),
        @NamedAttributeNode(PendingVault_.DATA_CREATORS),
    })
public class PendingVault {

    public static final String EG_PENDING_VAULT = "eg.PendingVault.1";
    private static String DEFAULT_PENDING_VAULT_NAME = "*** UNNAMED VAULT ***";
    public enum Estimate {
        UNDER_100GB,
        UNDER_10TB,
        OVER_10TB,
        UNKNOWN
    }

    public enum Billing_Type {
        NA,
        GRANT_FUNDING,
        BUDGET_CODE,
        SLICE,
        ORIG
    }
    // Vault Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    // Hibernate version
    @Version
    private long version;

    @Column(name = "affirmed", nullable = false)
    private Boolean affirmed = false;

    // Name of the vault
    @Column(name = "name", nullable = true, columnDefinition = "TEXT", length=400)
    private String name;

    // Description of the vault
    @Column(columnDefinition = "TEXT", length = 6000)
    private String description;

    // Description of the vault
    @Column(columnDefinition = "TEXT", length = 6000)
    private String notes;

    // NOTE: This field is optional. Always remember to check for null when handling it!
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "grantEndDate", nullable = true)
    private Date grantEndDate;

    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "reviewDate", nullable = true)
    private Date reviewDate;

    // Estimate of Vault size
    @Column(name = "estimate", nullable = true, columnDefinition = "TEXT")
    private Estimate estimate;

    // Billing method
    @Column(name = "billingType", nullable = true, columnDefinition = "TEXT")
    private Billing_Type billingType;

    // Billing method additional info
    @Column(name = "sliceID", nullable = true, columnDefinition = "TEXT")
    private String sliceID;

    // Billing method additional info
    @Column(name = "authoriser", nullable = true, columnDefinition = "TEXT")
    private String authoriser;

    // Billing method additional info
    @Column(name = "schoolOrUnit", nullable = true, columnDefinition = "TEXT")
    private String schoolOrUnit;

    // Billing method additional info
    @Column(name = "subunit", nullable = true, columnDefinition = "TEXT")
    private String subunit;

    // Billing method additional info
    @Column(name = "projectTitle", nullable = true, columnDefinition = "TEXT")
    private String projectTitle;

    // Name of the pure contact
    @Column(name = "contact", nullable = false, columnDefinition = "TEXT")
    private String contact;

    @ManyToOne
    private RetentionPolicy retentionPolicy;

    @JsonIgnore
    @ManyToOne
    private Group group;

    @ManyToOne
    private User user;

    @Transient
    private User owner;

    @Transient
    private User creator;

    @Transient
    private List<User> depositors;

    @Transient
    private List<User> nominatedDataManagers;

    @JsonIgnore
    @OneToMany(targetEntity=PendingDataCreator.class, mappedBy="pendingVault", orphanRemoval = true, cascade = CascadeType.PERSIST, fetch=FetchType.LAZY)
    private final List<PendingDataCreator> dataCreators = new ArrayList<>();

    @Column(name = "confirmed", nullable = false)
    private Boolean confirmed = false;

    @Column(name = "pureLink", nullable = false)
    private Boolean pureLink = false;

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

    public Billing_Type getBillingType() {
        return this.billingType;
    }

    public void setBillingType(Billing_Type billingType) {
        this.billingType = billingType;
    }

    public long getVersion() {
        return this.version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setName(String name) {
        this.name = StringUtils.isBlank(name)? DEFAULT_PENDING_VAULT_NAME : name;
    }

    public String getName() { return name; }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() { return this.notes; }

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

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    public void setEstimate(Estimate estimate) {
        this.estimate = estimate;
    }

    public Estimate getEstimate() { return this.estimate; }

    public Date getReviewDate() {
        return this.reviewDate;
    }

    public Group getGroup() { return group; }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getSliceID() {
        return this.sliceID;
    }

    public void setSliceID(String sliceID) {
        this.sliceID = sliceID;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public User getUser() { return this.user; }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAuthoriser() {
        return this.authoriser;
    }

    public void setAuthoriser(String authoriser) {
        this.authoriser = authoriser;
    }

    public String getSchoolOrUnit() {
        return this.schoolOrUnit;
    }

    public void setSchoolOrUnit(String schoolOrUnit) {
        this.schoolOrUnit = schoolOrUnit;
    }

    public String getSubunit() {
        return this.subunit;
    }

    public void setSubunit(String subunit) {
        this.subunit = subunit;
    }

    /* TODO: rename this column to Project Title (in the original mock up it was called
    project id
     */
    public String getProjectTitle() {
        return this.projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getContact() {
        return this.contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }


    public List<PendingDataCreator> getDataCreators() {
        return this.dataCreators;
    }

    public void setDataCreator(List<PendingDataCreator> dataCreators) {
        synchronized (this.dataCreators) {
            this.dataCreators.clear();
            this.dataCreators.addAll(dataCreators);
        }
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getCreator() {
        return this.creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<User> getDepositors() {
        return depositors;
    }

    public void setDepositors(List<User> depositors) {
        this.depositors = depositors;
    }

    public List<User> getNominatedDataManagers() {
        return nominatedDataManagers;
    }

    public void setNominatedDataManagers(List<User> nominatedDataManagers) {
        this.nominatedDataManagers = nominatedDataManagers;
    }

    public Boolean getPureLink() {
        return this.pureLink;
    }

    public void setPureLink(Boolean pureLink) {
        this.pureLink = pureLink;
    }

    public Boolean getConfirmed() {
        return this.confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }



    public VaultInfo convertToResponse() {
        VaultInfo retVal = new VaultInfo();
        retVal.setID(this.id);
        retVal.setName(this.name);
        retVal.setDescription(this.description);
        retVal.setEstimate(this.estimate);
        retVal.setNotes(this.notes);
        retVal.setAffirmed(this.affirmed);
        if (this.retentionPolicy != null) {
            retVal.setPolicyID(String.valueOf(this.retentionPolicy.getID()));
            retVal.setPolicyLength(String.valueOf(this.retentionPolicy.getMinRetentionPeriod()));
        }
        retVal.setGrantEndDate(this.grantEndDate);
        if (this.group != null) {
            retVal.setGroupID(String.valueOf(this.group.getID()));
        }
        retVal.setReviewDate(this.reviewDate);
        retVal.setBillingType(this.billingType);
        retVal.setSliceID(this.sliceID);
        retVal.setAuthoriser(this.authoriser);
        retVal.setSchoolOrUnit(this.schoolOrUnit);
        retVal.setSubunit(this.subunit);
        //retVal.setProjectId(this.projectID);
        retVal.setProjectTitle(this.projectTitle);
        retVal.setCreationTime(this.creationTime);
        String userId = this.user == null ? null : user.getID();
        retVal.setUserID(userId);
        String userName = user == null ? null : (user.getFirstname()+" "+user.getLastname());
        retVal.setUserName(userName);
        if (this.dataCreators != null) {
            List<String> creators = new ArrayList<>();
            for (PendingDataCreator pdc : this.dataCreators) {
                creators.add(pdc.getName());
            }
            retVal.setDataCreators(creators);
        }

        if (this.owner != null) {
            retVal.setOwnerId(this.owner.getID());
        }
        if (this.creator != null) {
            retVal.setVaultCreatorId(this.creator.getID());
        }
        if (this.nominatedDataManagers != null) {
            List<String> ndmIds = new ArrayList<>();
            for (User ndm : this.nominatedDataManagers) {
                ndmIds.add(ndm.getID());
            }
            retVal.setNominatedDataManagerIds(ndmIds);
        }
        if (this.depositors != null) {
            List<String> deps = new ArrayList<>();
            for (User ndm : this.depositors) {
                deps.add(ndm.getID());
            }
            retVal.setDepositorIds(deps);
        }
        retVal.setContact(this.contact);
        retVal.setPureLink(this.pureLink);
        retVal.setConfirmed(this.confirmed);

        return retVal;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        PendingVault that = (PendingVault) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
