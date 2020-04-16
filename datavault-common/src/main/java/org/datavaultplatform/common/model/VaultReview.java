package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "VaultReview")
@Entity
@Table(name="VaultReviews")
public class VaultReview {

    // VaultReview Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @ManyToOne
    private Vault vault;

    // A VaultReview can contain a number of DepositReviews
    @JsonIgnore
    @OneToMany(targetEntity=DepositReview.class, mappedBy="vaultReview", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("creationTime")
    private List<DepositReview> depositReviews;

    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "newReviewDate", nullable = true)
    private Date newReviewDate;

    // The date this review was finally actioned.
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "actionedDate", nullable = true)
    private Date actionedDate;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Vault getVault() { return vault; }

    public void setVault(Vault vault) {
        this.vault = vault;
    }


    public List<DepositReview> getDepositReviews() {
        return depositReviews;
    }

    public void setDepositReviews(List<DepositReview> depositReviews) {
        this.depositReviews = depositReviews;
    }

    public Date getNewReviewDate() {
        return newReviewDate;
    }

    public void setNewReviewDate(Date newReviewDate) {
        this.newReviewDate = newReviewDate;
    }

    public Date getActionedDate() {
        return actionedDate;
    }

    public void setActionedDate(Date actionedDate) {
        this.actionedDate = actionedDate;
    }
}
