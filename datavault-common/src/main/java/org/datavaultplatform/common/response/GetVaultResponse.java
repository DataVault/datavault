package org.datavaultplatform.common.response;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.io.FileUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetVaultResponse {
    
    private String id;
    
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date creationTime;
    
    private String name;
    private String description;
    private String policyID;
    private String groupID;
    
    private long vaultSize;
    
    public GetVaultResponse() { }
    public GetVaultResponse(String id, Date creationTime, String name, String description, String policyID, String groupID,long vaultSize) {
        this.id = id;
        this.creationTime = creationTime;
        this.name = name;
        this.description = description;
        this.policyID = policyID;
        this.groupID = groupID;
        this.vaultSize = vaultSize;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
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
}
