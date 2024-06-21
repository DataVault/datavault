package org.datavaultplatform.common.event;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.datavaultplatform.common.event.audit.*;
import org.datavaultplatform.common.event.client.*;
import org.datavaultplatform.common.event.delete.*;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.common.event.roles.*;
import org.datavaultplatform.common.event.vault.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.UuidGenerator;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.response.EventInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "eventClass", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AuditComplete.class, name = "org.datavaultplatform.common.event.audit.AuditComplete"),
        @JsonSubTypes.Type(value = AuditError.class, name = "org.datavaultplatform.common.event.audit.AuditError"),
        @JsonSubTypes.Type(value = AuditStart.class, name = "org.datavaultplatform.common.event.audit.AuditStart"),
        @JsonSubTypes.Type(value = ChunkAuditComplete.class, name = "org.datavaultplatform.common.event.audit.ChunkAuditComplete"),
        @JsonSubTypes.Type(value = ChunkAuditStarted.class, name = "org.datavaultplatform.common.event.audit.ChunkAuditStarted"),

        @JsonSubTypes.Type(value = Login.class, name = "org.datavaultplatform.common.event.client.Login"),
        @JsonSubTypes.Type(value = Logout.class, name = "org.datavaultplatform.common.event.client.Logout"),

        @JsonSubTypes.Type(value = DeleteStart.class, name = "org.datavaultplatform.common.event.delete.DeleteStart"),
        @JsonSubTypes.Type(value = DeleteComplete.class, name = "org.datavaultplatform.common.event.delete.DeleteComplete"),

        @JsonSubTypes.Type(value = ValidationComplete.class, name = "org.datavaultplatform.common.event.deposit.ValidationComplete"),
        @JsonSubTypes.Type(value = ComputedSize.class,       name = "org.datavaultplatform.common.event.deposit.ComputedSize"),
        @JsonSubTypes.Type(value = StartCopyUpload.class,    name = "org.datavaultplatform.common.event.deposit.StartCopyUpload"),
        @JsonSubTypes.Type(value = UploadComplete.class,     name = "org.datavaultplatform.common.event.deposit.UploadComplete"),
        @JsonSubTypes.Type(value = Complete.class,           name = "org.datavaultplatform.common.event.deposit.Complete"),

        @JsonSubTypes.Type(value = StartTarValidation.class, name = "org.datavaultplatform.common.event.deposit.StartTarValidation"),
        @JsonSubTypes.Type(value = ComputedEncryption.class, name = "org.datavaultplatform.common.event.deposit.ComputedEncryption"),
        @JsonSubTypes.Type(value = Start.class,              name = "org.datavaultplatform.common.event.deposit.Start"),
        @JsonSubTypes.Type(value = CompleteCopyUpload.class, name = "org.datavaultplatform.common.event.deposit.CompleteCopyUpload"),
        @JsonSubTypes.Type(value = TransferComplete.class,   name = "org.datavaultplatform.common.event.deposit.TransferComplete"),

        @JsonSubTypes.Type(value = PackageComplete.class,       name = "org.datavaultplatform.common.event.deposit.PackageComplete"),
        @JsonSubTypes.Type(value = ComputedChunks.class,        name = "org.datavaultplatform.common.event.deposit.ComputedChunks"),
        @JsonSubTypes.Type(value = TransferProgress.class,      name = "org.datavaultplatform.common.event.deposit.TransferProgress"),
        @JsonSubTypes.Type(value = ComputedDigest.class,        name = "org.datavaultplatform.common.event.deposit.ComputedDigest"),
        @JsonSubTypes.Type(value = CompleteTarValidation.class, name = "org.datavaultplatform.common.event.deposit.CompleteTarValidation"),

        @JsonSubTypes.Type(value = StartChunkValidation.class,    name = "org.datavaultplatform.common.event.deposit.StartChunkValidation"),
        @JsonSubTypes.Type(value = CompleteChunkValidation.class, name = "org.datavaultplatform.common.event.deposit.CompleteChunkValidation"),

        @JsonSubTypes.Type(value = ArchiveStoreRetrievedChunk.class,    name = "org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedChunk"),
        @JsonSubTypes.Type(value = UserStoreSpaceAvailableChecked.class, name = "org.datavaultplatform.common.event.retrieve.UserStoreSpaceAvailableChecked"),
        @JsonSubTypes.Type(value = UploadedToUserStore.class,    name = "org.datavaultplatform.common.event.retrieve.UploadedToUserStore"),
        
        @JsonSubTypes.Type(value = RetrieveError.class, name = "org.datavaultplatform.common.event.retrieve.RetrieveError"),
        @JsonSubTypes.Type(value = RetrieveStart.class,    name = "org.datavaultplatform.common.event.retrieve.RetrieveStart"),
        @JsonSubTypes.Type(value = RetrieveComplete.class, name = "org.datavaultplatform.common.event.retrieve.RetrieveComplete"),

        @JsonSubTypes.Type(value = ArchiveStoreRetrievedAll.class,    name = "org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedAll"),

        @JsonSubTypes.Type(value = CreateRoleAssignment.class,   name = "org.datavaultplatform.common.event.roles.CreateRoleAssignment"),
        @JsonSubTypes.Type(value = DeleteRoleAssignment.class,   name = "org.datavaultplatform.common.event.roles.DeleteRoleAssignment"),
        @JsonSubTypes.Type(value = OrphanVault.class,            name = "org.datavaultplatform.common.event.roles.OrphanVault"),
        @JsonSubTypes.Type(value = TransferVaultOwnership.class, name = "org.datavaultplatform.common.event.roles.TransferVaultOwnership"),
        @JsonSubTypes.Type(value = UpdateRoleAssignment.class,   name = "org.datavaultplatform.common.event.roles.UpdateRoleAssignment"),

        @JsonSubTypes.Type(value = Create.class,             name = "org.datavaultplatform.common.event.vault.Create"),
        @JsonSubTypes.Type(value = Pending.class,            name = "org.datavaultplatform.common.event.vault.Pending"),
        @JsonSubTypes.Type(value = Review.class,             name = "org.datavaultplatform.common.event.vault.Review"),
        @JsonSubTypes.Type(value = UpdatedDescription.class, name = "org.datavaultplatform.common.event.vault.UpdatedDescription"),
        @JsonSubTypes.Type(value = UpdatedName.class,        name = "org.datavaultplatform.common.event.vault.UpdatedName"),

        @JsonSubTypes.Type(value = Event.class,            name = "org.datavaultplatform.common.event.Event"),
        @JsonSubTypes.Type(value = Error.class,            name = "org.datavaultplatform.common.event.Error"),
        @JsonSubTypes.Type(value = InitStates.class,       name = "org.datavaultplatform.common.event.InitStates"),
        @JsonSubTypes.Type(value = UpdateProgress.class,   name = "org.datavaultplatform.common.event.UpdateProgress")
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Events")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="eventType", discriminatorType = DiscriminatorType.STRING, columnDefinition = "VARCHAR(31)")
@NamedEntityGraph(name = Event.EG_EVENT, attributeNodes = {
    @NamedAttributeNode(value = Event_.ARCHIVE, subgraph = "subArchive"),
    @NamedAttributeNode(value = Event_.DEPOSIT, subgraph = "subDeposit"),
    @NamedAttributeNode(value = Event_.ASSIGNEE),
    @NamedAttributeNode(value = Event_.AUDIT),

    @NamedAttributeNode(value = Event_.CHUNK, subgraph = "subChunk"),
    @NamedAttributeNode(value = Event_.JOB, subgraph = "subJob"),
    @NamedAttributeNode(value = Event_.ROLE),
    @NamedAttributeNode(value = Event_.SCHOOL),

    @NamedAttributeNode(value = Event_.USER),
    @NamedAttributeNode(value = Event_.VAULT, subgraph = "subVault")
}, subgraphs = {
    @NamedSubgraph(name="subArchive", attributeNodes = {
        @NamedAttributeNode(Archive_.ARCHIVE_STORE),
        @NamedAttributeNode(Archive_.DEPOSIT)
    }),
    @NamedSubgraph(name="subDeposit", attributeNodes = {
        @NamedAttributeNode(Deposit_.USER),
        @NamedAttributeNode(Deposit_.VAULT)
    }),
    @NamedSubgraph(name="subJob", attributeNodes = {
        @NamedAttributeNode(Job_.DEPOSIT)
    }),
    @NamedSubgraph(name="subChunk", attributeNodes = {
        @NamedAttributeNode(DepositChunk_.DEPOSIT)
    }),
    @NamedSubgraph(name="subVault", attributeNodes = {
        @NamedAttributeNode(Vault_.DATASET),
        @NamedAttributeNode(Vault_.GROUP),
        @NamedAttributeNode(Vault_.RETENTION_POLICY),
        @NamedAttributeNode(Vault_.USER)
    })
})
public class Event {
    public static final String EG_EVENT = "eg.Event.1";

