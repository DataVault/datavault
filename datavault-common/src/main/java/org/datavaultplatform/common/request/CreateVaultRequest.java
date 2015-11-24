package org.datavaultplatform.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateVaultRequest {

    private String name;
    private String description;
    private String policyID;
    private String groupID;
    
    public CreateVaultRequest() { }
    public CreateVaultRequest(String name, String description, String policyID, String groupID) {
        this.name = name;
        this.description = description;
        this.policyID = policyID;
        this.groupID = groupID;
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
}
