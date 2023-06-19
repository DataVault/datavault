package org.datavaultplatform.common.response;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

import lombok.Data;
import org.datavaultplatform.common.model.PendingVault;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "BillingInfo")
@Data
public class BillingInformation {
    
    @ApiObjectField(description = "The unique identifier for this vault")
    private String id;
    
    @ApiObjectField(description = "The vault ID")
    private String vaultID;

	@ApiObjectField(description = "The Billing type")
	private PendingVault.Billing_Type billingType;

    @ApiObjectField(description = "Billing Details Contact Name")
    private String contactName;
        
    @ApiObjectField(description = "Internal Customer Name")
    private String school;

    @ApiObjectField(description = "The Billing Subunit")
    private String subUnit;

	@ApiObjectField(description = "The Billing Project title")
	private String projectTitle;
    
    @ApiObjectField(description = "To know if the user uses BudgetCode")
    private Boolean budgetCode;

	@ApiObjectField(description = "The Billing SliceID")
	private String sliceID;
    
    @ApiObjectField(description = "The comments entered by Admin")
    private String specialComments;
    
    @ApiObjectField(description = "The value calculated for all deposits in the vault entered by Admin")
    private BigDecimal amountToBeBilled;
    
    @ApiObjectField(description = "The value calculated for all deposits in the vault entered by Admin")
    private BigDecimal amountBilled;
          
    @ApiObjectField(description = "Sum of vaults size for a projectId")
    private long projectSize;
   
    @ApiObjectField(description = "Sum of vaults size for a projectId")
	private long vaultSize;
 
	@ApiObjectField(description = "The Date the vault was last reviewed")
	private Date reviewDate;
   
    @ApiObjectField(description = "The creation Date of the vault")
	private Date creationTime;
    
    @ApiObjectField(description = "Name of the vault")
	private String vaultName;

    @ApiObjectField(description = "Project ID")
	private String projectId;
    
    @ApiObjectField(description = "Owner Name")
	private String userName;

	@ApiObjectField(description = "The Billing payment details.")
	private String paymentDetails;

    
    public BillingInformation() {
    	
    }


	public BillingInformation(String id, String vaultID, String contactName, String school, String subUnit, Boolean budgetCode,
			String specialComments, BigDecimal amountToBeBilled, BigDecimal amountBilled, long vaultSize, Date reviewDate,
							  Date creationTime, String vaultName,String projectId,String userName, String sliceID,
							  String projectTitle, PendingVault.Billing_Type billingType, String paymentDetails) {
    	this.id = id;        
        this.vaultID = vaultID;    
        this.contactName = contactName; 
        this.school = school;        
        this.subUnit = subUnit;        
        this.budgetCode = budgetCode;       
        this.specialComments = specialComments;
        this.amountToBeBilled = amountToBeBilled;       
        this.amountBilled = amountBilled;
        this.vaultSize = vaultSize;
        this.reviewDate = reviewDate;       
        this.creationTime = creationTime;
        this.vaultName = vaultName;
        this.projectId = projectId;
        this.userName = userName;
        this.sliceID = sliceID;
        this.projectTitle = projectTitle;
        this.billingType = billingType;
		this.paymentDetails = paymentDetails;
	}
	
	public BillingInformation(String id, String vaultID, String contactName, String school, String subUnit, Boolean budgetCode,
			String specialComments, BigDecimal amountToBeBilled, BigDecimal amountBilled, String projectId, String vaultName,
							  String sliceID, String projectTitle, PendingVault.Billing_Type billingType, String paymentDetails) {
    	this.id = id;        
        this.vaultID = vaultID;    
        this.contactName = contactName; 
        this.school = school;        
        this.subUnit = subUnit;        
        this.budgetCode = budgetCode;       
        this.specialComments = specialComments;
        this.amountToBeBilled = amountToBeBilled;       
        this.amountBilled = amountBilled;
        this.projectId= projectId;
        this.vaultName = vaultName;
        this.sliceID = sliceID;
        this.projectTitle = projectTitle;
        this.billingType = billingType;
		this.paymentDetails = paymentDetails;
	}

    public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVaultID() {
		return vaultID;
	}

	public void setVaultID(String vaultID) {
		this.vaultID = vaultID;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	public String getSubUnit() {
		return subUnit;
	}

	public void setSubUnit(String subUnit) {
		this.subUnit = subUnit;
	}

	public Boolean getBudgetCode() {
		return budgetCode;
	}

	public void setBudgetCode(Boolean budgetCode) {
		this.budgetCode = budgetCode;
	}

	public String getSpecialComments() {
		return specialComments;
	}

	public void setSpecialComments(String specialComments) {
		this.specialComments = specialComments;
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
    
    public long getVaultSize() {
		return vaultSize;
	}

	public void setVaultSize(long vaultSize) {
		this.vaultSize = vaultSize;
	}

	public Date getReviewDate() {
		return reviewDate;
	}

	public void setReviewDate(Date reviewDate) {
		this.reviewDate = reviewDate;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public String getVaultName() {
		return vaultName;
	}

	public void setVaultName(String vaultName) {
		this.vaultName = vaultName;
	}

	public long getProjectSize() {
		return projectSize;
	}

	public void setProjectSize(long projectSize) {
		this.projectSize = projectSize;
	}

	public PendingVault.Billing_Type getBillingType() {
		return billingType;
	}

	public void setBillingType(PendingVault.Billing_Type billingType) {
		this.billingType = billingType;
	}

	public String getProjectTitle() {
		return projectTitle;
	}

	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}

	public String getSliceID() {
		return sliceID;
	}

	public void setSliceID(String sliceID) {
		this.sliceID = sliceID;
	}

	 public String getPaymentDetails() {
        return this.paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
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
	
	public String getBudgetCodeStr() {
		if(budgetCode == null) {
			return null;
		}
		return Boolean.TRUE.equals(budgetCode) ? "Yes" : "No";
	}

}
