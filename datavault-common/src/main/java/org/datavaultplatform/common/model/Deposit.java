package org.datavaultplatform.common.model;

import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.event.Event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Version;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "Deposit")
@Entity
@Table(name="Deposits")
@NamedEntityGraph(
    name=Deposit.EG_DEPOSIT,
    attributeNodes = {
        @NamedAttributeNode(value = Deposit_.VAULT, subgraph = "subVault"),
        @NamedAttributeNode("user")
    },
    subgraphs = @NamedSubgraph(name="subVault", attributeNodes = {
        @NamedAttributeNode(Vault_.RETENTION_POLICY),
        @NamedAttributeNode(Vault_.GROUP),
        @NamedAttributeNode(Vault_.DATASET)
    })
)
public class Deposit {

    public static final String EG_DEPOSIT = "eg.Deposit.1";
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

    // A Deposit can have many Archives
    @JsonIgnore
    @OneToMany(targetEntity = Archive.class, mappedBy = "deposit", fetch=FetchType.LAZY)
    @OrderBy("creationTime")
    private List<Archive> archives;

    // A Deposit can have a number of events
    @JsonIgnore
    @OneToMany(targetEntity=Event.class, mappedBy="deposit", fetch=FetchType.LAZY)
    @OrderBy("timestamp, sequence")
    private List<Event> events;
    
    // A Deposit can have a number of deposit paths
    @OneToMany(targetEntity=DepositPath.class, mappedBy="deposit", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DepositPath> depositPaths;
    
    // A Deposit can have a number of deposit chunks
    @OneToMany(targetEntity=DepositChunk.class, mappedBy="deposit", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DepositChunk> depositChunks;
    
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

    // A Deposit can have a number of reviews
    @JsonIgnore
    @OneToMany(targetEntity=DepositReview.class, mappedBy="deposit", fetch=FetchType.LAZY)
    @OrderBy("creationTime")
    private List<DepositReview> depositReviews;


    @ApiObjectField(description = "Status of the Deposit", allowedvalues={"NOT_STARTED", "IN_PROGRESS", "COMPLETE"})
    private Status status;

    // THE ORDER OF THESE ENUM VALUES IS IMPORTANT. DO NOT CHANGE.
    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETE,
        FAILED,
        DELETE_IN_PROGRESS,
        DELETED,
        DELETE_FAILED
    }

    @ApiObjectField(description = "Deposit name to briefly describe the Deposit")
    @Column(columnDefinition = "TEXT", nullable = false, length = 400)
    private String name;

    @ApiObjectField(description = "Deposit description to provide more information about the Deposit")
    @Column(columnDefinition = "TEXT", length = 6000)
    private String description;
    
    @ApiObjectField(description = "Whether the deposit contains personal data or not")
    @Column(nullable = false)
    private Boolean hasPersonalData;
    
    @ApiObjectField(description = "Description of the nature of the personal data")
    @Column(columnDefinition = "TEXT", length = 6000)
    private String personalDataStatement;

    // For now, a deposit relates to a single bag.
    @ApiObjectField(description = "ID of the bag associated with this Deposit")
    @Column(columnDefinition = "TEXT")
    private String bagId;

    // Size of the deposit package (in bytes)
    @Column(columnDefinition = "TEXT")
    private long archiveSize;
    
    // Hash of the deposit package
    @Column(columnDefinition = "TEXT")
    private String archiveDigest;
    @Column(columnDefinition = "TEXT")
    private String archiveDigestAlgorithm;
    
    // Number of Chunks
    @Column(columnDefinition = "INT default 0")
    private int numOfChunks;
    @Column(columnDefinition = "TEXT")
    private String encArchiveDigest;
    
    // Encryption
    @Column(columnDefinition = "BLOB")
    private byte[] encIV;

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
    @ApiObjectField(description = "Size of the deposit (in bytes)")
    private long depositSize;
    
    @ManyToOne
    private User user;
    
    public Deposit() {}
    public Deposit(String name, String description, Boolean hasPersonalData) {
        this.name = name;
        this.description = description;
        this.hasPersonalData = hasPersonalData;
        this.status = Status.NOT_STARTED;
    }
    
    public String getID() { return id; }

    public long getVersion() { return version; }

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

    public List<Archive> getArchives() {
        return archives;
    }

    public String getName() { return name; }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() { return description; }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getHasPersonalData() {
        return hasPersonalData;
    }
    
    public void setHasPersonalData(Boolean hasPersonalData) {
        this.hasPersonalData = hasPersonalData;
    }
    
    public String getPersonalDataStatement() {
        return personalDataStatement;
    }
    
    public void setPersonalDataStatement(String personalDataStatement) {
        this.personalDataStatement = personalDataStatement;
    }
    
    public Status getStatus() { return status; }

    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getBagId() { return bagId; }
    
    public void setBagId(String bagId) {
        this.bagId = bagId;
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
    

    public int getNumOfChunks() {
        return numOfChunks;
    }

    public void setNumOfChunks(int numOfChunks) {
        this.numOfChunks = numOfChunks;
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

    public void setDepositChunks(List<DepositChunk> depositChunks) {
        this.depositChunks = depositChunks;
    }
    
    public List<DepositChunk> getDepositChunks() {
        return depositChunks;
    }

    public void setDepositPaths(List<DepositPath> depositPaths) {
        this.depositPaths = depositPaths;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public List<Retrieve> getRetrieves() { return retrieves; }

    public List<DepositReview> getDepositReviews() {
        return depositReviews;
    }

    public void setDepositReviews(List<DepositReview> depositReviews) {
        this.depositReviews = depositReviews;
    }

    public byte[] getEncIV() { return encIV; }
    
    public void setEncIV(byte[] encIV) { this.encIV = encIV; }
    
    public String getEncArchiveDigest() { return encArchiveDigest; }
    
    public void setEncArchiveDigest(String encArchiveDigest) {  this.encArchiveDigest = encArchiveDigest; }
    
    public User getUser() { return user; }
    
    public void setUser(User user) {
        this.user = user;
    }

    @JsonIgnore
    public Event getLastEvent(){
        Event lastEvent = events
                .stream()
                .max(Comparator.comparing(Event::getSequence))
                .orElse(null);
        return lastEvent;
    }

    @JsonIgnore
    public Event getLastNotFailedEvent(){
        Event lastEvent = events
                .stream()
                .filter(event-> !"org.datavaultplatform.common.event.Error".equals(event.getEventClass()))
                .max(Comparator.comparing(Event::getSequence))
                .orElse(null);
        return lastEvent;
    }
    
    public DepositInfo convertToResponse() {
        return new DepositInfo(
                id,
                user.getID(),
                creationTime,
                status,
                name,
                description,
                hasPersonalData,
                personalDataStatement,
                fileOrigin,
                shortFilePath,
                filePath,
                depositSize,
                vault.getID(),
                depositPaths,
                depositChunks
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
        Deposit deposit = (Deposit) o;
        return id != null && Objects.equals(id, deposit.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