    // Event Identifier
    @Id
    @UuidGenerator
    @Column(name = "id", unique = true, length = 36)
    private String id;
    
    // Event properties
    @Lob
    @Column(name = "message", columnDefinition = "LONGTEXT", length = 4000)
    public String message;
    public String retrieveId;
    public String eventClass;
    
    @Transient
    public Integer nextState = null;
    
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= DateTimeUtils.ISO_DATE_TIME_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    public int sequence;

    // By default events are persisted
    @Transient
    public Boolean persistent = true;
    
    // Related deposit
    @JsonIgnore
    @ManyToOne
    private Deposit deposit;
    @Transient
    public String depositId;
    
    // Related vault
    @JsonIgnore
    @ManyToOne
    private Vault vault;
    @Transient
    public String vaultId;
    
    // Related job
    @JsonIgnore
    @ManyToOne
    private Job job;
    @Transient
    public String jobId;
    
    // Related user
    @JsonIgnore
    @ManyToOne
    private User user;
    @Transient
    public String userId;
    
    // Event agent
    private String agent;
    
    // Web request properties
    public String remoteAddress;
    public String userAgent;
    
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(255)")
    private Agent.AgentType agentType;

    // For Audit

    // Related audit
    @JsonIgnore
    @ManyToOne
    private Audit audit;
    @Transient
    public String auditId;

