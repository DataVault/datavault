package org.datavault.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Date;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:31
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vault {

    // Some demo metadata
    private String name;

    // Vault Identifier
    private String id;
    
    // Serialise date in ISO 8601 format
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date creationTime;
    
    // A vault can contain a number of deposits
    @JsonIgnore
    private ArrayList<Deposit> deposits;

    public Vault() {}
    public Vault(String ID, String name) {
        this.id = ID;
        this.name = name;
        this.creationTime = new Date();
        this.deposits = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Date getCreationTime() {
        return creationTime;
    }

    public String getID() { return id; }
    
    public ArrayList<Deposit> getDeposits() {
        return deposits;
    }
    
    public void addDeposit(Deposit deposit) {
        this.deposits.add(deposit);
    }
    
    public Deposit getDeposit(int depositID) {
        return getDeposits().get(depositID);
    }
}
