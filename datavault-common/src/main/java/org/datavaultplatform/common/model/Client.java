package org.datavaultplatform.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;

/**
 * User: Robin Taylor
 * Date: 10/02/2016
 * Time: 10:53
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Clients")

public class Client {

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
}
