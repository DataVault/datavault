package org.datavaultplatform.common.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.datavaultplatform.common.response.BillingInformation;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:31
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Vaults")
public class Vault {

    private static final long ZERO = 0L;
    // Vault Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // Hibernate version
    @Version
    private long version;
    
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    
    // NOTE: This field is optional. Always remember to check for null when handling it!
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "grantEndDate", nullable = true)
    private Date grantEndDate;
    
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "reviewDate", nullable = false)
    private Date reviewDate;
    
    // Name of the vault
    @Column(name = "name", nullable = false, columnDefinition = "TEXT", length=400)
    private String name;

    // Description of the vault
    @Column(columnDefinition = "TEXT", length = 6000)
    private String description;

    // Size of the vault (in bytes)
    private long vaultSize;
    
    // A vault can contain a number of deposits
    @JsonIgnore
    @OneToMany(targetEntity=Deposit.class, mappedBy="vault", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("creationTime")
    private List<Deposit> deposits;

    // A vault can have many reviews
    @JsonIgnore
    @OneToMany(targetEntity=VaultReview.class, mappedBy="vault", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("creationTime")
    private List<VaultReview> vaultReviews;
    
    @JsonIgnore
    @OneToOne(targetEntity=BillingInfo.class, mappedBy="vault", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    private BillingInfo billinginfo;
  
	// A vault can have several data managers
    @JsonIgnore
    @OneToMany(targetEntity=DataManager.class, mappedBy="vault", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("uun")
    private List<DataManager> dataManagers;

    @ManyToOne
    private RetentionPolicy retentionPolicy;

    // Status of the retentionPolicy
    private int retentionPolicyStatus;

    // Date retention policy will expire
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date retentionPolicyExpiry;

    // Date retention policy was last checked
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date retentionPolicyLastChecked;

    @JsonIgnore
    @ManyToOne
    private Group group;

    @ManyToOne
    private User user;
    
    @ManyToOne
    private Dataset dataset;
    
    @Column(name = "projectId", nullable = true)
    private String projectId;
    
 // The raw content from the metadata provider e.g. XML
    @Lob
    @Column(name = "snapshot", nullable = true)
    private String snapshot;

    @JsonIgnore
    @OneToMany(targetEntity=DataCreator.class, mappedBy="vault", fetch=FetchType.LAZY)
    private List<DataCreator> dataCreators;

    @Column(name = "affirmed", nullable = false)
    private Boolean affirmed = false;

    @Column(columnDefinition = "TEXT", length = 6000)
    private String notes;

    // Estimate of Vault size
    @Column(name = "estimate", nullable = true, columnDefinition = "TEXT")
    private PendingVault.Estimate estimate;

    // Name of the pure contact
    @Column(name = "contact", nullable = false, columnDefinition = "TEXT")
    private String contact;

    @Column(name = "pureLink", nullable = false)
    private Boolean pureLink = false;
    
    public Vault() {}
    public Vault(String name) {
        this.name = name;
        this.creationTime = new Date();
        retentionPolicyStatus = RetentionPolicyStatus.UNCHECKED;
    }

    public String getID() { return id; }

    public long getVersion() { return version; }

  public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }
    
    public void setGrantEndDate(Date grantEndDate) {
        this.grantEndDate = grantEndDate;
    }
    
    public Date getGrantEndDate() {
        return grantEndDate;
    }
    
    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    public Date getReviewDate() {
        return reviewDate;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }

    public void setSize(long size) {
        this.vaultSize = size;
    }

    public long getSize() { return vaultSize; }
    
    public List<Deposit> getDeposits() {
        if (deposits == null) return new ArrayList<>();
        return deposits;
    }

    public void setVaultReviews(List<VaultReview> vaultReviews) {
        this.vaultReviews = vaultReviews;
    }

    public List<VaultReview> getVaultReviews() {
        if (vaultReviews == null) return new ArrayList<>();
        return vaultReviews;
    }

    public void addDeposit(Deposit deposit) {
        this.deposits.add(deposit);
    }

    public List<DataManager> getDataManagers() {
        if (dataManagers == null) return new ArrayList<>();
        return dataManagers;
    }

    public DataManager getDataManager(String uun) {
        for(DataManager dataManager : dataManagers){
            if(dataManager.getUUN().equals(uun)){
                return dataManager;
            }
        }
        return null;
    }
    
    public void addDataManager(DataManager dataManager) {
        this.dataManagers.add(dataManager);
    }
    
    public RetentionPolicy getRetentionPolicy() { return retentionPolicy; }

    public void setRetentionPolicy(RetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
    }

    public int getRetentionPolicyStatus() { return retentionPolicyStatus; }

    public void setRetentionPolicyExpiry(Date retentionPolicyExpiry) { this.retentionPolicyExpiry = retentionPolicyExpiry; }

    public Date getRetentionPolicyExpiry() { return retentionPolicyExpiry; }

    public void setRetentionPolicyLastChecked(Date retentionPolicyLastChecked) { this.retentionPolicyLastChecked = retentionPolicyLastChecked; }

    public Date getRetentionPolicyLastChecked() { return retentionPolicyLastChecked; }

    public void setRetentionPolicyStatus(int policyStatus) { this.retentionPolicyStatus = policyStatus; }

    public Group getGroup() { return group; }

    public void setGroup(Group group) {
        this.group = group;
    }

    /* Deprecated:
     * If you're trying to use this function you probably want to use VaultService.getOwner() instead
     */
    @Deprecated
    public User getUser() { return user; }

    @Deprecated
    public void setUser(User user) {
        this.user = user;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
    
    public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
	public String getSnapshot() {
		return this.snapshot;
	}
	
	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}
  
    public BillingInfo getBillinginfo() {
    	
		return billinginfo;
		
	}

	public void setBillinginfo(BillingInfo billinginfo) {
		this.billinginfo = billinginfo;
	}

    public List<DataCreator> getDataCreators() {
        return this.dataCreators;
    }

    public void setDataCreator(List<DataCreator> dataCreators) {
        this.dataCreators = dataCreators;
    }

    public Boolean getAffirmed() {
        return this.affirmed;
    }

    public void setAffirmed(Boolean affirmed) {
        this.affirmed = affirmed;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() { return this.notes; }

    public void setEstimate(PendingVault.Estimate estimate) {
        this.estimate = estimate;
    }

    public PendingVault.Estimate getEstimate() { return this.estimate; }

    public String getContact() {
        return this.contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Boolean getPureLink() {
        return this.pureLink;
    }

    public void setPureLink(Boolean pureLink) {
        this.pureLink = pureLink;
    }

	public VaultInfo convertToResponse() {
        return new VaultInfo(
                id,
                user == null ? null : user.getID(),
                user == null ? null : (user.getFirstname()+" "+user.getLastname()),
                dataset == null ? null : dataset.getID(),
                dataset == null ? null : dataset.getCrisId(),
                dataset == null ? null : dataset.getName(),
                creationTime,
                name,
                description,
                String.valueOf(retentionPolicy.getID()),
                String.valueOf(retentionPolicy.getMinRetentionPeriod()),
                group.getID(),
                vaultSize,
                retentionPolicyStatus,
                retentionPolicyExpiry,
                retentionPolicyLastChecked,
                grantEndDate,
                reviewDate,
                deposits != null? deposits.size() : ZERO,
                projectId);
    }
	public VaultInfo convertToResponseBilling() {		
        return new VaultInfo(
                id,
                user == null ? null : (user.getFirstname()+" "+user.getLastname()),
                creationTime,
                name,               
                vaultSize,               
                reviewDate,              
                billinginfo != null? billinginfo.getAmountToBeBilled() : null,
                billinginfo != null? billinginfo.getAmountBilled(): null,
        		projectId);
    }
	
	public BillingInformation convertToBillingDetailsResponse() {		
		return new BillingInformation(
                billinginfo != null? billinginfo.getID(): null,
                id,
                billinginfo != null? billinginfo.getContactName() : null,
                billinginfo != null? billinginfo.getSchool() : null,
                billinginfo != null? billinginfo.getSubUnit() : null,
                billinginfo != null? billinginfo.getBudgetCode() : null,
                billinginfo != null? billinginfo.getSpecialComments() : null,
                billinginfo != null? billinginfo.getAmountToBeBilled() : null,
                billinginfo != null? billinginfo.getAmountBilled() : null,
                projectId,
                name,
                billinginfo != null? billinginfo.getSliceID() : null,
                billinginfo != null? billinginfo.getProjectTitle() : null,
                billinginfo != null? billinginfo.getBillingType() : null
            );
    }
    
    @Override
    public boolean equals(Object other) {
        
        if (this == other) return true;
        
        if (!(other instanceof Vault)) {
            return false;
        }
        
        final Vault vault = (Vault)other;
        
        if (!vault.getID().equals(getID())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        return getID().hashCode();
    }
}
