package org.datavaultplatform.common.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

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

	private static final long ZERO = 0l;
	private static final BigDecimal ZERO_BIGDECIMAL = new BigDecimal("000.00");
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
    
    public Vault() {}
    public Vault(String name) {
        this.name = name;
        this.creationTime = new Date();
        retentionPolicyStatus = RetentionPolicyStatus.UNCHECKED;
    }

    public String getID() { return id; }

    public long getVersion() { return version; };
    
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
        if (deposits == null) return new ArrayList<Deposit>();
        return deposits;
    }

    public void addDeposit(Deposit deposit) {
        this.deposits.add(deposit);
    }

    public List<DataManager> getDataManagers() {
        if (dataManagers == null) return new ArrayList<DataManager>();
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
    
    public User getUser() { return user; }
    
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
  
    public BillingInfo getBillinginfo() {
    	
		return billinginfo;
		
	}
	public void setBillinginfo(BillingInfo billinginfo) {
		this.billinginfo = billinginfo;
	}
	public VaultInfo convertToResponse() {
        return new VaultInfo(
                id,
                user.getID(),
                user.getFirstname()+" "+user.getLastname(),
                dataset.getID(),
                dataset.getName(),
                creationTime,
                name,
                description,
                String.valueOf(retentionPolicy.getID()),
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
                user.getFirstname()+" "+user.getLastname(),                
                creationTime,
                name,               
                vaultSize,               
                reviewDate,              
                billinginfo != null? billinginfo.getAmountToBeBilled() : null,
                billinginfo != null? billinginfo.getAmountBilled(): null,
        		projectId);
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
