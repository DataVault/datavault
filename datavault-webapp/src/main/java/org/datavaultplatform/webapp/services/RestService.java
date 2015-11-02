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
    
    public String getFilesize(String filePath) {
        
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        
        HttpEntity<?> response = get(brokerURL + "/filesize" + filePath, String.class);
        return (String)response.getBody();
    }

    public Vault[] getVaultsListing() {
        HttpEntity<?> response = get(brokerURL + "/vaults", Vault[].class);
        return (Vault[])response.getBody();
    }

    public Vault[] getVaultsListingForGroup(String groupID) {
        HttpEntity<?> response = get(brokerURL + "/vaults/group/" + groupID, Vault[].class);
        return (Vault[])response.getBody();
    }

    public Vault[] getVaultsListingAll() {
        HttpEntity<?> response = get(brokerURL + "/vaults/all", Vault[].class);
        return (Vault[])response.getBody();
    }

    public Vault[] getVaultsListingAll(String sort, String order) {
        HttpEntity<?> response = get(brokerURL + "/vaults/all?sort=" + sort + "&order=" + order, Vault[].class);
        return (Vault[])response.getBody();
    }

    public Vault[] searchVaults(String query) {
        HttpEntity<?> response = get(brokerURL + "/vaults/search?query=" + query, Vault[].class);
        return (Vault[])response.getBody();
    }

    public Vault[] searchVaults(String query, String sort, String order) {
        HttpEntity<?> response = get(brokerURL + "/vaults/search?query=" + query + "&sort=" + sort + "&order=" + order, Vault[].class);
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

    public int getDepositsQueue() {
        HttpEntity<?> response = get(brokerURL + "/vaults/depositqueuecount", Integer.class);
        return (Integer)response.getBody();
    }

    public int getDepositsInProgress() {
        HttpEntity<?> response = get(brokerURL + "/vaults/depositinprogresscount", Integer.class);
        return (Integer)response.getBody();
    }

    public Deposit[] searchDeposits(String query) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits/search?query=" + query, Deposit[].class);
        return (Deposit[])response.getBody();
    }

    public Deposit[] searchDeposits(String query, String sort) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits/search?query=" + query + "&sort=" + sort, Deposit[].class);
        return (Deposit[])response.getBody();
    }

    public int getRestoresCount() {
        HttpEntity<?> response = get(brokerURL + "/vaults/restorecount", Integer.class);
        return (Integer)response.getBody();
    }

    public int getRestoresQueue() {
        HttpEntity<?> response = get(brokerURL + "/vaults/restorequeuecount", Integer.class);
        return (Integer)response.getBody();
    }

    public int getRestoresInProgress() {
        HttpEntity<?> response = get(brokerURL + "/vaults/restoreinprogresscount", Integer.class);
        return (Integer)response.getBody();
    }

    public Restore[] getRestoresListingAll() {
        HttpEntity<?> response = get(brokerURL + "/vaults/restores", Restore[].class);
        return (Restore[])response.getBody();
    }

    public Vault getVault(String id) {        
        HttpEntity<?> response = get(brokerURL + "/vaults/" + id, Vault.class);
        return (Vault)response.getBody();
    }

    public Vault checkVaultPolicy(String vaultId) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/checkpolicy", Vault.class);
        return (Vault)response.getBody();
    }

    public int checkAllVaultPolicies() {
        Vault[] vaults = getVaultsListingAll();
        for (Vault vault : vaults) {
            get(brokerURL + "/vaults/" + vault.getID() + "/checkpolicy", Vault.class);
        }
        return vaults.length;
    }

    public int getPolicyStatusCount(int status) {
        HttpEntity<?> response = get(brokerURL + "/vaults/policycount/" + status, Integer.class);
        return (Integer)response.getBody();
    }

    public Deposit[] getDepositsListing(String vaultId) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/deposits", Deposit[].class);
        return (Deposit[])response.getBody();
    }

    public Deposit[] getDepositsListingAll() {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits", Deposit[].class);
        return (Deposit[])response.getBody();
    }

    public Deposit[] getDepositsListingAll(String sort) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits?sort=" + sort, Deposit[].class);
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

    public Restore[] getDepositRestores(String vaultId, String depositID) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID + "/restores", Restore[].class);
        return (Restore[])response.getBody();
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

    public Group getGroup(String groupId) {
        HttpEntity<?> response = get(brokerURL + "/groups/" + groupId, Group.class);
        return (Group)response.getBody();
    }

    public Group[] getGroups() {
        HttpEntity<?> response = get(brokerURL + "/groups", Group[].class);
        return (Group[])response.getBody();
    }

    public int getGroupsCount() {
        HttpEntity<?> response = get(brokerURL + "/groups/count", Integer.class);
        return (Integer)response.getBody();
    }

    public int getGroupVaultCount(String vaultid) {
        HttpEntity<?> response = get(brokerURL + "/groups/" + vaultid + "/count", Integer.class);
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

    public User addUser(User user) {
        HttpEntity<?> response = post(brokerURL + "/users/", User.class, user);
        return (User)response.getBody();
    }
}
