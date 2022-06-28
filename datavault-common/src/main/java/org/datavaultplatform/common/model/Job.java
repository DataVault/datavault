package org.datavaultplatform.common.model;

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
import javax.persistence.Lob;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.datavaultplatform.common.event.Event;
import org.hibernate.annotations.GenericGenerator;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Jobs")
@NamedEntityGraph(
    name=Job.EG_JOB,
    attributeNodes = @NamedAttributeNode(value = Job_.DEPOSIT, subgraph = "subDeposit"),
    subgraphs = @NamedSubgraph(name="subDeposit", attributeNodes = {
        @NamedAttributeNode(Deposit_.USER),
        @NamedAttributeNode(Deposit_.VAULT)
    })
)
public class Job {

    public static final String EG_JOB = "eg.Job.1";

    // Job Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;
    
    // Hibernate version
    @Version
    private long version;
    
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
    
    @Column(columnDefinition = "TEXT")
    private String taskClass;
    
    // State information will be supplied by the worker task at run time
    @Lob
    private ArrayList<String> states = new ArrayList<>();
    private Integer state = null;
    
    // A generic indicator of progress
    private long progress;
    private long progressMax;
    
    @Column(columnDefinition = "TEXT")
    private String progressMessage;
    
    // Error information
    private boolean error;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    public Job() {}

    public Job(String taskClass) {
        this.taskClass = taskClass;
    }
    
    public String getID() { return id; }
    
    public long getVersion() { return version; }

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

    public String getTaskClass() {
        return taskClass;
    }

    public void setTaskClass(String taskClass) {
        this.taskClass = taskClass;
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

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public long getProgressMax() {
        return progressMax;
    }

    public void setProgressMax(long progressMax) {
        this.progressMax = progressMax;
    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    @Override
    public boolean equals(Object obj){
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Job rhs = (Job) obj;
        return new EqualsBuilder()
            .append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
            append(id).toHashCode();
    }
}
