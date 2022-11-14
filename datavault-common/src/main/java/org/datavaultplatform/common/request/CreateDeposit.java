package org.datavaultplatform.common.request;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "CreateDeposit")
@Data
public class CreateDeposit {
    
    @ApiObjectField(description = "Name to briefly describe the purpose or contents of this deposit")
    private String name;
    
    @ApiObjectField(description = "Detailed description of the purpose or contents of this deposit")
    private String description;
    
    @ApiObjectField(description = "Whether the deposit contains personal data or not")
    private String hasPersonalData;
    
    @ApiObjectField(description = "Description of the nature of the personal data")
    private String personalDataStatement;
    
    @ApiObjectField(description = "The vault which this deposit will be added to")
    private String vaultID;
    
    @ApiObjectField(description = "File paths of the data to deposit (including device ID)")
    private List<String> depositPaths;
    
    @ApiObjectField(description = "The temporary upload location for files")
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
