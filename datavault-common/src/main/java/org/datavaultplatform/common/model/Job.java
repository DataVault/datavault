package org.datavaultplatform.common.model;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.datavaultplatform.common.event.Event;
import org.hibernate.annotations.GenericGenerator;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Jobs")
public class Job {

    // Job Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;
    
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    
    @JsonIgnore
    @ManyToOne
    private Deposit deposit;
    
    // A Job can have a number of events
    @JsonIgnore
    @OneToMany(targetEntity=Event.class, mappedBy="job", fetch=FetchType.LAZY)
    @OrderBy("timestamp, sequence")
    private List<Event> events;
    
    private String jobClass;
    
    // State information will be supplied by the worker task at run time
    private ArrayList<String> states = new ArrayList<>();
    private Integer state = null;
    
    // Number of bytes ingested/transferred
    private long bytesTransferred;
    
    // Transfer rate
    private long bytesPerSec;

    public Job() {};
    public Job(String jobClass) {
        this.jobClass = jobClass;
    }
    
    public String getID() { return id; }

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

    public String getJobClass() { return jobClass; }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public ArrayList<String> getStates() { return states; }

    public void setStates(ArrayList<String> states) {
        this.states = states;
    }
    
    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
    
    public long getBytesTransferred() { return bytesTransferred; }
    
    public String getBytesTransferredStr() {
        return FileUtils.byteCountToDisplaySize(bytesTransferred);
    }

    public void setBytesTransferred(long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
    }

    public long getBytesPerSec() { return bytesPerSec; }
    
    public String getBytesPerSecStr() {
        return FileUtils.byteCountToDisplaySize(bytesPerSec);
    }

    public void setBytesPerSec(long bytesPerSec) {
        this.bytesPerSec = bytesPerSec;
    }
}
