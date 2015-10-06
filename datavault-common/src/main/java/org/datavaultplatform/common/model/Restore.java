package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Restores")
public class Restore {

    // Deposit Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;

    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @ManyToOne
    private Deposit deposit;

    // A Restore can have a number of events
    //@JsonIgnore
    //@OneToMany(targetEntity=Event.class, mappedBy="restore", fetch=FetchType.LAZY)
    //@OrderBy("timestamp, sequence")
    //private List<Event> events;

    // A Restore can have a number of active jobs
    //@JsonIgnore
    //@OneToMany(targetEntity=Job.class, mappedBy="restore", fetch=FetchType.LAZY)
    //@OrderBy("timestamp")
    //private List<Job> jobs;

    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETE
    }

    private String note;

    private Status status;

    String restorePath;
    
    // Additional properties might go here - e.g. format
    
    public Restore() {};

    public String getRestorePath() {
        return restorePath;
    }

    public void setRestorePath(String restorePath) {
        this.restorePath = restorePath;
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

//    public List<Event> getEvents() {
//        return events;
//    }

//    public List<Job> getJobs() {
//        return jobs;
//    }

}
