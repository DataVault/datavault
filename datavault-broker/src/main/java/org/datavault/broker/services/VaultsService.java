package org.datavault.broker.services;

import org.datavault.common.model.Vault;
import java.util.ArrayList;
import org.datavault.common.model.Deposit;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:39
 */
public class VaultsService {

    public ArrayList<Vault> getVaults() {

        ArrayList<Vault> vaults = new ArrayList<>();
        
        // Some synthetic vault data
        Vault v1 = new Vault("1", "Demo vault");
        Deposit d1 = new Deposit("Initial deposit");
        v1.addDeposit(d1);
        vaults.add(v1);
        
        Vault v2 = new Vault("2", "Another demo vault");
        Deposit d2 = new Deposit("Historical deposit");
        d2.setStatus(Deposit.Status.CLOSED);
        v2.addDeposit(d2);
        Deposit d3 = new Deposit("A further deposit");
        v2.addDeposit(d3);
        vaults.add(v2);
        
        return vaults;
    }
    
    public void addVault(Vault vault) {
        // Persist the new vault metadata ...
    }
    
    public Vault getVault(int vaultID) {
        return getVaults().get(vaultID);
    }
}

