package org.datavaultplatform.common.response;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicyStatus;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

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
    
    @ApiObjectField(description = "The policy that applies to this vault")
    private String policyID;
    
    @ApiObjectField(description = "The group which is related to this vault")
    private String groupID;
    
    @ApiObjectField(description = "The user who owns this vault")
    private String userID;
    
    @ApiObjectField(description = "A reference to an external metadata record that describes this vault")
    private String datasetID;
    
    @ApiObjectField(description = "The size of this vault in bytes")
    private long vaultSize;
    
    @ApiObjectField(description = "The status of the vault policy")
    private int policyStatus;
    
    public VaultInfo() { }

    public VaultInfo(String id, String userID, String datasetID, Date creationTime, String name, String description, String policyID, String groupID, long vaultSize, int policyStatus, Date policyExpiry, Date policyLastChecked) {
        this.id = id;
        this.userID = userID;
        this.datasetID = datasetID;
        this.creationTime = creationTime;
        this.name = name;
        this.description = description;
        this.policyID = policyID;
        this.groupID = groupID;
        this.vaultSize = vaultSize;
        this.policyStatus = policyStatus;
        this.policyExpiry = policyExpiry;
        this.policyLastChecked = policyLastChecked;
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

    public String getDatasetID() {
        return datasetID;
    }

    public void setDatasetID(String datasetID) {
        this.datasetID = datasetID;
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

    public long getVaultSize() {
        return vaultSize;
    }

    public void setVaultSize(long vaultSize) {
        this.vaultSize = vaultSize;
    }
    
    public String getSizeStr() {
        return FileUtils.byteCountToDisplaySize(vaultSize);
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
}
