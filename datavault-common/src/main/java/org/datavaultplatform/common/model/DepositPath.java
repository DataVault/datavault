package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "DepositPath")
@Entity
@Table(name="DepositPaths")
@NamedEntityGraph(
    name = DepositPath.EG_DEPOSIT_PATH,
    attributeNodes =
    @NamedAttributeNode(value = DepositPath_.DEPOSIT, subgraph = "subDeposit"),
    subgraphs = @NamedSubgraph(name = "subDeposit", attributeNodes = {
        @NamedAttributeNode(Deposit_.VAULT),
        @NamedAttributeNode(Deposit_.USER)
    })
)
public class DepositPath {

    public static final String EG_DEPOSIT_PATH =  "eg.DepositPath.1";

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

    @Override
    public boolean equals(Object obj){
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        DepositPath rhs = (DepositPath) obj;
        return new EqualsBuilder()
            .append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
            append(id).toHashCode();
    }
}
