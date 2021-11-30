package org.datavaultplatform.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "VaultInfo")
public class VaultInfo {
    
    @ApiObjectField(description = "The unique identifier for this vault")
    private String id;
    
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @ApiObjectField(description = "The date and time when this vault was created")
    private Date creationTime;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @ApiObjectField(description = "The date and time when the policy will expire")
    private Date policyExpiry;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @ApiObjectField(description = "The date and time when the policy check was last carried out")
    private Date policyLastChecked;

    @ApiObjectField(description = "The name of this vault")
    private String name;
    
    @ApiObjectField(description = "The date and time when this vault was created")
    private String description;

    @ApiObjectField(description = "Estimate of vault size")
    private PendingVault.Estimate estimate;

    @ApiObjectField(description = "How we are billing")
    private PendingVault.Billing_Type billingType;

    @ApiObjectField(description = "Notes regarding data retention")
    private String notes;
    
    @ApiObjectField(description = "The policy that applies to this vault")
    private String policyID;

    @ApiObjectField(description = "The length of the policy that applies to this vault")
    private String policyLength;
    
    @ApiObjectField(description = "The group which is related to this vault")
    private String groupID;
    
    @ApiObjectField(description = "The user UUN who owns this vault")
    private String userID;
    
    @ApiObjectField(description = "The user name who owns this vault")
    private String userName;

    @ApiObjectField(description = "A reference to an external metadata record that describes this vault")
    private String datasetID;

    @ApiObjectField(description = "Another reference to an external metadata record that describes this vault")
    private String crisID;

    @ApiObjectField(description = "The name of the external metadata record that describes this vault")
    private String datasetName;

    @ApiObjectField(description = "The size of this vault in bytes")
    private long vaultSize;
    
    @ApiObjectField(description = "The status of the vault policy")
    private int policyStatus;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @ApiObjectField(description = "Define the minimum of time the archive will be kept")
    private Date grantEndDate;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @ApiObjectField(description = "The date by which the vault should be reviewed for decision as to whether it should be deleted or whether there are funds available to support continued storage")
    private Date reviewDate;
    
    @ApiObjectField(description = "Number of Deposits in a vault")
    private long numberOfDeposits;
    
    @ApiObjectField(description = "Project Id from Pure")
    private String projectId;

    @ApiObjectField(description = "Slice ID from erm somewhere")
    private String sliceID;

    @ApiObjectField(description = "Authoriser of the billing")
    private String authoriser;

    @ApiObjectField(description = "School / Unit to be billed")
    private String schoolOrUnit;

    @ApiObjectField(description = "Subunit to be billed")
    private String subunit;

    @ApiObjectField(description = "Project Title (from Grant billing fieldset)")
    private String projectTitle;
    
    @ApiObjectField(description = "Amount to be Billed")
    private BigDecimal amountToBeBilled;
    
    @ApiObjectField(description = "Amount Billed")
    private BigDecimal amountBilled;
    
    @ApiObjectField(description = "Sum of vaults size for a projectId")
    private long projectSize;

    @ApiObjectField(description = "Did the user accept the various rules on the create vault intro page")
    private Boolean affirmed = false;

    @ApiObjectField(description = "Did the user accept the Pure Link rule on the summary page")
    private Boolean pureLink = false;

    @ApiObjectField(description = "Did the user confirm the pending vault yet")
    private Boolean confirmed = false;

    @ApiObjectField(description = "Pure Contact")
    private String contact;

    @ApiObjectField(description = "Pending / Vault Owner ID")
    private String ownerId;

    @ApiObjectField(description = "Vault Owner Name")
    private String ownerName;

    @ApiObjectField(description = "Pending Vault Creator ID")
    private String vaultCreatorId;
    
    @ApiObjectField(description = "Data Creators")
    private List<String> creators;

    @ApiObjectField(description = "Nominated Data Managers")
    private List<String> nominatedDataManagerIds;

    @ApiObjectField(description = "Depositors")
    private List<String> depositorIds;

    public VaultInfo() { }

