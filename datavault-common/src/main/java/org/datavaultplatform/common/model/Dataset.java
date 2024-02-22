package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import jakarta.persistence.*;
import org.hibernate.Hibernate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Datasets")
@NamedEntityGraph(name=Dataset.EG_DATASET)
public class Dataset {

    public static final String EG_DATASET = "eg.Dataset.1";
    @Id
    @Column(name = "id", unique = true, length = 180)
    private String id;
    
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;
    
    // The raw content from the metadata provider e.g. XML
    @JsonIgnore
    @Transient

    //@Lob - COMMENTED OUT BY DAVIDHAY - no column mapping reqd if TRANSIENT
    private String content;
    
    @Transient
    private Boolean visible = true;

    @Column(name = "crisId", nullable = false, columnDefinition = "TEXT")
    private String crisId;
    
    public Dataset() {}

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

	public Boolean getVisible() {
		return this.visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

    public String getCrisId() {
        return this.crisId;
    }

    public void setCrisId(String crisId) {
        this.crisId = crisId;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Dataset dataset = (Dataset) o;
        return id != null && Objects.equals(id, dataset.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}