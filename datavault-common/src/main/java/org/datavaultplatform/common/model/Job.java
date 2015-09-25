package org.datavaultplatform.common.model;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
    
    // TODO: we need a more robust/generic approach to deposit state
    // Handle with inheritance?
    public enum Status {
        NOT_STARTED,
        CALCULATE_SIZE, // other pre-flight checks?
        TRANSFER_FILES,
        CREATE_PACKAGE,
        STORE_ARCHIVE_PACKAGE,
        COMPLETE
    }
    
    private Status status;
    
    // Number of bytes ingested/transferred
    private long bytesTransferred;
    
    // Transfer rate
    private long bytesPerSec;
    
    public Job() {
        this.status = Status.NOT_STARTED;
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

    public Status getStatus() { return status; }

    public void setStatus(Status status) {
        this.status = status;
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
