package org.datavaultplatform.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "CreateVault")
public class CreateVault {

    @ApiObjectField(description = "A name for the new vault")
    private String name;
    
    @ApiObjectField(description = "A description of the vault")
    private String description;

    @ApiObjectField(description = "Notes regarding data retention")
    private String notes;

    @ApiObjectField(description = "Estimate of vault size")
    private String estimate;
    
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



    public CreateVault() { }
    public CreateVault(String name, String description, String notes, String policyID, String groupID, String datasetID, String grantEndDate, String reviewDate, Boolean partial, Boolean affirmed) {
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

    public String getEstimate() {
        return this.estimate;
    }

    public void setEstimate(String estimate) {
        this.estimate = estimate;
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
}
