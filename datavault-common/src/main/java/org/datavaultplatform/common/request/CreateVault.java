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
    
    public CreateVault() { }
    public CreateVault(String name, String description, String policyID, String groupID, String datasetID, String grantEndDate, String reviewDate) {
        this.name = name;
        this.description = description;
        this.policyID = policyID;
        this.groupID = groupID;
        this.datasetID = datasetID;
        this.grantEndDate = grantEndDate;
        this.reviewDate = reviewDate;
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
}
