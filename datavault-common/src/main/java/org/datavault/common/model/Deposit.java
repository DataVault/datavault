package org.datavault.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.GenericGenerator;

/**
 * User: Tom Higgins
 * Date: 12/05/2015
 * Time: 10:47
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="Deposits")
public class Deposit {

    // Deposit Identifier
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;
    
    @JsonIgnore
    @ManyToOne
    private Vault vault;
    
    public enum Status {
        OPEN, CLOSED
    }
    
    private String note;
    private Status status;
    
    // A deposit can be related to multiple archive bags.
    private ArrayList<String> bagIdentifiers;
    
    public Deposit() {}
    public Deposit(String note) {
        this.note = note;
        this.status = Status.OPEN;
        this.bagIdentifiers = new ArrayList<>();
    }
    
    public String getID() { return id; }

    public Vault getVault() { return vault; }
    
    public void setVault(Vault vault) {
        this.vault = vault;
    }
    
    public String getNote() { return note; }
    
    public void setNote(String note) {
        this.note = note;
    }

    public Status getStatus() { return status; }

    public void setStatus(Status status) {
        this.status = status;
    }
    
    public ArrayList<String> getBagIdentifiers() { return bagIdentifiers; }
}
