package org.datavault.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;

/**
 * User: Tom Higgins
 * Date: 12/05/2015
 * Time: 10:47
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Deposit {

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
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    
    public ArrayList<String> getBagIdentifiers() {
        return bagIdentifiers;
    }
}
