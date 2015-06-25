package org.datavault.broker.controllers;

import java.util.List;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Path;

import org.datavault.common.model.Vault;
import org.datavault.common.model.Deposit;
import org.datavault.common.model.Withdrawal;
import org.datavault.common.model.FileFixity;
import org.datavault.common.job.Job;
import org.datavault.broker.services.VaultsService;
import org.datavault.broker.services.DepositsService;
import org.datavault.broker.services.MetadataService;
import org.datavault.broker.services.MacFilesService;
import org.datavault.queue.Sender;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * User: Tom Higgins
 * Date: 11/05/2015
 * Time: 14:29
 */

@RestController
public class VaultsController {
    
    private VaultsService vaultsService;
    private DepositsService depositsService;
    private MetadataService metadataService;
    private MacFilesService macFilesService;
    private Sender sender;

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }
    
    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }
    
    public void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }
    
    public void setMacFilesService(MacFilesService macFilesService) {
        this.macFilesService = macFilesService;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    @RequestMapping(value = "/vaults", method = RequestMethod.GET)
    public List<Vault> getVaults() {
        return vaultsService.getVaults();
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
    public List<Deposit> getDeposits(@PathVariable("vaultid") String vaultID) {
        Vault vault = vaultsService.getVault(vaultID);
        return vault.getDeposits();
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits", method = RequestMethod.POST)
    public Deposit addDeposit(@PathVariable("vaultid") String vaultID,
                              @RequestBody Deposit deposit) throws Exception {

        Vault vault = vaultsService.getVault(vaultID);
        if (vault == null) {
            throw new Exception("Vault '" + vaultID + "' does not exist");
        }
        
        // Check the source file path is valid
        Path path = macFilesService.getAbsolutePath(deposit.getFilePath());
        if (path == null) {
            throw new IllegalArgumentException("Path '" + deposit.getFilePath() + "' is invalid");
        }
        
        // Add the deposit object
        depositsService.addDeposit(vault, deposit);
        
        // Ask the worker to process the deposit
        try {
            HashMap<String, String> depositProperties = new HashMap<>();
            depositProperties.put("depositId", deposit.getID());
            depositProperties.put("bagId", deposit.getBagId());
            depositProperties.put("filePath", path.toString()); // The absolute path
            
            Job depositJob = new Job("org.datavault.worker.jobs.Deposit", depositProperties);
            ObjectMapper mapper = new ObjectMapper();
            String jsonDeposit = mapper.writeValueAsString(depositJob);
            sender.send(jsonDeposit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return deposit;
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}", method = RequestMethod.GET)
    public Deposit getDeposit(@PathVariable("vaultid") String vaultID,
                              @PathVariable("depositid") String depositID) {
        Deposit deposit = depositsService.getDeposit(depositID);
        return deposit;
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/manifest", method = RequestMethod.GET)
    public List<FileFixity> getDepositManifest(@PathVariable("vaultid") String vaultID,
                                      @PathVariable("depositid") String depositID) throws IOException {
        Deposit deposit = depositsService.getDeposit(depositID);
        List<FileFixity> manifest = metadataService.getManifest(deposit.getBagId());
        return manifest;
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/status", method = RequestMethod.GET)
    public Deposit.Status getDepositState(@PathVariable("vaultid") String vaultID,
                                          @PathVariable("depositid") String depositID) {
        Deposit deposit = depositsService.getDeposit(depositID);
        return deposit.getStatus();
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/status", method = RequestMethod.POST)
    public Deposit setDepositState(@PathVariable("vaultid") String vaultID,
                              @PathVariable("depositid") String depositID,
                              @RequestBody Deposit.Status status) {
        Deposit deposit = depositsService.getDeposit(depositID);
        deposit.setStatus(status);
        depositsService.updateDeposit(deposit);
        return deposit;
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/withdraw", method = RequestMethod.POST)
    public Boolean withdrawDeposit(@PathVariable("vaultid") String vaultID,
                                   @PathVariable("depositid") String depositID,
                                   @RequestBody Withdrawal withdrawal) {
        
        Deposit deposit = depositsService.getDeposit(depositID);
        
        // Validate the path
        String withdrawalPath = withdrawal.getWithdrawalPath();
        if (withdrawalPath == null) {
            throw new IllegalArgumentException("Path was null");
        }
        
        Path path = macFilesService.getAbsolutePath(withdrawalPath);
        if (path == null) {
            throw new IllegalArgumentException("Path '" + withdrawalPath + "' is invalid");
        }
        
        // Ask the worker to process the withdrawal
        try {
            HashMap<String, String> withdrawProperties = new HashMap<>();
            withdrawProperties.put("bagId", deposit.getBagId());
            withdrawProperties.put("withdrawalPath", path.toString()); // The absolute path
            
            Job withdrawJob = new Job("org.datavault.worker.jobs.Withdraw", withdrawProperties);
            ObjectMapper mapper = new ObjectMapper();
            String jsonWithdraw = mapper.writeValueAsString(withdrawJob);
            sender.send(jsonWithdraw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return true;
    }
}
