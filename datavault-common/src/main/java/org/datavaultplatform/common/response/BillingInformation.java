package org.datavaultplatform.common.response;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "BillingInfo")
public class BillingInformation {
    
    @ApiObjectField(description = "The unique identifier for this vault")
    private String id;
    
    @ApiObjectField(description = "The vault ID")
    private String vaultID;

   
    @ApiObjectField(description = "Billing Details Contact Name")
    private String contactName;
        
    @ApiObjectField(description = "Internal Customer Name")
    private String school;

    @ApiObjectField(description = "The Billing Subunit")
    private String subUnit;
    
    @ApiObjectField(description = "To know if the user uses BudgetCode")
    private Boolean budgetCode;
    
    @ApiObjectField(description = "The comments entered by Admin")
    private String specialComments;
    
    @ApiObjectField(description = "The value calculated for all deposits in the vault entered by Admin")
    private BigDecimal amountToBeBilled;
    
    @ApiObjectField(description = "The value calculated for all deposits in the vault entered by Admin")
    private BigDecimal amountBilled;
          
    @ApiObjectField(description = "Project Id from Pure")
    private String projectId;
    
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
	private String projectID;
    
    @ApiObjectField(description = "Owner Name")
	private String userName;
    
    public BillingInformation() {
    	
    }

	public BillingInformation(String id, String vaultID, String contactName, String school, String subUnit, Boolean budgetCode,
			String specialComments, BigDecimal amountToBeBilled, BigDecimal amountBilled, long vaultSize, Date reviewDate, Date creationTime, String vaultName,String projectID,String userName) {
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
        this.projectID= projectID;
        this.userName= userName;
	}
	
	public BillingInformation(String id, String vaultID, String contactName, String school, String subUnit, Boolean budgetCode,
			String specialComments, BigDecimal amountToBeBilled, BigDecimal amountBilled, String projectID, String vaultName) {
    	this.id = id;        
        this.vaultID = vaultID;    
        this.contactName = contactName; 
        this.school = school;        
        this.subUnit = subUnit;        
        this.budgetCode = budgetCode;       
        this.specialComments = specialComments;
        this.amountToBeBilled = amountToBeBilled;       
        this.amountBilled = amountBilled;
        this.projectID= projectID;
        this.vaultName = vaultName;
	}

    public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getProjectID() {
		return projectID;
	}

	public void setProjectID(String projectID) {
		this.projectID = projectID;
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

	public void setAmounToBeBilled(BigDecimal amountToBeBilled) {
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


	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
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
	
	public String getBudgetCodeStr() {
		if(budgetCode == null) {
			return null;
		}
		return Boolean.TRUE.equals(budgetCode) ? "Yes" : "No";
	}
}
