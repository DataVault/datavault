package org.datavaultplatform.common.model;

import org.datavaultplatform.common.event.Event;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.OrderBy;
import org.hibernate.annotations.GenericGenerator;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Deposits")
public class Deposit {

    // Deposit Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;
    
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    
    @JsonIgnore
    @ManyToOne
    private Vault vault;
    
    // A Deposit can have a number of events
    @JsonIgnore
    @OneToMany(targetEntity=Event.class, mappedBy="deposit", fetch=FetchType.LAZY)
    @OrderBy("timestamp, sequence")
    private List<Event> events;
    
    // A Deposit can have a number of active jobs
    @JsonIgnore
    @OneToMany(targetEntity=Job.class, mappedBy="deposit", fetch=FetchType.LAZY)
    @OrderBy("timestamp")
    private List<Job> jobs;
    
    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETE
    }
    
    private String note;
    private Status status;
    
    // For now, a deposit relates to a single bag.
    private String bagId;
    
    // Record the file path that the user selected for this deposit.
    private String fileOrigin;
    private String shortFilePath;
    private String filePath;
    
    // Size of the deposit (in bytes)
    private long depositSize;
    
    public Deposit() {}
    public Deposit(String note) {
        this.note = note;
        this.status = Status.NOT_STARTED;
    }
    
    public String getID() { return id; }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }
    
    public Vault getVault() { return vault; }
    
    public void setVault(Vault vault) {
        this.vault = vault;
    }
    
    public String getNote() { return note; }
    
    public void setNote(String note) {
        this.note = note;
    }

    public Status getStatus() { return status; }

    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getBagId() { return bagId; }
    
    public void setBagId(String bagId) {
        this.bagId = bagId;
    }

    public String getFileOrigin() {
        return fileOrigin;
    }

    public void setFileOrigin(String fileOrigin) {
        this.fileOrigin = fileOrigin;
    }
    
    public String getFilePath() { return filePath; }

    public String getShortFilePath() {
        return shortFilePath;
    }

    public void setShortFilePath(String shortFilePath) {
        this.shortFilePath = shortFilePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public void setSize(long size) {
        this.depositSize = size;
    }

    public long getSize() { return depositSize; }

    public String getSizeStr() {
        return FileUtils.byteCountToDisplaySize(depositSize);
    }
    
    public List<Event> getEvents() {
        return events;
    }

    public List<Job> getJobs() {
        return jobs;
    }
}
