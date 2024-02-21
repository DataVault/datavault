package org.datavaultplatform.broker.actuator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LocalFileStoreInfo {
    private String id;
    private String label;
    private String rootPath;
    private boolean isValid;
    private String validationException;
    private boolean retrieveEnabled;
}
