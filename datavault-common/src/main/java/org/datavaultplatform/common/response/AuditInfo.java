package org.datavaultplatform.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.util.DateTimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Audit")
public class AuditInfo {

    @Schema(description = "Universally Unique Identifier for the Audit")
    private String id;

    @Schema(description = "Date that the audit started")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= DateTimeUtils.ISO_DATE_TIME_FORMAT)
    private Date creationTime;

    @Schema(description = "Status of the Audit")
    private
    Audit.Status status;

    @Schema(description = "Deposit chunks")
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
