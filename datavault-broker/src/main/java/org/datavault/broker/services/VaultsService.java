package org.datavault.broker.services;

import org.datavault.common.model.Vault;
import java.util.ArrayList;
import java.util.Hashtable;

import org.datavault.common.model.Deposit;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:39
 */
public class VaultsService {

    public Hashtable<String, Vault> getVaults() {

        Hashtable<String, Vault> vaults = new Hashtable<>();
        
        // Some synthetic vault data
        Vault v1 = new Vault("1", "Demo vault");
        v1.setDescription("A example first vault");
        v1.setSize(987654321);
        Deposit d1 = new Deposit("Initial deposit");
        v1.addDeposit(d1);
        vaults.put(v1.getID(), v1);
        
        Vault v2 = new Vault("2", "Another demo vault");
        v2.setDescription("A vault for testing purposes");
        v2.setSize(123456789);
        Deposit d2 = new Deposit("Historical deposit");
        d2.setStatus(Deposit.Status.CLOSED);
        v2.addDeposit(d2);
        Deposit d3 = new Deposit("A further deposit");
        v2.addDeposit(d3);
        vaults.put(v2.getID(), v2);
        
        return vaults;
    }
    
    public void addVault(Vault vault) {
        // Persist the new vault metadata ...
    }
    
    public Vault getVault(String vaultID) {
        return getVaults().get(vaultID);
    }
}

