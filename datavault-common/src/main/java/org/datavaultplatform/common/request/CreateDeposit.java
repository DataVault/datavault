package org.datavaultplatform.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "CreateDeposit")
public class CreateDeposit {
    
    @ApiObjectField(description = "Note to briefly describe the purpose or contents of this deposit")
    private String note;
    
    @ApiObjectField(description = "File path of the data to deposit (including device ID)")
    private String filePath;
    
    public CreateDeposit() { }
    public CreateDeposit(String note, String filePath) {
        this.note = note;
        this.filePath = filePath;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
