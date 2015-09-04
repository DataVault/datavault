package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.event.Event;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

/**
 * User: Robin Taylor
 * Date: 01/05/2015
 * Time: 14:04
 */
public class RestService {


    private String brokerURL;
    
    // TODO Placeholder
    private String userID = "USER1";

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }
    
    public FileInfo[] getFilesListing(String filePath) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(headers);
        
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        
        HttpEntity<FileInfo[]> response = restTemplate.exchange(brokerURL + "/files" + filePath, HttpMethod.GET, entity, FileInfo[].class);
        FileInfo[] files = response.getBody();
        
        return files;
    }

    public Vault[] getVaultsListing() {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(headers);
        
        HttpEntity<Vault[]> response = restTemplate.exchange(brokerURL + "/vaults", HttpMethod.GET, entity, Vault[].class);
        Vault[] vaults = response.getBody();
        
        return vaults;
    }

    public Vault getVault(String id) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(headers);
        
        HttpEntity<Vault> response = restTemplate.exchange(brokerURL + "/vaults/" + id, HttpMethod.GET, entity, Vault.class);
        Vault vault = response.getBody();
        
        return vault;
    }

    public Vault addVault(Vault vault) {
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(vault, headers);
        
        HttpEntity<Vault> response = restTemplate.exchange(brokerURL + "/vaults/", HttpMethod.POST, entity, Vault.class);
        Vault returnedVault = response.getBody();
        
        return returnedVault;
    }

    public Deposit addDeposit(String vaultId, Deposit deposit) {
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(deposit, headers);

        HttpEntity<Deposit> response = restTemplate.exchange(brokerURL + "/vaults/" + vaultId + "/deposits", HttpMethod.POST, entity, Deposit.class);
        Deposit returnedDeposit = response.getBody();
        
        return returnedDeposit;
    }
    
    public Deposit[] getDepositsListing(String vaultId) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(headers);
        
        HttpEntity<Deposit[]> response = restTemplate.exchange(brokerURL + "/vaults/" + vaultId + "/deposits", HttpMethod.GET, entity, Deposit[].class);
        Deposit[] deposits = response.getBody();
        
        return deposits;
    }
    
    public Deposit getDeposit(String vaultId, String depositID) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(headers);
        
        HttpEntity<Deposit> response = restTemplate.exchange(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID, HttpMethod.GET, entity, Deposit.class);
        Deposit deposit = response.getBody();
        
        return deposit;
    }
    
    public FileFixity[] getDepositManifest(String vaultId, String depositID) {
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(headers);
        
        HttpEntity<FileFixity[]> response = restTemplate.exchange(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID + "/manifest", HttpMethod.GET, entity, FileFixity[].class);
        FileFixity[] manifest = response.getBody();

        return manifest;
    }
    
    public Boolean restoreDeposit(String vaultId, String depositID, Restore restore) {        
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(restore, headers);

        HttpEntity<Boolean> response = restTemplate.exchange(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID + "/restore", HttpMethod.POST, entity, Boolean.class);
        Boolean result = response.getBody();
        
        return result;
    }
    
    public Event[] getDepositEvents(String vaultId, String depositID) {
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(headers);
        
        HttpEntity<Event[]> response = restTemplate.exchange(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID + "/events", HttpMethod.GET, entity, Event[].class);
        Event[] events = response.getBody();
        
        return events;
    }

    public Policy[] getPolicyListing() {
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(headers);
        
        HttpEntity<Policy[]> response = restTemplate.exchange(brokerURL + "/policies", HttpMethod.GET, entity, Policy[].class);
        Policy[] policies = response.getBody();

        return policies;
    }

    public Policy getPolicy(String policyId) {
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(headers);
        
        HttpEntity<Policy> response = restTemplate.exchange(brokerURL + "/policies/" + policyId, HttpMethod.GET, entity, Policy.class);
        Policy policy = response.getBody();

        return policy;
    }
    
    public User getUser(String userId) {
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", userID);
        HttpEntity entity = new HttpEntity(headers);
        
        HttpEntity<User> response = restTemplate.exchange(brokerURL + "/users/" + userId, HttpMethod.GET, entity, User.class);
        User user = response.getBody();

        return user;
    }
}
