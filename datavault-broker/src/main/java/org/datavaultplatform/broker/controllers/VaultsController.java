package org.datavaultplatform.broker.controllers;

import java.util.List;
import java.util.HashMap;
import java.io.IOException;

import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Restore;
import org.datavaultplatform.common.model.FileFixity;
import org.datavaultplatform.common.model.Policy;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.job.Job;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.MetadataService;
import org.datavaultplatform.broker.services.MacFilesService;
import org.datavaultplatform.broker.services.PoliciesService;
import org.datavaultplatform.queue.Sender;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.services.UsersService;

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
    private PoliciesService policiesService;
    private UsersService usersService;
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
    
    public void setPoliciesService(PoliciesService policiesService) {
        this.policiesService = policiesService;
    }
    
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    @RequestMapping(value = "/vaults", method = RequestMethod.GET)
    public List<Vault> getVaults(@RequestHeader(value = "X-UserID", required = true) String userID) {
        return vaultsService.getVaults();
    }
    
    @RequestMapping(value = "/vaults", method = RequestMethod.POST)
    public Vault addVault(@RequestHeader(value = "X-UserID", required = true) String userID,
                          @RequestBody Vault vault) throws Exception {
        String policyID = vault.getPolicyID();
        Policy policy = policiesService.getPolicy(policyID);
        if (policy == null) {
            throw new Exception("Policy '" + policyID + "' does not exist");
        }
        vault.setPolicy(policy);
        
        User user = usersService.getUser(userID);
        if (user == null) {
            throw new Exception("User '" + userID + "' does not exist");
        }
        vault.setUser(user);
        
        vaultsService.addVault(vault);
        return vault;
    }

    @RequestMapping(value = "/vaults/{vaultid}", method = RequestMethod.GET)
    public Vault getVault(@RequestHeader(value = "X-UserID", required = true) String userID,
                          @PathVariable("vaultid") String vaultID) {
        return vaultsService.getVault(vaultID);
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits", method = RequestMethod.GET)
    public List<Deposit> getDeposits(@RequestHeader(value = "X-UserID", required = true) String userID,
                                     @PathVariable("vaultid") String vaultID) {
        Vault vault = vaultsService.getVault(vaultID);
        return vault.getDeposits();
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits", method = RequestMethod.POST)
    public Deposit addDeposit(@RequestHeader(value = "X-UserID", required = true) String userID,
                              @PathVariable("vaultid") String vaultID,
                              @RequestBody Deposit deposit) throws Exception {

        Vault vault = vaultsService.getVault(vaultID);
        if (vault == null) {
            throw new Exception("Vault '" + vaultID + "' does not exist");
        }
        
        // Check the source file path is valid
        if (!macFilesService.validPath(deposit.getFilePath())) {
            throw new IllegalArgumentException("Path '" + deposit.getFilePath() + "' is invalid");
        }
        
        // Add the deposit object
        depositsService.addDeposit(vault, deposit);
        
        // Ask the worker to process the deposit
        try {
            ObjectMapper mapper = new ObjectMapper();

            HashMap<String, String> depositProperties = new HashMap<>();
            depositProperties.put("depositId", deposit.getID());
            depositProperties.put("bagId", deposit.getBagId());
            depositProperties.put("filePath", deposit.getFilePath()); // Note: no longer an absolute path
            
            // Deposit and Vault metadata
            // TODO: at the moment we're just serialising the objects to JSON.
            // In future we'll need a more formal schema/representation (e.g. RDF or JSON-LD).
            depositProperties.put("depositMetadata", mapper.writeValueAsString(deposit));
            depositProperties.put("vaultMetadata", mapper.writeValueAsString(vault));
            
            Job depositJob = new Job("org.datavaultplatform.worker.jobs.Deposit", depositProperties);
            String jsonDeposit = mapper.writeValueAsString(depositJob);
            sender.send(jsonDeposit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return deposit;
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}", method = RequestMethod.GET)
    public Deposit getDeposit(@RequestHeader(value = "X-UserID", required = true) String userID, 
                              @PathVariable("vaultid") String vaultID,
                              @PathVariable("depositid") String depositID) {
        Deposit deposit = depositsService.getDeposit(depositID);
        return deposit;
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/manifest", method = RequestMethod.GET)
    public List<FileFixity> getDepositManifest(@RequestHeader(value = "X-UserID", required = true) String userID, 
                                               @PathVariable("vaultid") String vaultID,
                                               @PathVariable("depositid") String depositID) throws IOException {
        Deposit deposit = depositsService.getDeposit(depositID);
        List<FileFixity> manifest = metadataService.getManifest(deposit.getBagId());
        return manifest;
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/events", method = RequestMethod.GET)
    public List<Event> getDepositEvents(@RequestHeader(value = "X-UserID", required = true) String userID, 
                                        @PathVariable("vaultid") String vaultID,
                                        @PathVariable("depositid") String depositID) {
        Deposit deposit = depositsService.getDeposit(depositID);
        List<Event> events = deposit.getEvents();
        return events;
    }

    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/status", method = RequestMethod.GET)
    public Deposit.Status getDepositState(@RequestHeader(value = "X-UserID", required = true) String userID, 
                                          @PathVariable("vaultid") String vaultID,
                                          @PathVariable("depositid") String depositID) {
        Deposit deposit = depositsService.getDeposit(depositID);
        return deposit.getStatus();
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/status", method = RequestMethod.POST)
    public Deposit setDepositState(@RequestHeader(value = "X-UserID", required = true) String userID, 
                                   @PathVariable("vaultid") String vaultID,
                                   @PathVariable("depositid") String depositID,
                                   @RequestBody Deposit.Status status) {
        Deposit deposit = depositsService.getDeposit(depositID);
        deposit.setStatus(status);
        depositsService.updateDeposit(deposit);
        return deposit;
    }
    
    @RequestMapping(value = "/vaults/{vaultid}/deposits/{depositid}/restore", method = RequestMethod.POST)
    public Boolean restoreDeposit(@RequestHeader(value = "X-UserID", required = true) String userID, 
                                  @PathVariable("vaultid") String vaultID,
                                  @PathVariable("depositid") String depositID,
                                  @RequestBody Restore restore) {
        
        Deposit deposit = depositsService.getDeposit(depositID);
        
        // Validate the path
        String restorePath = restore.getRestorePath();
        if (restorePath == null) {
            throw new IllegalArgumentException("Path was null");
        }

        // Check the source file path is valid
        if (!macFilesService.validPath(restorePath)) {
            throw new IllegalArgumentException("Path '" + restorePath + "' is invalid");
        }
        
        // Ask the worker to process the data restore
        try {
            HashMap<String, String> restoreProperties = new HashMap<>();
            restoreProperties.put("depositId", deposit.getID());
            restoreProperties.put("bagId", deposit.getBagId());
            restoreProperties.put("restorePath", restorePath); // No longer the absolute path
            
            Job restoreJob = new Job("org.datavaultplatform.worker.jobs.Restore", restoreProperties);
            ObjectMapper mapper = new ObjectMapper();
            String jsonRestore = mapper.writeValueAsString(restoreJob);
            sender.send(jsonRestore);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return true;
    }
}
