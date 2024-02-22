package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "VaultReview")
@Entity
@Table(name="VaultReviews")
@NamedEntityGraph(
    name=VaultReview.EG_VAULT_REVIEW,
    attributeNodes = @NamedAttributeNode(value = VaultReview_.VAULT, subgraph = "subVault"),
    subgraphs = @NamedSubgraph(name="subVault", attributeNodes = {
        @NamedAttributeNode(Vault_.DATASET),
        @NamedAttributeNode(Vault_.GROUP),
        @NamedAttributeNode(Vault_.RETENTION_POLICY),
        @NamedAttributeNode(Vault_.USER)
    }))
public class VaultReview {

    public static final String EG_VAULT_REVIEW = "eg.VaultReview.1";

    // VaultReview Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // Serialise date in ISO 8601 format
    //@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creationTime", nullable = false)
    private Date creationTime;

    @ManyToOne
    private Vault vault;

    // A VaultReview can contain a number of DepositReviews
    @JsonIgnore
    @OneToMany(targetEntity=DepositReview.class, mappedBy="vaultReview", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("creationTime")
    private List<DepositReview> depositReviews;

    // Serialise date in ISO 8601 format
    //@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "newReviewDate", nullable = true)
    private Date newReviewDate;

    // Serialise date in ISO 8601 format
    //@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "oldReviewDate", nullable = true)
    private Date oldReviewDate;

    // The date this review was finally actioned.
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "actionedDate", nullable = true)
    private Date actionedDate;

    // A comment, what more can I say
    @Column(name = "comment", nullable = true, columnDefinition = "TEXT")
    private String comment;



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

    public Date getOldReviewDate() {
        return oldReviewDate;
    }

    public void setOldReviewDate(Date oldReviewDate) {
        this.oldReviewDate = oldReviewDate;
    }

    public Date getActionedDate() {
        return actionedDate;
    }

    public void setActionedDate(Date actionedDate) {
        this.actionedDate = actionedDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        VaultReview that = (VaultReview) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
