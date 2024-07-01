package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(
        name="Role_assignments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = { "role_id", "user_id", "school_id" }),
                @UniqueConstraint(columnNames = { "role_id", "user_id", "vault_id" }),
                @UniqueConstraint(columnNames = { "role_id", "user_id", "pending_vault_id" })
        }
)
@NamedEntityGraph(name=RoleAssignment.EG_ROLE_ASSIGNMENT,
    attributeNodes = @NamedAttributeNode(value = RoleAssignment_.ROLE, subgraph = "subRole"),
    subgraphs = @NamedSubgraph(name="subRole", attributeNodes = @NamedAttributeNode(RoleModel_.PERMISSIONS))
)
public class RoleAssignment {
    public static final String EG_ROLE_ASSIGNMENT = "eg.RoleAssignment.1";
    @Id
    @GenericGenerator(
            name = "hibSEQ",
            type = org.hibernate.id.enhanced.SequenceStyleGenerator.class,
            parameters = {
                    @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.FORCE_TBL_PARAM, value = "true")
            })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibSEQ")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleModel role;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "school_id")
    private String schoolId;

    @Column(name = "vault_id")
    private String vaultId;

    @Column(name = "pending_vault_id")
    private String pendingVaultId;

    public RoleAssignment() {}

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setRole(RoleModel role) {
        this.role = role;
    }

    public RoleModel getRole() {
        return role;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setVaultId(String vaultId) {
        this.vaultId = vaultId;
    }

    public String getVaultId() {
        return vaultId;
    }

    public void setPendingVaultId(String pendingVaultId) {
        this.pendingVaultId = pendingVaultId;
    }

    public String getPendingVaultId() {
        return this.pendingVaultId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleAssignment that = (RoleAssignment) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(role, that.role) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(schoolId, that.schoolId) &&
                Objects.equals(vaultId, that.vaultId) &&
                Objects.equals(pendingVaultId, that.pendingVaultId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, userId, schoolId, vaultId, pendingVaultId);
    }

    @JsonIgnore
    public String getTargetId() {
        if (schoolId != null) {
            return schoolId;
        } else if (vaultId != null) {
            return vaultId;
        } else if (pendingVaultId != null) {
            return pendingVaultId;
        } else {
            throw new IllegalStateException("Target ID requested global role assignment");
        }
    }
}
