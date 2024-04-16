package org.datavaultplatform.common.response;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "EventInfo")
public class EventInfo {
    
    @ApiObjectField(description = "The unique identifier for this event")
    private String id;
    
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= DateTimeUtils.ISO_DATE_TIME_FORMAT)
    @ApiObjectField(description = "The date and time when this event occured")
    private Date timestamp;
    
    private String message;
    private String userID;
    private String vaultID;
    private String eventClass;
    private String depositID;
    private String agent;
    private String agentType;
    private String remoteAddress;
    private String userAgent;
    
    public EventInfo() { }
    public EventInfo(String id, Date timestamp, String message, String userID, String vaultID, String eventClass, String depositID, String actor, String actorType, String remoteAddress, String userAgent) {
        this.id = id;
        this.timestamp = timestamp;
        this.message = message;
        this.userID = userID;
        this.vaultID = vaultID;
        this.eventClass = eventClass;
        this.depositID = depositID;
        this.agent = actor;
        this.agentType = actorType;
        this.remoteAddress = remoteAddress;
        this.userAgent = userAgent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getVaultID() {
        return vaultID;
    }

    public void setVaultID(String vaultID) {
        this.vaultID = vaultID;
    }

    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }

    public String getDepositID() {
        return depositID;
    }

    public void setDepositID(String depositID) {
        this.depositID = depositID;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
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
