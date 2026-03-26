package org.datavaultplatform.common.response;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "VaultInfo")
@Data
public class VaultInfo {
    
    @ApiObjectField(description = "The unique identifier for this vault")
    private String id;
    
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DateTimeUtils.ISO_DATE_TIME_FORMAT)
    @ApiObjectField(description = "The date and time when this vault was created")
    private LocalDateTime creationTime;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DateTimeUtils.ISO_DATE_TIME_FORMAT)
    @ApiObjectField(description = "The date and time when the policy will expire")
    private LocalDateTime policyExpiry;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DateTimeUtils.ISO_DATE_TIME_FORMAT)
    @ApiObjectField(description = "The date and time when the policy check was last carried out")
    private LocalDateTime policyLastChecked;

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

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= DateTimeUtils.ISO_DATE_FORMAT)
    @ApiObjectField(description = "Define the minimum of time the archive will be kept")
    private LocalDate grantEndDate;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= DateTimeUtils.ISO_DATE_FORMAT)
    @ApiObjectField(description = "The date by which the vault should be reviewed for decision as to whether it should be deleted or whether there are funds available to support continued storage")
    private LocalDate reviewDate;
    
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

    @ApiObjectField(description = "The Billing page sliceQueryChoice radio button value")
	private PendingVault.Slice_Query_Choice sliceQueryChoice;

	@ApiObjectField(description = "The Billing page fundingQueryChoice radio button value")
	private PendingVault.Funding_Query_Choice fundingQueryChoice;

	@ApiObjectField(description = "The Billing page feewaiverQueryChoice radio button value")
	private PendingVault.Feewaiver_Query_Choice feewaiverQueryChoice;

    @ApiObjectField(description = "The Billing payment details.")
	private String paymentDetails;
    
    private VaultReviewStatusInfo vaultReviewStatusInfo;

    public VaultInfo() { }

    public VaultInfo(String id, String userID, String userName, String datasetID, String crisID, String datasetName,
                     LocalDateTime creationTime, String name, String description, String policyID, String policyLength, String groupID,
                     long vaultSize, int policyStatus, LocalDateTime policyExpiry, LocalDateTime policyLastChecked, LocalDate grantEndDate,
                     LocalDate reviewDate, long numberOfDeposits, String projectId) {
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

     public VaultInfo(String id,String userName, LocalDateTime creationTime, String name,
    		long vaultSize, LocalDate reviewDate, LocalDate grantEndDate, BigDecimal amountToBeBilled,BigDecimal amountBilled, String projectId,
            String paymentDetails) {
        this.id = id;
        this.userName = userName;
        this.creationTime = creationTime;
        this.name = name;
        this.vaultSize = vaultSize;
        this.reviewDate = reviewDate;
        this.grantEndDate = grantEndDate;
        this.amountToBeBilled = amountToBeBilled;
        this.amountBilled = amountBilled;
        this.projectId = projectId;
        this.paymentDetails = paymentDetails;
    }

    public VaultInfo(String id,String userName, LocalDateTime creationTime, String name,
    		long vaultSize, LocalDate reviewDate,BigDecimal amountToBeBilled,BigDecimal amountBilled, String projectId,
            String paymentDetails) {
        this.id = id;        
        this.userName = userName;    
        this.creationTime = creationTime;
        this.name = name;        
        this.vaultSize = vaultSize;        
        this.reviewDate = reviewDate;
        this.amountToBeBilled = amountToBeBilled;
        this.amountBilled = amountBilled;
        this.projectId = projectId;
        this.paymentDetails = paymentDetails;
    }

	public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
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

    public String getPolicyStatusStr() {
        if (policyStatus == RetentionPolicyStatus.UNCHECKED) return "Un-checked";
        else if (policyStatus == RetentionPolicyStatus.OK) return "OK";
        else if (policyStatus == RetentionPolicyStatus.REVIEW) return "Review";
        else return ("Unknown");
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

	public CreateVault convertToCreate() {
        /*
        TODO: need to add validation / defend against nulls just a work in progress
         */
        CreateVault cv = new CreateVault();
        cv.setPendingID(this.getID());
        cv.setAffirmed(this.getAffirmed());
        if (this.getBillingType() != null) {
            cv.setBillingType(this.getBillingType().toString());
            cv.setSliceID(this.getSliceID());

            if (this.getBillingType().equals(PendingVault.Billing_Type.GRANT_FUNDING)) {
                cv.setGrantAuthoriser(this.getAuthoriser());
                cv.setGrantSchoolOrUnit(this.getSchoolOrUnit());
                cv.setGrantSubunit(this.getSubunit());
                cv.setProjectTitle(this.getProjectTitle());

                if (this.getGrantEndDate() != null) {
                    cv.setBillingGrantEndDate(this.getGrantEndDate());
                    cv.setGrantEndDate(this.getGrantEndDate());
                }
            }

            if (this.getBillingType().equals(PendingVault.Billing_Type.BUDGET_CODE)) {
                cv.setBudgetAuthoriser(this.getAuthoriser());
                cv.setBudgetSchoolOrUnit(this.getSchoolOrUnit());
                cv.setBudgetSubunit(this.getSubunit());
            }

            // New Billing types
            if (this.getBillingType().equals(PendingVault.Billing_Type.WILL_PAY)) {
                cv.setBudgetAuthoriser(this.getAuthoriser());
                cv.setBudgetSchoolOrUnit(this.getSchoolOrUnit());
                cv.setBudgetSubunit(this.getSubunit());
                cv.setProjectTitle(this.getProjectTitle());
                cv.setPaymentDetails(this.paymentDetails);

                if (this.getGrantEndDate() != null) {
                    cv.setBillingGrantEndDate(this.getGrantEndDate());
                    cv.setGrantEndDate(this.getGrantEndDate());
                }
            }

            if (this.getBillingType().equals(PendingVault.Billing_Type.FEEWAIVER)) {
               // TBD
            }

            if (this.getBillingType().equals(PendingVault.Billing_Type.BUY_NEW_SLICE)) {
                // TBD
             }

            if (this.getBillingType().equals(PendingVault.Billing_Type.FUNDING_NO_OR_DO_NOT_KNOW)) {
                // TBD
            }
        }

        cv.setName(this.getName());
        cv.setDescription(this.getDescription());
        cv.setPolicyInfo(this.getPolicyID() + "-" + this.getPolicyLength());

        if (this.getGrantEndDate() != null) {
            cv.setGrantEndDate(this.getGrantEndDate());
        }
        cv.setGroupID(this.getGroupID());
        if (this.getReviewDate() != null) {
            cv.setReviewDate(this.getReviewDate());
        }
        if (this.getEstimate() != null) {
            cv.setEstimate(this.getEstimate().toString());
        }
        cv.setContactPerson(this.getContact());

        //cv.setIsOwner(vault.getIsOwner());
        // if vault owner is null set isowner to true
        // if vault owner is the same as the logged in user set isowner to true
        // if vault owner is different ot hte logged in user set to false
        boolean isOwner = this.getOwnerId() == null || this.getOwnerId().equals(this.getUserID());
        cv.setIsOwner(isOwner);
        cv.setVaultOwner(this.getOwnerId());
        cv.setNominatedDataManagers(this.getNominatedDataManagerIds());
        cv.setDepositors(this.getDepositorIds());
        cv.setDataCreators(this.getDataCreators());
        cv.setNotes(this.getNotes());
        cv.setPureLink(this.getPureLink());
        cv.setConfirmed(this.getConfirmed());
        cv.setVaultCreator(this.getVaultCreatorId());

        // Billing page checkbox information
        if (this.getSliceQueryChoice() != null) {
            cv.setSliceQueryChoice(this.getSliceQueryChoice().toString());
        }
        if (this.getFundingQueryChoice() != null) {
            cv.setFundingQueryChoice(this.getFundingQueryChoice().toString());
        }
        if (this.getFeewaiverQueryChoice() != null) {
            cv.setFeewaiverQueryChoice(this.getFeewaiverQueryChoice().toString());
        }

        cv.setPaymentDetails(this.paymentDetails);

        return cv;
    }
    
    /**
     * see 'templates/vaults/security.html'
     */
    public static VaultInfo create(){
        return new VaultInfo();
    }

    public void setDataCreators(List<String> creators) {
        this.creators = creators;
    }

    public List<String> getDataCreators() {
        return this.creators;
    }

}
