package org.datavaultplatform.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.model.Audit;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "Audit")
public class AuditInfo {

    @ApiObjectField(description = "Universally Unique Identifier for the Audit", name="Audit")
    private String id;

    @ApiObjectField(description = "Date that the audit started")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date creationTime;

    @ApiObjectField(description = "Status of the Audit", allowedvalues={"FAILED", "IN_PROGRESS", "COMPLETE"})
    private
    Audit.Status status;

    @ApiObjectField(description = "Deposit chunks")
    private List<AuditChunkStatusInfo> auditChunks;

    public AuditInfo() {}
    public AuditInfo(String id, Date creationTime, Audit.Status status) {
        this.setId(id);
        this.setCreationTime(creationTime);
        this.setStatus(status);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Audit.Status getStatus() {
        return status;
    }

    public void setStatus(Audit.Status status) {
        this.status = status;
    }

    public List<AuditChunkStatusInfo> getAuditChunks() {
        return auditChunks;
    }

    public void setAuditChunks(List<AuditChunkStatusInfo> auditChunks) {
        this.auditChunks = auditChunks;
    }
}
