package org.datavault.broker.controllers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.datavault.common.model.Vault;
import org.datavault.common.model.Deposit;
import org.datavault.broker.services.VaultsService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:29
 */

@RestController
public class VaultsController {
    
    private VaultsService vaultsService;

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }
    
    @RequestMapping(value = "/vaults", method = RequestMethod.GET)
    public ArrayList<Vault> getVaults() {
        Hashtable<String, Vault> vaults = vaultsService.getVaults();
        Enumeration e = vaults.keys();
        ArrayList<Vault> theVaults = new ArrayList();
        while (e.hasMoreElements()) {
            theVaults.add(vaults.get(e.nextElement()));
        }
        return theVaults;
    }
    
    @RequestMapping(value = "/vaults", method = RequestMethod.POST)
    public Vault addVault(@RequestBody Vault vault) {
        vaultsService.addVault(vault);
        return vault;
    }

    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public Vault getVault(@PathVariable("vaultid") String vaultID) {
        return vaultsService.getVault(vaultID);
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits", method = RequestMethod.GET)
    public ArrayList<Deposit> getDeposits(@PathVariable("vaultid") String vaultID) {
        Vault vault = vaultsService.getVault(vaultID);
        return vault.getDeposits();
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}", method = RequestMethod.GET)
    public Deposit getDeposit(@PathVariable("vaultid") String vaultID,
                              @PathVariable("depositid") int depositID) {
        Deposit deposit = vaultsService.getVault(vaultID).getDeposit(depositID);
        return deposit;
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/status", method = RequestMethod.GET)
    public Deposit.Status getDepositState(@PathVariable("vaultid") String vaultID,
                                          @PathVariable("depositid") int depositID) {
        Deposit deposit = vaultsService.getVault(vaultID).getDeposit(depositID);
        return deposit.getStatus();
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/status", method = RequestMethod.POST)
    public Deposit setDepositState(@PathVariable("vaultid") String vaultID,
                              @PathVariable("depositid") int depositID,
                              @RequestBody Deposit.Status status) {
        Deposit deposit = vaultsService.getVault(vaultID).getDeposit(depositID);
        deposit.setStatus(status);
        return deposit;
    }
}
