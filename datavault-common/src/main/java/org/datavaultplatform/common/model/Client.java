package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * User: Robin Taylor
 * Date: 10/02/2016
 * Time: 10:53
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Clients")
@NamedEntityGraph(name=Client.EG_CLIENT)
public class Client {

    public static final String EG_CLIENT = "eg.Client.1";
    // User Identifier (not a UUID)
    @Id
    @Column(name = "id", unique = true, length = 180)
    private String id;

    // Name of the client application
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    // API key
    @Column(name = "apiKey", columnDefinition = "TEXT")
    private String apiKey;

    // ipAddress
    @Column(name = "ipAddress", columnDefinition = "TEXT")
    private String ipAddress;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    @Override
    public boolean equals(Object obj){
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Client rhs = (Client) obj;
        return new EqualsBuilder()
            .append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
            append(id).toHashCode();
    }
}
