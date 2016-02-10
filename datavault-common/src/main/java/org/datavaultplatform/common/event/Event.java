package org.datavaultplatform.common.event;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorColumn;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.GenericGenerator;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Job;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Events")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="eventType", discriminatorType = DiscriminatorType.STRING)
public class Event {
    
    // Event Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;
    
    // Event properties
    public String message;
    public String depositId;
    public String restoreId;
    public String eventClass;
    
    @Transient
    public Integer nextState = null;
    
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
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
    
    // Related job
    @JsonIgnore
    @ManyToOne
    private Job job;
    
    // Non-hibernate Job ID set by worker on event creation
    @Transient
    public String jobId;
    
    public Event() {};
    public Event(String jobId, String depositId, String message) {
        this.eventClass = Event.class.getCanonicalName();
        this.jobId = jobId;
        this.depositId = depositId;
        this.message = message;
        this.timestamp = new Date();
        this.sequence = 0;
    }

    public Event(String jobId, String depositId, String restoreId, String message) {
        this.eventClass = Event.class.getCanonicalName();
        this.jobId = jobId;
        this.depositId = depositId;
        this.restoreId = restoreId;
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

    public String getRestoreId() {
        return restoreId;
    }

    public void setRestoreId(String restoreId) {
        this.restoreId = restoreId;
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
    
    public Event withNextState(Integer state) {
        this.nextState = state;
        return this;
    }
}
