package org.datavaultplatform.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.model.PendingVault;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "CreateVault")
public class CreateVault {

    /*
    TODO: Make a base Vault class and move everything that is shared between Vault and Pending vault into it
     */
    @ApiObjectField(description = "An ID for the partially completed pending vault")
    private String pendingID;

    @ApiObjectField(description = "A name for the new vault")
    private String name;
    
    @ApiObjectField(description = "A description of the vault")
    private String description;

    @ApiObjectField(description = "Notes regarding data retention")
    private String notes;

    @ApiObjectField(description = "Estimate of vault size")
    private String estimate;

    @ApiObjectField(description = "How we are billing")
    private String billingType;
    
    @ApiObjectField(description = "The policy that will be applied to this vault")
    private String policyID;
    
    @ApiObjectField(description = "The group which is related to this vault")
    private String groupID;
    
    @ApiObjectField(description = "A reference to an external metadata record that describes this vault")
    private String datasetID;
    
    @ApiObjectField(description = "Define the minimum of time the archive will be kept")
    private String grantEndDate;
    
    @ApiObjectField(description = "The date by which the vault should be reviewed for decision as to whether it should be deleted or whether there are funds available to support continued storage")
    private String reviewDate;

    @ApiObjectField(description = "Is the data a mid completion save")
    private Boolean partial = false;

    @ApiObjectField(description = "Did the user accept the various rules on the create vault intro page")
    private Boolean affirmed = false;

    @ApiObjectField(description = "If the billng type is slice we will store an identifier for the slice")
    private String sliceID;

    @ApiObjectField(description = "If the billng type is grant or budget we will store an identifier for authoriser")
    private String authoriser;

    @ApiObjectField(description = "If the billng type is grant or budget we will store an identifier for school / unit")
    private String schoolOrUnit;

    @ApiObjectField(description = "If the billng type is grant or budget we will store an identifier for subunit")
    private String subunit;

    @ApiObjectField(description = "If the billng type is grant or budget we will store an identifier for projectID")
    private String projectID;

    @ApiObjectField(description = "Is the logged in user the vault owner")
    private Boolean isOwner;

    @ApiObjectField(description = "Prospective Owner of the vault (who can be different from who is creating the pending vault)")
    private String vaultOwner;

    @ApiObjectField(description = "List of pending Nominated Data Managers")
    private List<String> nominatedDataManagers;

    @ApiObjectField(description = "List of pending depositors")
    private List<String> depositors;

    @ApiObjectField(description = "contact person for Pure")
    private String contactPerson;

    @ApiObjectField(description = "List of creators of the dataset")
    private List<String> dataCreators;

    public CreateVault() { }
    /*public CreateVault(String name, String description, String notes, String policyID, String groupID, String datasetID, String grantEndDate,
                       String reviewDate, Boolean partial, Boolean affirmed, String estimate, String billingType, String sliceID,
                       String authoriser, String schoolOrUnit, String subunit, String projectID, List<String> dataCreators,
                       String vaultOwner, List<String> nominatedDataManagers, List<String> depositors, String contactPerson) {
        this.name = name;
        this.description = description;
        this.notes = notes;
        this.policyID = policyID;
        this.groupID = groupID;
        this.datasetID = datasetID;
        this.grantEndDate = grantEndDate;
        this.reviewDate = reviewDate;
        this.partial = partial;
        this.affirmed = affirmed;
        this.estimate = estimate;
        this.billingType = billingType;
        this.sliceID = sliceID;
        this.authoriser = authoriser;
        this.schoolOrUnit = schoolOrUnit;
        this.subunit = subunit;
        this.projectID = projectID;
        this.dataCreators = dataCreators;
        this.vaultOwner = vaultOwner;
        this.nominatedDataManagers = nominatedDataManagers;
        this.depositors = depositors;
        this.contactPerson = contactPerson;
    }*/

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

    public String getEstimate() {
        return this.estimate;
    }

    public void setEstimate(String estimate) {
        this.estimate = estimate;
    }

    public String getBillingType() {
        return this.billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public String getPolicyID() {
        return policyID;
    }

    public void setPolicyID(String policyID) {
        this.policyID = policyID;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getDatasetID() {
        return datasetID;
    }

    public void setDatasetID(String datasetID) {
        this.datasetID = datasetID;
    }

    public String getGrantEndDate() {
        return grantEndDate;
    }

    public void setGrantEndDate(String grantEndDate) {
        this.grantEndDate = grantEndDate;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }

    public Boolean isPartial() {
        return this.partial;
    }

    public void setPartial(Boolean partial) {
        this.partial = partial;
    }

    public Boolean getAffirmed() {
        return this.affirmed;
    }

    public void setAffirmed(Boolean affirmed) {
        this.affirmed = affirmed;
    }

    public String getSliceID() {
        return this.sliceID;
    }

    public void setSliceID(String sliceID) {
        this.sliceID = sliceID;
    }

    public String getPendingID() {
        return this.pendingID;
    }

    public void setPendingID(String pendingID) {
        this.pendingID = pendingID;
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

    public String getProjectID() {
        return this.projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getVaultOwner() { return this.vaultOwner; }

    public void setVaultOwner(String vaultOwner) { this.vaultOwner = vaultOwner; }

    public List<String> getNominatedDataManagers() { return this.nominatedDataManagers; }

    public void setNominatedDataManagers(List<String> nominatedDataManagers) { this.nominatedDataManagers = nominatedDataManagers; }

    public List<String> getDepositors() { return this.depositors; }

    public void setDepositors(List<String> depositors) { this.depositors = depositors; }

    public String getContactPerson() { return this.contactPerson; }

    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public List<String> getDataCreators() { return this.dataCreators; }

    public void setDataCreators(List<String> dataCreators) { this.dataCreators = dataCreators; }

    public Boolean getIsOwner() {
        return this.isOwner;
    }

    public void setIsOwner(Boolean isOwner) {
        this.isOwner = isOwner;
    }
}
