package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.datavaultplatform.common.response.AuditChunkStatusInfo;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="AuditChunkStatus")
@NamedEntityGraph(
    name=AuditChunkStatus.EG_AUDIT_CHUNK_STATUS,
    attributeNodes = {
        @NamedAttributeNode(AuditChunkStatus_.AUDIT),
        @NamedAttributeNode(value = AuditChunkStatus_.DEPOSIT_CHUNK, subgraph = "subDepositChunk")
    },
    subgraphs = @NamedSubgraph(
        name="subDepositChunk",
        attributeNodes = @NamedAttributeNode(DepositChunk_.DEPOSIT)
    )
)
public class AuditChunkStatus {

    public static final String EG_AUDIT_CHUNK_STATUS = "eg.AuditChunkStatus.1";
    // Deposit Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= DateTimeUtils.ISO_DATE_TIME_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DateTimeUtils.ISO_DATE_TIME_FORMAT)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        AuditChunkStatus that = (AuditChunkStatus) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
