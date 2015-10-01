package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.event.Event;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * User: Robin Taylor
 * Date: 01/05/2015
 * Time: 14:04
 */
public class RestService {


    private String brokerURL;
    
    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }
    
    private HttpEntity<?> exchange(String url, Class clazz, HttpMethod method, Object payload) {
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-UserID", SecurityContextHolder.getContext().getAuthentication().getName());

        HttpEntity entity;
        if (method == HttpMethod.GET) {
            entity = new HttpEntity(headers);
        } else if (method == HttpMethod.POST) {
            entity = new HttpEntity(payload, headers);
        } else {
            throw new IllegalArgumentException("REST method not implemented!");
        }
        
        return restTemplate.exchange(url, method, entity, clazz);
        
    }
    
    public HttpEntity<?> get(String url, Class clazz) {
        return exchange(url, clazz, HttpMethod.GET, null);
    }
    
    public HttpEntity<?> post(String url, Class clazz, Object payload) {
        return exchange(url, clazz, HttpMethod.POST, payload);
    }
    
    /* GET requests */
    
    public FileStore[] getFileStoreListing() {        
        HttpEntity<?> response = get(brokerURL + "/filestores", FileStore[].class);
        return (FileStore[])response.getBody();
    }
    
    public FileInfo[] getFilesListing(String filePath) {
        
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        
        HttpEntity<?> response = get(brokerURL + "/files" + filePath, FileInfo[].class);
        return (FileInfo[])response.getBody();
    }

    public Vault[] getVaultsListing() {
        HttpEntity<?> response = get(brokerURL + "/vaults", Vault[].class);
        return (Vault[])response.getBody();
    }

    public Vault[] getVaultsListingAll() {
        HttpEntity<?> response = get(brokerURL + "/vaults/all", Vault[].class);
        return (Vault[])response.getBody();
    }

    public int getVaultsCount() {
        HttpEntity<?> response = get(brokerURL + "/vaults/count", Integer.class);
        return (Integer)response.getBody();
    }

    public Long getVaultsSize() {
        HttpEntity<?> response = get(brokerURL + "/vaults/size", Long.class);
        return (Long)response.getBody();
    }

    public int getDepositsCount() {
        HttpEntity<?> response = get(brokerURL + "/vaults/depositcount", Integer.class);
        return (Integer)response.getBody();
    }

    public Vault getVault(String id) {        
        HttpEntity<?> response = get(brokerURL + "/vaults/" + id, Vault.class);
        return (Vault)response.getBody();
    }

    public Deposit[] getDepositsListing(String vaultId) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/deposits", Deposit[].class);
        return (Deposit[])response.getBody();
    }

    public Deposit[] getDepositsListingAll() {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits", Deposit[].class);
        return (Deposit[])response.getBody();
    }


    public Deposit getDeposit(String vaultId, String depositID) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID, Deposit.class);
        return (Deposit)response.getBody();
    }
    
    public FileFixity[] getDepositManifest(String vaultId, String depositID) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID + "/manifest", FileFixity[].class);
        return (FileFixity[])response.getBody();
    }
    
    public Event[] getDepositEvents(String vaultId, String depositID) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID + "/events", Event[].class);
        return (Event[])response.getBody();
    }
    
    public Job[] getDepositJobs(String vaultId, String depositID) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID + "/jobs", Job[].class);
        return (Job[])response.getBody();
    }

    public Policy[] getPolicyListing() {
        HttpEntity<?> response = get(brokerURL + "/policies", Policy[].class);
        return (Policy[])response.getBody();
    }

    public Policy getPolicy(String policyId) {
        HttpEntity<?> response = get(brokerURL + "/policies/" + policyId, Policy.class);
        return (Policy)response.getBody();
    }

    public User getUser(String userId) {
        HttpEntity<?> response = get(brokerURL + "/users/" + userId, User.class);
        return (User)response.getBody();
    }

    public User[] getUsers() {
        HttpEntity<?> response = get(brokerURL + "/users", User[].class);
        return (User[])response.getBody();
    }

    public int getUsersCount() {
        HttpEntity<?> response = get(brokerURL + "/users/count", Integer.class);
        return (Integer)response.getBody();
    }

    /* POST requests */
    
    public Vault addVault(Vault vault) {
        HttpEntity<?> response = post(brokerURL + "/vaults/", Vault.class, vault);
        return (Vault)response.getBody();
    }

    public Deposit addDeposit(String vaultId, Deposit deposit) {
        HttpEntity<?> response = post(brokerURL + "/vaults/" + vaultId + "/deposits", Deposit.class, deposit);
        return (Deposit)response.getBody();
    }
    
    public Boolean restoreDeposit(String vaultId, String depositID, Restore restore) {        
        HttpEntity<?> response = post(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID + "/restore", Boolean.class, restore);
        return (Boolean)response.getBody();
    }
}
