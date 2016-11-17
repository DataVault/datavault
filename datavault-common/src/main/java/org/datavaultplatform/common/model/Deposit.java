package org.datavaultplatform.common.model;

import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.event.Event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.OrderBy;
import javax.persistence.Version;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "Deposit")
@Entity
@Table(name="Deposits")
public class Deposit {

    // Deposit Identifier
    @Id
    @ApiObjectField(description = "Universally Unique Identifier for the Deposit", name="Deposit")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // Hibernate version
    @Version
    private long version;
    
    // Serialise date in ISO 8601 format
    @ApiObjectField(description = "Date that the vault was created")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    
    @ManyToOne
    private Vault vault;
    
    // A Deposit can have a number of events
    @JsonIgnore
    @OneToMany(targetEntity=Event.class, mappedBy="deposit", fetch=FetchType.LAZY)
    @OrderBy("timestamp, sequence")
    private List<Event> events;
    
    // A Deposit can have a number of deposit paths
    @OneToMany(targetEntity=DepositPath.class, mappedBy="deposit", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DepositPath> depositPaths;
    
    // A Deposit can have a number of active jobs
    @JsonIgnore
    @OneToMany(targetEntity=Job.class, mappedBy="deposit", fetch=FetchType.LAZY)
    @OrderBy("timestamp")
    private List<Job> jobs;
    
    // A Deposit can have a number of retrieves
    @JsonIgnore
    @OneToMany(targetEntity=Retrieve.class, mappedBy="deposit", fetch=FetchType.LAZY)
    @OrderBy("timestamp")
    private List<Retrieve> retrieves;

    @ApiObjectField(description = "Status of the Deposit", allowedvalues={"NOT_STARTED", "IN_PROGRESS", "COMPLETE"})
    private Status status;
    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETE,
        FAILED
    }

    @ApiObjectField(description = "Deposit note to briefly describe the Deposit")
    @Column(columnDefinition = "TEXT")
    private String note;

    // For now, a deposit relates to a single bag.
    @ApiObjectField(description = "ID of the bag associated with this Deposit")
    @Column(columnDefinition = "TEXT")
    private String bagId;
    
    // Archive properties (e.g. archive storage path or ID)
    @Column(columnDefinition = "TEXT")
    private String archiveDevice;
    
    @Column(columnDefinition = "TEXT")
    private String archiveId;
    
    // Size of the deposit package (in bytes)
    @Column(columnDefinition = "TEXT")
    private long archiveSize;
    
    // Hash of the deposit package
    @Column(columnDefinition = "TEXT")
    private String archiveDigest;
    @Column(columnDefinition = "TEXT")
    private String archiveDigestAlgorithm;
    
    // Record the file path that the user selected for this deposit.
    @ApiObjectField(description = "Origin of the deposited filepath")
    @Column(columnDefinition = "TEXT")
    private String fileOrigin;
    @ApiObjectField(description = "Short version of the origin of the deposited filepath")
    @Column(columnDefinition = "TEXT")
    private String shortFilePath;
    @ApiObjectField(description = "Filepath of the origin deposit")
    @Column(columnDefinition = "TEXT")
    private String filePath;
    
    // Size of the deposit (in bytes)
    @ApiObjectField(description = "Size of the depoit (in bytes)")
    private long depositSize;
    
    public Deposit() {}
    public Deposit(String note) {
        this.note = note;
        this.status = Status.NOT_STARTED;
    }
    
    public String getID() { return id; }

    public long getVersion() { return version; };
    
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

    public String getArchiveDevice() {
        return archiveDevice;
    }

    public void setArchiveDevice(String archiveDevice) {
        this.archiveDevice = archiveDevice;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public long getArchiveSize() {
        return archiveSize;
    }

    public void setArchiveSize(long archiveSize) {
        this.archiveSize = archiveSize;
    }

    public String getArchiveDigest() {
        return archiveDigest;
    }

    public void setArchiveDigest(String archiveDigest) {
        this.archiveDigest = archiveDigest;
    }

    public String getArchiveDigestAlgorithm() {
        return archiveDigestAlgorithm;
    }

    public void setArchiveDigestAlgorithm(String archiveDigestAlgorithm) {
        this.archiveDigestAlgorithm = archiveDigestAlgorithm;
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
    
    public List<Event> getEvents() {
        return events;
    }
    
    public List<DepositPath> getDepositPaths() {
        return depositPaths;
    }

    public void setDepositPaths(List<DepositPath> depositPaths) {
        this.depositPaths = depositPaths;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public List<Retrieve> getRetrieves() { return retrieves; }
    
    public DepositInfo convertToResponse() {
        return new DepositInfo(
                id,
                creationTime,
                status,
                note,
                fileOrigin,
                shortFilePath,
                filePath,
                depositSize,
                vault.getID(),
                depositPaths
            );
    }
}
