package org.datavaultplatform.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.DepositPath;
import org.datavaultplatform.common.util.DateTimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import org.datavaultplatform.common.io.DataVaultFileUtils;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Deposit")
@Data
public class DepositInfo {

    @Schema(description = "Universally Unique Identifier for the Deposit")
    private String id;
    
    // Serialise date in ISO 8601 format
    @Schema(description = "Date that the vault was created")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= DateTimeUtils.ISO_DATE_TIME_FORMAT)
    private LocalDateTime creationTime;

    @Schema(description = "Status of the Deposit")
    private Deposit.Status status;

    @Schema(description = "Deposit name to briefly describe the Deposit")
    private String name;
    
    @Schema(description = "Deposit description to provide more information about the Deposit")
    private String description;

    @Schema(description = "Whether the deposit contains personal data or not")
    private Boolean hasPersonalData;
    
    @Schema(description = "Description of the nature of the personal data")
    private String personalDataStatement;
    
    // Record the file path that the user selected for this deposit.
    @Schema(description = "Origin of the deposited filepath")
    private String fileOrigin;
    
    @Schema(description = "Short version of the origin of the deposited filepath")
    private String shortFilePath;
    
    @Schema(description = "Filepath of the origin deposit")
    private String filePath;
    
    @Schema(description = "Vault ID where this deposit was carried out")
    private String vaultID;

    @Schema(description = "Vault Name where this deposit was carried out")
    private String vaultName;
    
    // Size of the deposit (in bytes)
    @Schema(description = "Size of the depoit (in bytes)")
    private long depositSize;
    
    @Schema(description = "Deposit paths")
    private List<DepositPath> depositPaths;

    @Schema(description = "Deposit chunks")
    private List<DepositChunk> depositChunks;

    @Schema(description = "The user UUN who did this deposit")
    private String userID;

    @Schema(description = "The user name who did this deposit")
    private String userName;

    @Schema(description = "The vault owner UUN")
    private String vaultOwnerID;

    @Schema(description = "The vault owner name")
    private String vaultOwnerName;

    @Schema(description = "The Vault Dataset ID i.e. Pure Record ID")
    private String datasetID;

    @Schema(description = "The Vault Dataset ID i.e. Pure Record ID (alternative)")
    private String crisID;

    @Schema(description = "The Vault Group Name i.e. School")
    private String groupName;

    @Schema(description = "The Vault Group ID i.e. School ID")
    private String groupID;

    @Schema(description = "The Vault Group Name i.e. School")
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
