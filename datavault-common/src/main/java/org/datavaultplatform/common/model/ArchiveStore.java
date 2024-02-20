package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Convert;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.custom.HashMapConverter;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="ArchiveStores")
@NamedEntityGraph(name=ArchiveStore.EG_ARCHIVE_STORE)
@Slf4j
public class ArchiveStore implements DataStore {
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
    @Convert(converter = HashMapConverter.class)
    @Column(name="properties", columnDefinition="LONGBLOB")
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        ArchiveStore that = (ArchiveStore) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /*
    This method takes an ArchiveStore and returns the specific instance of that Archive Store
     */
    @JsonIgnore
    public Device getDevice(StorageClassNameResolver resolver) {
        Device device = StorageClassUtils.createStorage(getStorageClass(), getProperties(), Device.class, resolver);
        return device;
    }

}
