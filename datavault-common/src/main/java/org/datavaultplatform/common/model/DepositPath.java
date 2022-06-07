package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "DepositPath")
@Entity
@Table(name="DepositPaths")
public class DepositPath {

    // Deposit Identifier
    @Id
    @ApiObjectField(description = "Universally Unique Identifier for the Deposit Path", name="Deposit Path")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;
    
    @JsonIgnore
    @ManyToOne
    private Deposit deposit;
    
    // Record the file path that the user selected for this deposit.
    @ApiObjectField(description = "Filepath of the origin deposit")
    @Column(columnDefinition = "TEXT")
    private String filePath;
    
    // Record the type of this path (e.g. is it a user upload or on a server filestore).
    private Path.PathType pathType;
    
    public DepositPath() {}
    public DepositPath(Deposit deposit, String filePath, Path.PathType pathType) {
        this.deposit = deposit;
        this.filePath = filePath;
        this.pathType = pathType;
    }
    
    public String getID() { return id; }

    public Deposit getDeposit() {
        return deposit;
    }

    public void setDeposit(Deposit deposit) {
        this.deposit = deposit;
    }
    
    public String getFilePath() { return filePath; }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Path.PathType getPathType() {
        return pathType;
    }

    public void setPathType(Path.PathType pathType) {
        this.pathType = pathType;
    }
}
