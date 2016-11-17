package org.datavaultplatform.common.request;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "CreateDeposit")
public class CreateDeposit {
    
    @ApiObjectField(description = "Note to briefly describe the purpose or contents of this deposit")
    private String note;
    
    @ApiObjectField(description = "The vault which this deposit will be added to")
    private String vaultID;
    
    @ApiObjectField(description = "File paths of the data to deposit (including device ID)")
    private List<String> depositPaths;
    
    @ApiObjectField(description = "The temporary upload location for files")
    private String fileUploadHandle;
    
    public CreateDeposit() { }
    public CreateDeposit(String note, List<String> depositPaths, String vaultID, String fileUploadHandle) {
        this.note = note;
        this.depositPaths = depositPaths;
        this.vaultID = vaultID;
        this.fileUploadHandle = fileUploadHandle;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
