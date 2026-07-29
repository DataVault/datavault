package org.datavaultplatform.common.request;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "CreateDeposit")
@Data
public class CreateDeposit {
    
    @Schema(description = "Name to briefly describe the purpose or contents of this deposit")
    private String name;
    
    @Schema(description = "Detailed description of the purpose or contents of this deposit")
    private String description;
    
    @Schema(description = "Whether the deposit contains personal data or not")
    private String hasPersonalData;
    
    @Schema(description = "Description of the nature of the personal data")
    private String personalDataStatement;
    
    @Schema(description = "The vault which this deposit will be added to")
    private String vaultID;
    
    @Schema(description = "File paths of the data to deposit (including device ID)")
    private List<String> depositPaths;
    
    @Schema(description = "The temporary upload location for files")
    private String fileUploadHandle;
    
    public CreateDeposit() { }
    public CreateDeposit(String name, String description, String hasPersonalData, String personalDataStatement, List<String> depositPaths, String vaultID, String fileUploadHandle) {
        this.name = name;
        this.description = description;
        this.hasPersonalData = hasPersonalData;
        this.personalDataStatement = personalDataStatement;
        this.depositPaths = depositPaths;
        this.vaultID = vaultID;
        this.fileUploadHandle = fileUploadHandle;
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
    
    public String getHasPersonalData() {
        return hasPersonalData;
    }
    
    public void setHasPersonalData(String hasPersonalData) {
        this.hasPersonalData = hasPersonalData;
    }
    
    public String getPersonalDataStatement() {
        return personalDataStatement;
    }
    
    public void setPersonalDataStatement(String personalDataStatement) {
        this.personalDataStatement = personalDataStatement;
    }
    
    public String getVaultID() {
        return vaultID;
    }

    public void setVaultID(String vaultID) {
        this.vaultID = vaultID;
    }
    
    public List<String> getDepositPaths() {
        return depositPaths;
    }
    
    public void setDepositPaths(List<String> depositPaths) {
        this.depositPaths = depositPaths;
    }

    public String getFileUploadHandle() {
        return fileUploadHandle;
    }

    public void setFileUploadHandle(String fileUploadHandle) {
        this.fileUploadHandle = fileUploadHandle;
    }
}
