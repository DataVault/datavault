package org.datavaultplatform.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.DepositPath;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;
import org.datavaultplatform.common.io.FileUtils;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "Deposit")
public class DepositInfo {

    @ApiObjectField(description = "Universally Unique Identifier for the Deposit", name="Deposit")
    private String id;
    
    // Serialise date in ISO 8601 format
    @ApiObjectField(description = "Date that the vault was created")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date creationTime;

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
    
    @ApiObjectField(description = "Vault where this deposit was carried out")
    private String vaultID;
    
    // Size of the deposit (in bytes)
    @ApiObjectField(description = "Size of the depoit (in bytes)")
    private long depositSize;
    
    @ApiObjectField(description = "Deposit paths")
    private List<DepositPath> depositPaths;

    @ApiObjectField(description = "Deposit chunks")
    private List<DepositChunk> depositChunks;

    @ApiObjectField(description = "The user who did this deposit")
    private String userID;
    
    public DepositInfo() {}
    public DepositInfo(String id, String userID, Date creationTime, Deposit.Status status, String name, String description, boolean hasPersonalData, String personalDataStatement, String fileOrigin, String shortFilePath, String filePath, long depositSize, String vaultID, List<DepositPath> depositPaths, List<DepositChunk> depositChunks) {
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

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
    
    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Deposit.Status getStatus() {
        return status;
    }

    public void setStatus(Deposit.Status status) {
        this.status = status;
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
    
    public Boolean getHasPersonalData() {
        return hasPersonalData;
    }
    
    public void setHasPersonalData(Boolean hasPersonalData) {
        this.hasPersonalData = hasPersonalData;
    }
    
    public String getPersonalDataStatement() {
        return personalDataStatement;
    }
    
    public void setPersonalDataStatement(String personalDataStatement) {
        this.personalDataStatement = personalDataStatement;
    }
    
    public String getFileOrigin() {
        return fileOrigin;
    }

    public void setFileOrigin(String fileOrigin) {
        this.fileOrigin = fileOrigin;
    }

    public String getShortFilePath() {
        return shortFilePath;
    }

    public void setShortFilePath(String shortFilePath) {
        this.shortFilePath = shortFilePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getVaultID() {
        return vaultID;
    }

    public void setVaultID(String vaultID) {
        this.vaultID = vaultID;
    }

    public long getDepositSize() {
        return depositSize;
    }

    public void setDepositSize(long depositSize) {
        this.depositSize = depositSize;
    }

    public String getSizeStr() { return FileUtils.getGibibyteSizeStr(depositSize); }

    public List<DepositPath> getDepositPaths() {
        return depositPaths;
    }

    public void setDepositPaths(List<DepositPath> depositPaths) {
        this.depositPaths = depositPaths;
    }

    public List<DepositChunk> getDepositChunks() {
        return depositChunks;
    }

    public void setDepositChunks(List<DepositChunk> depositChunks) {
        this.depositChunks = depositChunks;
    }
}
