package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Retrieves")
@NamedEntityGraph(
    name=Retrieve.EG_RETRIEVE,
    attributeNodes = {
        @NamedAttributeNode(value = Retrieve_.DEPOSIT, subgraph = "subDeposit"),
        @NamedAttributeNode(Retrieve_.USER)},
    subgraphs = @NamedSubgraph(name="subDeposit",
        attributeNodes = {
        @NamedAttributeNode(Deposit_.VAULT),
        @NamedAttributeNode(Deposit_.USER)
    })
)
public class Retrieve {

    public static final String EG_RETRIEVE = "eg.Retrieve.1";
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

    @ManyToOne
    private Deposit deposit;

    // A Retrieve can have a number of events
    //@JsonIgnore
    //@OneToMany(targetEntity=Event.class, mappedBy="retrieve", fetch=FetchType.LAZY)
    //@OrderBy("timestamp, sequence")
    //private List<Event> events;

    // A Retrieve can have a number of active jobs
    //@JsonIgnore
    //@OneToMany(targetEntity=Job.class, mappedBy="retrieve", fetch=FetchType.LAZY)
    //@OrderBy("timestamp")
    //private List<Job> jobs;

    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETE
    }

    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(columnDefinition = "TEXT")
    private Status status;

    @Column(columnDefinition = "TEXT")
    String retrievePath;
    
    @Column(nullable = false)
    private Boolean hasExternalRecipients;
    
    @ManyToOne
    private User user;
    
    // Additional properties might go here - e.g. format
    
    public Retrieve() {}

    public String getID() { return id; }

    public String getRetrievePath() {
        return retrievePath;
    }

    public void setRetrievePath(String retrievePath) {
        this.retrievePath = retrievePath;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Deposit getDeposit() { return deposit; }

    public void setDeposit(Deposit deposit) {
        this.deposit = deposit;
    }

    public String getNote() { return note; }

    public void setNote(String note) {
        this.note = note;
    }

    public Status getStatus() { return status; }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Boolean getHasExternalRecipients() {
        return hasExternalRecipients;
    }

    public void setHasExternalRecipients(Boolean hasExternalRecipients) {
        this.hasExternalRecipients = hasExternalRecipients;
    }

    public User getUser() { return user; }
    
    public void setUser(User user) {
        this.user = user;
    }

//    public List<Event> getEvents() {
//        return events;
//    }

//    public List<Job> getJobs() {
//        return jobs;
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Retrieve retrieve = (Retrieve) o;
        return id != null && Objects.equals(id, retrieve.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
