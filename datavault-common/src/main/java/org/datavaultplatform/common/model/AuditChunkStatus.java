package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.response.AuditChunkStatusInfo;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="AuditChunkStatus")
public class AuditChunkStatus {

    // Deposit Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completeTime;

//    @OneToMany(targetEntity = DepositChunk.class, mappedBy = "deposit", fetch=FetchType.LAZY)
//    private List<DepositChunk> chunks;

    public enum Status {
        QUEUING,
        IN_PROGRESS,
        COMPLETE,
        FIXED,
        ERROR
    }

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(columnDefinition = "TEXT")
    private Status status;

    @JsonIgnore
    @ManyToOne
    private Audit audit;

    @JsonIgnore
    @ManyToOne
    private DepositChunk depositChunk;

    @Column(columnDefinition = "TEXT")
    private String archiveId;

    @Column(columnDefinition = "TEXT")
    private String location;

    // Additional properties might go here - e.g. format

    public AuditChunkStatus() {}

    public String getID() { return id; }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

//    public List<DepositChunk> getDepositChunks() { return chunks; }
//
//    public void setDepositChunks(List<DepositChunk> chunks) {
//        this.chunks = chunks;
//    }

    public String getNote() { return note; }

    public void setNote(String note) {
        this.note = note;
    }

    public Status getStatus() { return status; }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public void queuing() {
        this.status = Status.QUEUING;
    }

    public void started() {
        this.status = Status.IN_PROGRESS;
    }

    public void complete() {
        this.status = Status.COMPLETE;
    }

    public void failed(String note) {
        this.status = Status.ERROR;
        this.note = note;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }


    public DepositChunk getDepositChunk() {
        return depositChunk;
    }

    public void setDepositChunk(DepositChunk depositChunk) {
        this.depositChunk = depositChunk;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public AuditChunkStatusInfo convertToResponse() {
        return new AuditChunkStatusInfo(
                id,
                timestamp,
                completeTime,
                status,
                depositChunk,
                depositChunk.getDeposit(),
                archiveId,
                note
        );
    }
}
