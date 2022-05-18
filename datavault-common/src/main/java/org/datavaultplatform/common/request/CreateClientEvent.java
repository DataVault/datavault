package org.datavaultplatform.common.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "CreateClientEvent")
public class CreateClientEvent {
    @JsonIgnore
    private String sessionId;

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        CreateClientEvent rhs = (CreateClientEvent) obj;
        return new EqualsBuilder()
            .append(remoteAddress, rhs.remoteAddress)
            .append(userAgent, rhs.userAgent)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,37)
            .append(remoteAddress)
            .append(userAgent)
            .build();
    }
}