package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.datavaultplatform.common.response.AuditInfo;
import org.datavaultplatform.common.util.DateTimeUtils;
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
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= DateTimeUtils.ISO_DATE_TIME_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @OneToMany(targetEntity = DepositChunk.class, mappedBy = "deposit", fetch=FetchType.LAZY)
    private List<DepositChunk> chunks;

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
