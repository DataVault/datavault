package org.datavaultplatform.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "CreateClientEvent")
public class CreateClientEvent {

    @ApiObjectField(description = "The remote IP address which triggered this event")
    private String remoteAddress;
    
    @ApiObjectField(description = "The browser user agent (if applicable)")
    private String userAgent;
    
    public CreateClientEvent() { }
    public CreateClientEvent(String remoteAddress, String userAgent) {
        this.remoteAddress = remoteAddress;
        this.userAgent = userAgent;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}