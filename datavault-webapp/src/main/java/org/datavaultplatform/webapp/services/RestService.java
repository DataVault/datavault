package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.metadata.*;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.*;
import org.datavaultplatform.common.response.*;
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

        // If we have a logged on user then pass that information.
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            headers.set("X-UserID", SecurityContextHolder.getContext().getAuthentication().getName());
        }

        HttpEntity entity;
        if (method == HttpMethod.GET) {
            entity = new HttpEntity(headers);
        } else if (method == HttpMethod.PUT) {
            entity = new HttpEntity(payload, headers);
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

    public HttpEntity<?> put(String url, Class clazz, Object payload) {
        return exchange(url, clazz, HttpMethod.PUT, payload);
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

    public VaultInfo[] getVaultsListing() {
        HttpEntity<?> response = get(brokerURL + "/vaults", VaultInfo[].class);
        return (VaultInfo[])response.getBody();
    }

    public VaultInfo[] getVaultsListingForGroup(String groupID) {
        HttpEntity<?> response = get(brokerURL + "/groups/" + groupID + "/vaults", VaultInfo[].class);
        return (VaultInfo[])response.getBody();
    }

    public VaultInfo[] getVaultsListingAll() {
        HttpEntity<?> response = get(brokerURL + "/vaults/all", VaultInfo[].class);
        return (VaultInfo[])response.getBody();
    }

    public VaultInfo[] getVaultsListingAll(String sort, String order) {
        HttpEntity<?> response = get(brokerURL + "/vaults/all?sort=" + sort + "&order=" + order, VaultInfo[].class);
        return (VaultInfo[])response.getBody();
    }

    public VaultInfo[] searchVaults(String query) {
        HttpEntity<?> response = get(brokerURL + "/vaults/search?query=" + query, VaultInfo[].class);
        return (VaultInfo[])response.getBody();
    }

    public VaultInfo[] searchVaults(String query, String sort, String order) {
        HttpEntity<?> response = get(brokerURL + "/vaults/search?query=" + query + "&sort=" + sort + "&order=" + order, VaultInfo[].class);
        return (VaultInfo[])response.getBody();
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

    public DepositInfo[] searchDeposits(String query) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits/search?query=" + query, DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }

    public DepositInfo[] searchDeposits(String query, String sort) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits/search?query=" + query + "&sort=" + sort, DepositInfo[].class);
        return (DepositInfo[])response.getBody();
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

    public VaultInfo getVault(String id) {        
        HttpEntity<?> response = get(brokerURL + "/vaults/" + id, VaultInfo.class);
        return (VaultInfo)response.getBody();
    }

    public Vault checkVaultPolicy(String vaultId) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/checkpolicy", Vault.class);
        return (Vault)response.getBody();
    }

    public int checkAllVaultPolicies() {
        VaultInfo[] vaults = getVaultsListingAll();
        for (VaultInfo vault : vaults) {
            get(brokerURL + "/vaults/" + vault.getID() + "/checkpolicy", Vault.class);
        }
        return vaults.length;
    }

    public int getPolicyStatusCount(int status) {
        HttpEntity<?> response = get(brokerURL + "/vaults/policycount/" + status, Integer.class);
        return (Integer)response.getBody();
    }

    public DepositInfo[] getDepositsListing(String vaultId) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/deposits", DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }

    public DepositInfo[] getDepositsListingAll() {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits", DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }

    public DepositInfo[] getDepositsListingAll(String sort) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits?sort=" + sort, DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }

    public DepositInfo getDeposit(String vaultId, String depositID) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID, DepositInfo.class);
        return (DepositInfo)response.getBody();
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

    public Dataset[] getDatasets() {
        HttpEntity<?> response = get(brokerURL + "/metadata/datasets", Dataset[].class);
        return (Dataset[])response.getBody();
    }
    
    /* POST requests */
    
    public VaultInfo addVault(CreateVault createVault) {
        HttpEntity<?> response = post(brokerURL + "/vaults/", VaultInfo.class, createVault);
        return (VaultInfo)response.getBody();
    }

    public DepositInfo addDeposit(String vaultId, CreateDeposit createDeposit) {
        HttpEntity<?> response = post(brokerURL + "/vaults/" + vaultId + "/deposits", DepositInfo.class, createDeposit);
        return (DepositInfo)response.getBody();
    }
    
    public Boolean restoreDeposit(String vaultId, String depositID, Restore restore) {        
        HttpEntity<?> response = post(brokerURL + "/vaults/" + vaultId + "/deposits/" + depositID + "/restore", Boolean.class, restore);
        return (Boolean)response.getBody();
    }

    public User addUser(User user) {
        HttpEntity<?> response = post(brokerURL + "/users/", User.class, user);
        return (User)response.getBody();
    }

    public User editUser(User user) {
        HttpEntity<?> response = put(brokerURL + "/users/", User.class, user);
        return (User)response.getBody();
    }

    public Boolean keysExist(String userId) {
        HttpEntity<?> response = get(brokerURL + "/users/" + userId + "/keys", Boolean.class);
        return (Boolean)response.getBody();
    }

    public String addKeys(String userId) {
        // Bit odd to POST a null object, but a POST seems appropriate since it is a non-idempotent, create request
        HttpEntity<?> response = post(brokerURL + "/users/" + userId + "/keys", String.class, null);
        return (String)response.getBody();
    }
}
