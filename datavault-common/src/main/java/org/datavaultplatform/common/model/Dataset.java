package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Datasets")
public class Dataset {
    
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
}