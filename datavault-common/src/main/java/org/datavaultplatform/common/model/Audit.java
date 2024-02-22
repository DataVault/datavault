package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import org.datavaultplatform.common.response.AuditInfo;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Audits")
@NamedEntityGraph(name=Audit.EG_AUDIT)
public class Audit {

    public static final String EG_AUDIT = "eg.Audit.1";
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

    //@OneToMany(targetEntity = DepositChunk.class, mappedBy = "deposit", fetch=FetchType.LAZY)
    //THIS WAS A BUG - NOW TRANSIENT
    @Transient
    private List<DepositChunk> chunks;

    @OneToMany(targetEntity = AuditChunkStatus.class, mappedBy = "audit", fetch=FetchType.LAZY)
    private List<AuditChunkStatus> auditChunkStatuses;

    public enum Status {
        FAILED,
        IN_PROGRESS,
        COMPLETE
    }

    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(columnDefinition = "TEXT")
    private Status status;
    
    // Additional properties might go here - e.g. format
    
    public Audit() {}

    public String getID() { return id; }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public List<DepositChunk> getDepositChunks() { return chunks; }

    public void setDepositChunks(List<DepositChunk> chunks) {
        this.chunks = chunks;
    }

    public List<AuditChunkStatus> getAuditChunkStatuses() {
        return auditChunkStatuses;
    }

    public void setAuditChunkStatuses(
        List<AuditChunkStatus> auditChunkStatuses) {
        this.auditChunkStatuses = auditChunkStatuses;
    }

    public String getNote() { return note; }

    public void setNote(String note) {
        this.note = note;
    }

    public Status getStatus() { return status; }

    public void setStatus(Status status) {
        this.status = status;
    }

    public AuditInfo convertToResponse() {
        return new AuditInfo(
            id,
            timestamp,
            status
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
        Audit audit = (Audit) o;
        return id != null && Objects.equals(id, audit.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
