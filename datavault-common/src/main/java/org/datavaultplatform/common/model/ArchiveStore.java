package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="ArchiveStores")
@NamedEntityGraph(name=ArchiveStore.EG_ARCHIVE_STORE)
public class ArchiveStore {
    public static final String EG_ARCHIVE_STORE = "eg.ArchiveStore.1";

    // Storage Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, length = 36)
    private String id;

    // An ArchiveStore can have many Archives
    @JsonIgnore
    @OneToMany(targetEntity = Archive.class, mappedBy = "archiveStore", fetch=FetchType.LAZY)
    @OrderBy("creationTime")
    private List<Archive> archives;

    // Class to use for access to storage
    @Column(columnDefinition = "TEXT")
    private String storageClass;
    
    // A textual description of the storage system
    @Column(columnDefinition = "TEXT")
    private String label;

    // One ArchiveStore should be flagged as the preferred option for retrieving archives.
    @Column(name = "retrieveEnabled", nullable = false)
    private boolean retrieveEnabled;
    
    // Properties to use for this storage system
    // NOTE: this is not a secure mechanism for storing credentials!
    @Lob
    private HashMap<String,String> properties = new HashMap<>();
    
    public ArchiveStore() {}
    public ArchiveStore(String storageClass, HashMap<String,String> properties, String label, boolean retrieveEnabled) {
        this.storageClass = storageClass;
        this.properties = properties;
        this.label = label;
        this.retrieveEnabled = retrieveEnabled;
    }

    public String getID() { return id; }

    public String getStorageClass() { return storageClass; }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    public String getLabel() { return label; }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRetrieveEnabled() {
        return retrieveEnabled;
    }

    public void setRetrieveEnabled(boolean retrieveEnabled) {
        this.retrieveEnabled = retrieveEnabled;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ArchiveStore rhs = (ArchiveStore) obj;
        return new EqualsBuilder()
            .append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
            append(id).toHashCode();
    }

}
