package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.retentionpolicy.PolicyStatus;
import org.datavaultplatform.common.response.GetVaultResponse;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:31
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Vaults")
public class Vault {

    // Vault Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;

    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    
    // Name of the vault
    @Column(name = "name", nullable = false)
    private String name;

    // Description of the vault
    private String description;

    // Size of the vault (in bytes)
    private long vaultSize;
    
    // A vault can contain a number of deposits
    @JsonIgnore
    @OneToMany(targetEntity=Deposit.class, mappedBy="vault", fetch=FetchType.EAGER)
    @OrderBy("creationTime")
    private List<Deposit> deposits;

    @ManyToOne
    private Policy policy;
    // Raw policy ID
    private String policyID;

    // Status of the policy
    private int policyStatus;

    // Date policy was last checked
    private Date policyLastChecked;

    @JsonIgnore
    @ManyToOne
    private Group group;
    // Raw group ID
    private String groupID;

    @ManyToOne
    private User user;
    
    public Vault() {}
    public Vault(String name) {
        this.name = name;
        this.creationTime = new Date();
        policyStatus = PolicyStatus.UNCHECKED;
    }

    public String getID() { return id; }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }

    public void setSize(long size) {
        this.vaultSize = size;
    }

    public long getSize() { return vaultSize; }
    
    public List<Deposit> getDeposits() {
        if (deposits == null) return new ArrayList();
        return deposits;
    }
    
    public void addDeposit(Deposit deposit) {
        this.deposits.add(deposit);
    }

    public Policy getPolicy() { return policy; }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public String getPolicyID() {
        return policyID;
    }

    public void setPolicyID(String policyID) {
        this.policyID = policyID;
    }

    public int getPolicyStatus() { return policyStatus; }

    public void setPolicyLastChecked(Date policyLastChecked) { this.policyLastChecked = policyLastChecked; }

    public Date getPolicyLastChecked() { return policyLastChecked; }

    public String getPolicyStatusString() {
        if (policyStatus == PolicyStatus.UNCHECKED) return "Un-checked";
        else if (policyStatus == PolicyStatus.OK) return "OK";
        else if (policyStatus == PolicyStatus.REVIEW) return "Review";
        else return ("Unknown");
    }

    public void setPolicyStatus(int policyStatus) { this.policyStatus = policyStatus; }

    public Group getGroup() { return group; }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public User getUser() { return user; }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public GetVaultResponse convertToResponse() {
        return new GetVaultResponse(
                id,
                creationTime,
                name,
                description,
                policy.getID(),
                groupID,
                vaultSize);
    }
    
    @Override
    public boolean equals(Object other) {
        
        if (this == other) return true;
        
        if (!(other instanceof Vault)) {
            return false;
        }
        
        final Vault vault = (Vault)other;
        
        if (!vault.getID().equals(getID())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        return getID().hashCode();
    }
}