    public VaultInfo(String id, String userID, String userName, String datasetID, String crisID, String datasetName,
                     Date creationTime, String name, String description, String policyID, String policyLength, String groupID,
                     long vaultSize, int policyStatus, Date policyExpiry, Date policyLastChecked, Date grantEndDate,
                     Date reviewDate, long numberOfDeposits, String projectId) {
        this.id = id;
        this.userID = userID;
        this.userName = userName;
        this.datasetID = datasetID;
        this.datasetName = datasetName;
        this.creationTime = creationTime;
        this.name = name;
        this.description = description;
        this.policyID = policyID;
        this.policyLength = policyLength;
        this.groupID = groupID;
        this.vaultSize = vaultSize;
        this.policyStatus = policyStatus;
        this.policyExpiry = policyExpiry;
        this.policyLastChecked = policyLastChecked;
        this.grantEndDate = grantEndDate;
        this.reviewDate = reviewDate;
        this.numberOfDeposits = numberOfDeposits;
        this.projectId = projectId;
        this.crisID = crisID;
    }
    
    public VaultInfo(String id,String userName, Date creationTime, String name,  
    		long vaultSize, Date reviewDate,BigDecimal amountToBeBilled,BigDecimal amountBilled, String projectId) {
        this.id = id;        
        this.userName = userName;    
        this.creationTime = creationTime;
        this.name = name;        
        this.vaultSize = vaultSize;        
        this.reviewDate = reviewDate;
        this.amountToBeBilled = amountToBeBilled;
        this.amountBilled = amountBilled;
        this.projectId = projectId;
    }

	public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDatasetID() {
        return datasetID;
    }

    public void setDatasetID(String datasetID) {
        this.datasetID = datasetID;
    }

    public String getCrisID() {
        return crisID;
    }