    // Related archive
    @JsonIgnore
    @ManyToOne
    private Archive archive;
    @Transient
    public String archiveId;

    private String location;

    // Related deposit chunk
    @JsonIgnore
    @ManyToOne
    private DepositChunk chunk;
    @Transient
    public String chunkId;

    // Related Role assignee
    @JsonIgnore
    @ManyToOne
    private User assignee;
    @Transient
    public String assigneeId;

    // Related Role school target
    @JsonIgnore
    @ManyToOne
    private Group school;
    @Transient
    public String schoolId;

    // Related Role target
    @JsonIgnore
    @ManyToOne
    private RoleModel role;
    @Transient
    public String roleId;

    @Getter
    @Setter
    @Column(name = "chunkNumber", columnDefinition = "INT DEFAULT NULL")
    private Integer chunkNumber;

    @Getter
    @Setter
    @Column(name = "archive_store_id", columnDefinition = "VARCHAR(256) DEFAULT NULL")
    private String archiveStoreId;
    
    public Event() {
        this.eventClass = this.getClass().getCanonicalName();
    }

    public Event(String message) {
        this();
        this.message = message;
        this.timestamp = new Date();
        this.sequence = 0;
    }

    public Event(String jobId, String depositId, String retrieveId, String message) {
        this.eventClass = Event.class.getCanonicalName();
        this.jobId = jobId;
        this.depositId = depositId;
        this.retrieveId = retrieveId;
        this.auditId = null;
        this.message = message;
        this.timestamp = new Date();
        this.sequence = 0;
    }
    
    public Event(String jobId, String auditId, String chunkId, String archiveId, String location, String message) {
        this.eventClass = Event.class.getCanonicalName();
        this.jobId = jobId;
        this.auditId = auditId;
        this.chunkId = chunkId;
        this.archiveId = archiveId;
        this.location = location;
        this.message = message;
        this.timestamp = new Date();
        this.sequence = 0;
    }

    public String getID() { return id; }

    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getDepositId() {
        return depositId;
    }

    public void setDepositId(String depositId) {
        this.depositId = depositId;
    }

    public String getVaultId() {
        return vaultId;
    }

    public void setVaultId(String vaultId) {
        this.vaultId = vaultId;
    }
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public Agent.AgentType getAgentType() {
        return agentType;
    }

    public void setAgentType(Agent.AgentType agentType) {
        this.agentType = agentType;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getRetrieveId() {
        return retrieveId;
    }

    public void setRetrieveId(String retrieveId) {
        this.retrieveId = retrieveId;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    public void setPersistent(Boolean persistent) {
        this.persistent = persistent;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }
    
    public Deposit getDeposit() {
        return deposit;
    }

    public void setDeposit(Deposit deposit) {
        this.deposit = deposit;
    }

    public Vault getVault() {
        return vault;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public Event withNextState(Integer state) {
        this.nextState = state;
        return this;
    }
    
    public Event withUserId(String userId) {
        this.userId = userId;
        return this;
    }
    
    public EventInfo convertToResponse() {
        return new EventInfo(id,
                timestamp,
                message,
                user != null ? user.getID() : null,
                vault != null ? vault.getID() : null,
                eventClass,
                deposit != null ? deposit.getID() : null,
                agent,
                agentType != null ? agentType.toString() : null,
                remoteAddress,
                userAgent);
    }

    public String getAuditId() { return auditId; }

    public void setAuditId(String auditId) { this.auditId = auditId; }

    public String getChunkId() { return chunkId; }

    public void setChunkId(String chunkId) { this.chunkId = chunkId; }

    public String getArchiveId() { return archiveId; }

    public void setArchiveId(String archiveId) { this.archiveId = archiveId; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public Group getSchool() {
        return school;
    }

    public void setSchool(Group school) {
        this.school = school;
    }

    public RoleModel getRole() {
        return role;
    }

    public void setRole(RoleModel role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Event event = (Event) o;
        return id != null && Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Event refreshIdFields() {
        setUserId(getIdentifiedId(getUser()));
        setDepositId(getIdentifiedId(getDeposit()));
        setJobId(getIdentifiedId(getJob()));
        setVaultId(getIdentifiedId(getVault()));
        return this;
    }

    private String getIdentifiedId(Identified identified) {
        if (identified == null) {
            return null;
        } else {
            return identified.getID();
        }
    }
}
