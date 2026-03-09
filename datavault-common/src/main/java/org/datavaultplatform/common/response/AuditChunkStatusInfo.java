package org.datavaultplatform.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.util.DateTimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "AuditChunkStatus")
public class AuditChunkStatusInfo {

    @Schema(description = "Universally Unique Identifier for the Audit")
    private String id;

    @Schema(description = "Date that the audit started")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DateTimeUtils.ISO_DATE_TIME_FORMAT)
    private Date creationTime;

    @Schema(description = "Date that the audit finished")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DateTimeUtils.ISO_DATE_TIME_FORMAT)
    private Date completedTime;

    @Schema(description = "Status of the Audit")
    private
    AuditChunkStatus.Status status;

    @Schema(description = "Deposit chunks")
    private DepositChunk depositChunk;

    @Schema(description = "The chunk deposit")
    private Deposit deposit;

    @Schema(description = "The archive ID")
    private String archiveId;

    @Schema(description = "The error note if any")
    private String note;

    public AuditChunkStatusInfo() {}
    public AuditChunkStatusInfo(String id, Date creationTime, Date completedTime, AuditChunkStatus.Status status, DepositChunk depositChunk, Deposit deposit,
                                String archiveId, String note){
        this.setId(id);
        this.setCreationTime(creationTime);
        this.setCompletedTime(completedTime);
        this.setStatus(status);
        this.setDepositChunk(depositChunk);
        this.setDeposit(deposit);
        this.setArchiveId(archiveId);
        this.setNote(note);
    }

    public String getID() {
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

    public AuditChunkStatus.Status getStatus() {
        return status;
    }

    public void setStatus(AuditChunkStatus.Status status) {
        this.status = status;
    }

    public DepositChunk getDepositChunk() {
        return depositChunk;
    }

    public void setDepositChunk(DepositChunk depositChunk) {
        this.depositChunk = depositChunk;
    }

    public void setDeposit(Deposit deposit) {
        this.deposit = deposit;
    }

    public Deposit getDeposit() {
        return deposit;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public Date getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(Date completedTime) {
        this.completedTime = completedTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