    public void setCrisID(String crisID) {
        this.crisID = crisID;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public PendingVault.Estimate getEstimate() {
        return this.estimate;
    }

    public void setEstimate(PendingVault.Estimate estimate) {
        this.estimate = estimate;
    }

    public PendingVault.Billing_Type getBillingType() {
        return this.billingType;
    }

    public void setBillingType(PendingVault.Billing_Type billingType) {
        this.billingType = billingType;
    }

    public String getPolicyID() {
        return policyID;
    }

    public void setPolicyID(String policyID) {
        this.policyID = policyID;
    }

    public String getPolicyLength() {
        return policyLength;
    }

    public void setPolicyLength(String policyLength) {
        this.policyLength = policyLength;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public long getVaultSize() {
        return vaultSize;
    }

    public void setVaultSize(long vaultSize) {
        this.vaultSize = vaultSize;
    }

    public String getSizeStr() {
        if ( vaultSize == 0 ){
            return "No deposits yet";
        }
        double s = vaultSize;
        double gibibytes = s/1024/1024/1024;
        DecimalFormat df = new DecimalFormat("#");
        String dx = df.format(gibibytes);
        if(dx.equals("0")){
            return "< 1 GB";
        }
        return dx + " GB";
    }

    public int getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(int policyStatus) {
        this.policyStatus = policyStatus;
    }

    public Date getPolicyLastChecked() {
        return policyLastChecked;
    }

    public void setPolicyLastChecked(Date policyLastChecked) {
        this.policyLastChecked = policyLastChecked;
    }

    public Date getPolicyExpiry() {
        return policyExpiry;
    }

    public void setPolicyExpiry(Date policyExpiry) {
        this.policyExpiry = policyExpiry;
    }

    public String getPolicyStatusStr() {
        if (policyStatus == RetentionPolicyStatus.UNCHECKED) return "Un-checked";
        else if (policyStatus == RetentionPolicyStatus.OK) return "OK";
        else if (policyStatus == RetentionPolicyStatus.REVIEW) return "Review";
        else return ("Unknown");
    }
    
    public Date getGrantEndDate() {
        return grantEndDate;
    }

    public void setGrantEndDate(Date grantEndDate) {
        this.grantEndDate = grantEndDate;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

	public long getNumberOfDeposits() {
		return numberOfDeposits;
	}

	public void setNumberOfDeposits(long numberOfDeposits) {
		this.numberOfDeposits = numberOfDeposits;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getSliceID() {
        return this.sliceID;
    }

    public void setSliceID(String sliceID) {
        this.sliceID = sliceID;
    }

	public long getProjectSize() {
		return projectSize;
	}

	public void setProjectSize(long projectSize) {
		this.projectSize = projectSize;
	}
	
	public String getProjectSizeStr() {
        if ( projectSize == 0 ){
            return "0";
        }
        double s = projectSize;
        double gibibytes = s/1024/1024/1024;
        DecimalFormat df = new DecimalFormat("#");
        String dx = df.format(gibibytes);
        if(dx.equals("0")){
            return "< 1 GB";
        }
        return dx + " GB";
    }


	public BigDecimal getAmountToBeBilled() {
		return amountToBeBilled;
	}

	public void setAmountToBeBilled(BigDecimal amountToBeBilled) {
		this.amountToBeBilled = amountToBeBilled;
	}

	public BigDecimal getAmountBilled() {
		return amountBilled;
	}

	public void setAmountBilled(BigDecimal amountBilled) {
		this.amountBilled = amountBilled;
	}

    public Boolean getAffirmed() {
        return affirmed;
    }

    public void setAffirmed(Boolean affirmed) {
        this.affirmed = affirmed;
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

    public String getContact() {
        return this.contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setDataCreators(List<String> creators) {
        this.creators = creators;
    }

    public List<String> getDataCreators() {
        return this.creators;
    }

    public List<String> getNominatedDataManagerIds() {
        return nominatedDataManagerIds;
    }

    public void setNominatedDataManagerIds(List<String> nominatedDataManagerIds) {
        this.nominatedDataManagerIds = nominatedDataManagerIds;
    }

    public List<String> getDepositorIds() {
        return depositorIds;
    }

    public void setDepositorIds(List<String> depositorIds) {
        this.depositorIds = depositorIds;
    }

    public Boolean getPureLink() {
        return pureLink;
    }

    public void setPureLink(Boolean pureLink) {
        this.pureLink = pureLink;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }
    

    public String getVaultCreatorId() {
		return vaultCreatorId;
	}

	public void setVaultCreatorId(String vaultCreatorId) {
		this.vaultCreatorId = vaultCreatorId;
	}

	public CreateVault convertToCreate() {
        /*
        TODO: need to add validation / defend against nulls just a work in progress
         */
        CreateVault cv = new CreateVault();
        cv.setPendingID(this.getID());
        cv.setAffirmed(this.getAffirmed());
        if (this.getBillingType() != null) {
            cv.setBillingType(this.getBillingType().toString());
        }
        if (this.getBillingType().equals(PendingVault.Billing_Type.SLICE)) {
            cv.setSliceID(this.getSliceID());
        }

        if (this.getBillingType().equals(PendingVault.Billing_Type.GRANT_FUNDING)) {
            cv.setGrantAuthoriser(this.getAuthoriser());
            cv.setGrantSchoolOrUnit(this.getSchoolOrUnit());
            cv.setGrantSubunit(this.getSubunit());
            cv.setProjectTitle(this.getProjectTitle());
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

            if (this.getGrantEndDate() != null) {
                cv.setBillingGrantEndDate(formatter.format(this.getGrantEndDate()));
            }
        }

        if (this.getBillingType().equals(PendingVault.Billing_Type.BUDGET_CODE)) {
            cv.setBudgetAuthoriser(this.getAuthoriser());
            cv.setBudgetSchoolOrUnit(this.getSchoolOrUnit());
            cv.setBudgetSubunit(this.getSubunit());
        }

        cv.setName(this.getName());
        //logger.info("Vault Description is: '" + vault.getDescription());
        cv.setDescription(this.getDescription());
        //logger.info("Create Vault Description is: '" + cv.getDescription());
        cv.setPolicyInfo(this.getPolicyID() + "-" + this.getPolicyLength());
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        if (this.getGrantEndDate() != null) {
            cv.setGrantEndDate(formatter.format(this.getGrantEndDate()));
        }
        cv.setGroupID(this.getGroupID());
        if (this.getReviewDate() != null) {

            cv.setReviewDate(formatter.format(this.getReviewDate()));
        }
        if (this.getEstimate() != null) {
            cv.setEstimate(this.getEstimate().toString());
        }
        cv.setContactPerson(this.getContact());

        //cv.setIsOwner(vault.getIsOwner());
        // if vault owner is null set isowner to true
        // if vault owner is the same as the logged in user set isowner to true
        // if vault owner is different ot hte logged in user set to false
        //logger.debug("Setting default isOwner");
        Boolean isOwner = true;
        if (this.getOwnerId() != null && ! this.getOwnerId().equals(this.getUserID())) {
            //logger.debug("Changing default isOwner");
            isOwner = false;
        }
        cv.setIsOwner(isOwner);
        cv.setVaultOwner(this.getOwnerId());
        cv.setNominatedDataManagers(this.getNominatedDataManagerIds());
        cv.setDepositors(this.getDepositorIds());
        cv.setDataCreators(this.getDataCreators());
        cv.setNotes(this.getNotes());
        cv.setPureLink(this.getPureLink());
        cv.setConfirmed(this.getConfirmed());

        return cv;
    }
}
