package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.jsondoc.core.annotation.ApiObject;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "DepositReview")
@Entity
@Table(name="DepositReviews")
@NamedEntityGraph(
    name=DepositReview.EG_DEPOSIT_REVIEW,
    attributeNodes = {
        @NamedAttributeNode(value = DepositReview_.DEPOSIT, subgraph = "subDeposit"),
        @NamedAttributeNode(value = DepositReview_.VAULT_REVIEW, subgraph = "subVaultReview")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "subDeposit",
            attributeNodes = {
                @NamedAttributeNode(Deposit_.USER),
                @NamedAttributeNode(Deposit_.VAULT)
            }
        ),
        @NamedSubgraph(
            name = "subVaultReview",
            attributeNodes = {
                @NamedAttributeNode(VaultReview_.VAULT)
            }
        )
    }
)
public class DepositReview  {

    public static final String EG_DEPOSIT_REVIEW = "eg.DepositReview.1";
    // Deposit Identifier
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

    /*  DEPRECATED */
    @Deprecated
    // Has the user indicated the deposit can be deleted?
    @Column(name = "toBeDeleted")
    private boolean toBeDeleted;

    // When should the deposit be deleted?
    @Column(name="deleteStatus", nullable = false)
    private int deleteStatus;

    // A comment, what more can I say
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    // The date this review was finally actioned.
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "actionedDate", nullable = true)
    private Date actionedDate;


    @ManyToOne
    private VaultReview vaultReview;

    @ManyToOne
    private Deposit deposit;


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

    public boolean isToBeDeleted() {
        return toBeDeleted;
    }

    public void setToBeDeleted(boolean toBeDeleted) {
        this.toBeDeleted = toBeDeleted;
    }

    public int getDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(int deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getActionedDate() {
        return actionedDate;
    }

    public void setActionedDate(Date actionedDate) {
        this.actionedDate = actionedDate;
    }

    public VaultReview getVaultReview() {
        return vaultReview;
    }

    public void setVaultReview(VaultReview vaultReview) {
        this.vaultReview = vaultReview;
    }

    public Deposit getDeposit() {
        return deposit;
    }

    public void setDeposit(Deposit deposit) {
        this.deposit = deposit;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        DepositReview that = (DepositReview) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
