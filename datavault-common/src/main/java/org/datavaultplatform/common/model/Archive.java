package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.datavaultplatform.common.util.DateTimeUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Archives")
@NamedEntityGraph(
    name=Archive.EG_ARCHIVE,
    attributeNodes = {
        @NamedAttributeNode(Archive_.ARCHIVE_STORE),
        @NamedAttributeNode(value = Archive_.DEPOSIT, subgraph = "subDeposit")
    },
    subgraphs = @NamedSubgraph(name="subDeposit", attributeNodes = {
        @NamedAttributeNode(Deposit_.USER),
        @NamedAttributeNode(Deposit_.VAULT)
    })
)
public class Archive {
    public static final String EG_ARCHIVE = "eg.Archive.1";

    // Archive Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= DateTimeUtils.ISO_DATE_TIME_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    // A Deposit can have many Archives
    @ManyToOne
    private Deposit deposit;

    // An ArchiveStore can have many Archives
    @ManyToOne
    private ArchiveStore archiveStore;

    @Column(columnDefinition = "TEXT")
    private String archiveId;


    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public void setDeposit(Deposit deposit) {
        this.deposit = deposit;
    }

    public void setArchiveStore(ArchiveStore archiveStore) {
        this.archiveStore = archiveStore;
    }

    public ArchiveStore getArchiveStore() {
        return archiveStore;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public Deposit getDeposit() {
        return this.deposit;
    }
    public String getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Archive archive = (Archive) o;
        return id != null && Objects.equals(id, archive.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
