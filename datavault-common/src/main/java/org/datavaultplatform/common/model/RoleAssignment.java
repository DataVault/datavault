package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(
        name="Role_assignments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = { "role_id", "user_id", "school_id", "vault_id" })
        }
)
public class RoleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleModel role;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private Group school;

    @ManyToOne
    @JoinColumn(name = "vault_id")
    private Vault vault;

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

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setSchool(Group school) {
        this.school = school;
    }

    public Group getSchool() {
        return school;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
    }

    public Vault getVault() {
        return vault;
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
                Objects.equals(user, that.user) &&
                Objects.equals(school, that.school) &&
                Objects.equals(vault, that.vault);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, user, school, vault);
    }

    @JsonIgnore
    public String getTargetId() {
        if (school != null) {
            return school.getID();
        } else if (vault != null) {
            return vault.getID();
        } else {
            throw new IllegalStateException("Target ID requested global role assignment");
        }
    }
}
