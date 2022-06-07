package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.datavaultplatform.common.response.AuditInfo;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Audits")
public class Audit {

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

}
