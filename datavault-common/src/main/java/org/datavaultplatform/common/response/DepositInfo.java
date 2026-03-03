package org.datavaultplatform.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.DepositPath;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;
import org.datavaultplatform.common.io.DataVaultFileUtils;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "Deposit")
@Data
public class DepositInfo {

    @ApiObjectField(description = "Universally Unique Identifier for the Deposit", name="Deposit")
    private String id;
    
    // Serialise date in ISO 8601 format
    @ApiObjectField(description = "Date that the vault was created")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= DateTimeUtils.ISO_DATE_TIME_FORMAT)
    private LocalDateTime creationTime;

    @ApiObjectField(description = "Status of the Deposit", allowedvalues={"NOT_STARTED", "IN_PROGRESS", "COMPLETE"})
    private Deposit.Status status;

    @ApiObjectField(description = "Deposit name to briefly describe the Deposit")
    private String name;
    
    @ApiObjectField(description = "Deposit description to provide more information about the Deposit")
    private String description;

    @ApiObjectField(description = "Whether the deposit contains personal data or not")
    private Boolean hasPersonalData;
    
    @ApiObjectField(description = "Description of the nature of the personal data")
    private String personalDataStatement;
    
    // Record the file path that the user selected for this deposit.
    @ApiObjectField(description = "Origin of the deposited filepath")
    private String fileOrigin;
    
    @ApiObjectField(description = "Short version of the origin of the deposited filepath")
    private String shortFilePath;
    
    @ApiObjectField(description = "Filepath of the origin deposit")
    private String filePath;
    
    @ApiObjectField(description = "Vault ID where this deposit was carried out")
    private String vaultID;

    @ApiObjectField(description = "Vault Name where this deposit was carried out")
    private String vaultName;
    
    // Size of the deposit (in bytes)
    @ApiObjectField(description = "Size of the depoit (in bytes)")
    private long depositSize;
    
    @ApiObjectField(description = "Deposit paths")
    private List<DepositPath> depositPaths;

    @ApiObjectField(description = "Deposit chunks")
    private List<DepositChunk> depositChunks;

    @ApiObjectField(description = "The user UUN who did this deposit")
    private String userID;

    @ApiObjectField(description = "The user name who did this deposit")
    private String userName;

    @ApiObjectField(description = "The vault owner UUN")
    private String vaultOwnerID;

    @ApiObjectField(description = "The vault owner name")
    private String vaultOwnerName;

    @ApiObjectField(description = "The Vault Dataset ID i.e. Pure Record ID")
    private String datasetID;

    @ApiObjectField(description = "The Vault Dataset ID i.e. Pure Record ID (alternative)")
    private String crisID;

    @ApiObjectField(description = "The Vault Group Name i.e. School")
    private String groupName;

    @ApiObjectField(description = "The Vault Group ID i.e. School ID")
    private String groupID;

    @ApiObjectField(description = "The Vault Group Name i.e. School")
    private String vaultReviewDate;
    
    public DepositInfo() {}
    public DepositInfo(String id, String userID, LocalDateTime creationTime, Deposit.Status status, String name,
                       String description, boolean hasPersonalData, String personalDataStatement, String fileOrigin,
                       String shortFilePath, String filePath, long depositSize, String vaultID,
                       List<DepositPath> depositPaths, List<DepositChunk> depositChunks) {
        this.id = id;
        this.userID = userID;
        this.creationTime = creationTime;
        this.status = status;
        this.name = name;
        this.description = description;
        this.hasPersonalData = hasPersonalData;
        this.personalDataStatement = personalDataStatement;
        this.fileOrigin = fileOrigin;
        this.shortFilePath = shortFilePath;
        this.filePath = filePath;
        this.depositSize = depositSize;
        this.vaultID = vaultID;
        this.depositPaths = depositPaths;
        this.depositChunks = depositChunks;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getSizeStr() { return DataVaultFileUtils.getGibibyteSizeStr(depositSize); }
}
