package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.UuidGenerator;
import org.datavaultplatform.common.util.DateTimeUtils;
import org.jsondoc.core.annotation.ApiObject;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
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

    // handles VaultReviews with null creationTime
    public static final Comparator<VaultReview> BY_CREATION_TIME =
            Comparator.nullsFirst(Comparator.comparing(
                    VaultReview::getCreationTime,
                    Comparator.nullsFirst(Comparator.naturalOrder())));

    public static final String EG_VAULT_REVIEW = "eg.VaultReview.1";

    // VaultReview Identifier
    @Id
    @UuidGenerator
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // Serialise date in ISO 8601 format
    //@JsonFormat(shape=JsonFormat.Shape.STRING, pattern= DateTimeUtils.ISO_DATE_TIME_FORMAT)
    @Column(name = "creationTime", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creationTime;

    @JsonIgnore
    @ManyToOne
    private Vault vault;

    // A VaultReview can contain a number of DepositReviews
    @JsonIgnore
    @OneToMany(targetEntity=DepositReview.class, mappedBy="vaultReview", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("creationTime")
    private List<DepositReview> depositReviews;

    // Serialise date in ISO 8601 format
    //@JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DateTimeUtils.ISO_DATE_FORMAT)
    @Column(name = "newReviewDate", nullable = true, columnDefinition = "DATE")
    private LocalDate newReviewDate;
    
    // Serialise date in ISO 8601 format
    //@JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DateTimeUtils.ISO_DATE_FORMAT)
    @Column(name = "oldReviewDate", nullable = true, columnDefinition = "DATE")
    private LocalDate oldReviewDate;

    // The date this review was finally actioned.
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=DateTimeUtils.ISO_DATE_TIME_FORMAT)
    @Column(name = "actionedDate", nullable = true, columnDefinition = "TIMESTAMP")
    private LocalDateTime actionedDate;

    // A comment, what more can I say
    @Column(name = "comment", nullable = true, columnDefinition = "TEXT")
    private String comment;

    public VaultReview() {
        // Default Constructor
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public Vault getVault() { return vault; }

    public void setVault(Vault vault) {
        this.vault = vault;
    }


    public List<DepositReview> getDepositReviews() {
        if (depositReviews == null) {
            this.depositReviews = new java.util.ArrayList<>();
        }
        return depositReviews;
    }

    public void setDepositReviews(List<DepositReview> depositReviews) {
        this.depositReviews = depositReviews;
    }

    @Deprecated
    LocalDate getNewReviewDate() {
        return newReviewDate;
    }

    @Deprecated
    private void setNewReviewDate(LocalDate newReviewDate) {
        this.newReviewDate = newReviewDate;
    }

    public LocalDate getOldReviewDate() {
        return oldReviewDate;
    }

    /**
     * Just after this VaultReview is actioned - the Vault's reviewDate can be updated for the next review.
     * @param oldReviewDate - the Vault.reviewDate at the time the VaultReview is actioned.
     * @see org.datavaultplatform.broker.scheduled.CheckForDelete
     */
    public void setOldReviewDate(LocalDate oldReviewDate) {
        this.oldReviewDate = oldReviewDate;
    }

    public LocalDateTime getActionedDate() {
        return actionedDate;
    }

    public void setActionedDate(LocalDateTime actionedDate) {
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

    @JsonIgnore
    public boolean isReviewUnderway() {
        return actionedDate == null;
    }

    @JsonIgnore
    public boolean isReviewSubmitted() {
        return !isReviewUnderway();
    }

    public void addDepositReview(DepositReview deposit) {
        if (this.depositReviews == null) {
            this.depositReviews = new java.util.ArrayList<>();
        }
        depositReviews.add(deposit);
        deposit.setVaultReview(this);
    }
}
