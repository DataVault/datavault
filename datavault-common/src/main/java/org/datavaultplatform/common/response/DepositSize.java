package org.datavaultplatform.common.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "DepositSize")
public class DepositSize {
    @ApiObjectField(description = "The max deposit size allowed")
    private Long max;
    @ApiObjectField(description = "Whether the potential deposit is under the limit")
    private Boolean result;

    public Long getMax() {
        return this.max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public Boolean getResult() {
        return this.result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }
}
