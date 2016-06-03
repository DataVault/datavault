package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.model.Dataset;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.*;
import org.datavaultplatform.common.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(RestService.class);

    private String brokerURL;
    private String brokerApiKey;
    
    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public void setBrokerApiKey(String brokerApiKey) {
        this.brokerApiKey = brokerApiKey;
    }
    
    private HttpEntity<?> exchange(String url, Class clazz, HttpMethod method, Object payload) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        // If we have a logged on user then pass that information.
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            headers.set("X-UserID", SecurityContextHolder.getContext().getAuthentication().getName());
        }

        headers.set("X-Client-Key", brokerApiKey);

        HttpEntity entity;
        if (method == HttpMethod.GET) {
            entity = new HttpEntity(headers);
        } else if (method == HttpMethod.PUT) {
            entity = new HttpEntity(payload, headers);
        } else if (method == HttpMethod.POST) {
            entity = new HttpEntity(payload, headers);
        } else if (method == HttpMethod.DELETE) {
            entity = new HttpEntity(headers);
        } else {
            throw new IllegalArgumentException("REST method not implemented!");
        }

        logger.debug("Calling Broker with url:" + url);

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
    
    public HttpEntity<?> delete(String url, Class clazz) {
        return exchange(url, clazz, HttpMethod.DELETE, null);
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
        HttpEntity<?> response = get(brokerURL + "/admin/vaults", VaultInfo[].class);
        return (VaultInfo[])response.getBody();
    }

    public VaultInfo[] getVaultsListingAll(String sort, String order) {
        HttpEntity<?> response = get(brokerURL + "/admin/vaults?sort=" + sort + "&order=" + order, VaultInfo[].class);
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
        HttpEntity<?> response = get(brokerURL + "/statistics/count", Integer.class);
        return (Integer)response.getBody();
    }

    public Long getVaultsSize() {
        HttpEntity<?> response = get(brokerURL + "/statistics/size", Long.class);
        return (Long)response.getBody();
    }

    public int getDepositsCount() {
        HttpEntity<?> response = get(brokerURL + "/statistics/depositcount", Integer.class);
        return (Integer)response.getBody();
    }

    public int getDepositsQueue() {
        HttpEntity<?> response = get(brokerURL + "/vaults/depositqueuecount", Integer.class);
        return (Integer)response.getBody();
    }

    public int getDepositsInProgressCount() {
        HttpEntity<?> response = get(brokerURL + "/statistics/depositinprogresscount", Integer.class);
        return (Integer)response.getBody();
    }

    public Deposit[] getDepositsInProgress() {
        HttpEntity<?> response = get(brokerURL + "/statistics/depositinprogress", Deposit[].class);
        return (Deposit[])response.getBody();
    }

    public DepositInfo[] searchDeposits(String query) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits/search?query=" + query, DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }

    public DepositInfo[] searchDeposits(String query, String sort) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits/search?query=" + query + "&sort=" + sort, DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }

    public int getRetrievesCount() {
        HttpEntity<?> response = get(brokerURL + "/statistics/retrievecount", Integer.class);
        return (Integer)response.getBody();
    }

    public int getRetrievesQueue() {
        HttpEntity<?> response = get(brokerURL + "/vaults/retrievequeuecount", Integer.class);
        return (Integer)response.getBody();
    }

    public int getRetrievesInProgressCount() {
        HttpEntity<?> response = get(brokerURL + "/statistics/retrieveinprogresscount", Integer.class);
        return (Integer)response.getBody();
    }

    public Retrieve[] getRetrievesInProgress() {
        HttpEntity<?> response = get(brokerURL + "/vaults/retrieveinprogress", Retrieve[].class);
        return (Retrieve[])response.getBody();
    }

    public Retrieve[] getRetrievesListingAll() {
        HttpEntity<?> response = get(brokerURL + "/admin/retrieves", Retrieve[].class);
        return (Retrieve[])response.getBody();
    }

    public VaultInfo getVault(String id) {        
        HttpEntity<?> response = get(brokerURL + "/vaults/" + id, VaultInfo.class);
        return (VaultInfo)response.getBody();
    }

    public Vault checkVaultRetentionPolicy(String vaultId) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/checkretentionpolicy", Vault.class);
        return (Vault)response.getBody();
    }

    public int checkAllVaultRetentionPolicies() {
        VaultInfo[] vaults = getVaultsListingAll();
        for (VaultInfo vault : vaults) {
            get(brokerURL + "/vaults/" + vault.getID() + "/checkretentionpolicy", Vault.class);
        }
        return vaults.length;
    }

    public int getRetentionPolicyStatusCount(int status) {
        HttpEntity<?> response = get(brokerURL + "/vaults/retentionpolicycount/" + status, Integer.class);
        return (Integer)response.getBody();
    }

    public DepositInfo[] getDepositsListing(String vaultId) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/deposits", DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }

    public DepositInfo[] getDepositsListingAll() {
        HttpEntity<?> response = get(brokerURL + "/admin/deposits", DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }

    public DepositInfo[] getDepositsListingAll(String sort) {
        HttpEntity<?> response = get(brokerURL + "/admin/deposits?sort=" + sort, DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }

    public DepositInfo getDeposit(String depositID) {
        HttpEntity<?> response = get(brokerURL + "/deposits/" + depositID, DepositInfo.class);
        return (DepositInfo)response.getBody();
    }
    
    public FileFixity[] getDepositManifest(String depositID) {
        HttpEntity<?> response = get(brokerURL + "/deposits/" + depositID + "/manifest", FileFixity[].class);
        return (FileFixity[])response.getBody();
    }
    
    public EventInfo[] getDepositEvents(String depositID) {
        HttpEntity<?> response = get(brokerURL + "/deposits/" + depositID + "/events", EventInfo[].class);
        return (EventInfo[])response.getBody();
    }

    public Job[] getDepositJobs(String depositID) {
        HttpEntity<?> response = get(brokerURL + "/deposits/" + depositID + "/jobs", Job[].class);
        return (Job[])response.getBody();
    }

    public Retrieve[] getDepositRetrieves(String depositID) {
        HttpEntity<?> response = get(brokerURL + "/deposits/" + depositID + "/retrieves", Retrieve[].class);
        return (Retrieve[])response.getBody();
    }

    public RetentionPolicy[] getRetentionPolicyListing() {
        HttpEntity<?> response = get(brokerURL + "/retentionpolicies", RetentionPolicy[].class);
        return (RetentionPolicy[])response.getBody();
    }

    public RetentionPolicy getRetentionPolicy(String retentionPolicyId) {
        HttpEntity<?> response = get(brokerURL + "/retentionpolicies/" + retentionPolicyId, RetentionPolicy.class);
        return (RetentionPolicy)response.getBody();
    }

    public User getUser(String userId) {
        HttpEntity<?> response = get(brokerURL + "/users/" + userId, User.class);
        return (User)response.getBody();
    }

    public User[] getUsers() {
        HttpEntity<?> response = get(brokerURL + "/admin/users", User[].class);
        return (User[])response.getBody();
    }

    public User[] searchUsers(String query) {
        HttpEntity<?> response = get(brokerURL + "/admin/users/search?query=" + query, User[].class);
        return (User[])response.getBody();
    }

    public int getUsersCount() {
        HttpEntity<?> response = get(brokerURL + "/admin/users/count", Integer.class);
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
    
    public EventInfo[] getEvents() {
        HttpEntity<?> response = get(brokerURL + "/admin/events?sort=timestamp", EventInfo[].class);
        return (EventInfo[])response.getBody();
    }
    
    public int getEventCount() {
        HttpEntity<?> response = get(brokerURL + "/statistics/eventcount", Integer.class);
        return (Integer)response.getBody();
    }
    
    /* POST requests */


    public FileStore addFileStore(FileStore fileStore) {
        HttpEntity<?> response = post(brokerURL + "/filestores/", FileStore.class, fileStore);
        return (FileStore)response.getBody();
    }

    public VaultInfo addVault(CreateVault createVault) {
        HttpEntity<?> response = post(brokerURL + "/vaults/", VaultInfo.class, createVault);
        return (VaultInfo)response.getBody();
    }

    public DepositInfo addDeposit(CreateDeposit createDeposit) {
        HttpEntity<?> response = post(brokerURL + "/deposits", DepositInfo.class, createDeposit);
        return (DepositInfo)response.getBody();
    }
    
    public Group addGroup(Group group) {
        HttpEntity<?> response = post(brokerURL + "/groups/", Group.class, group);
        return (Group)response.getBody();
    }
    
    public Boolean retrieveDeposit(String depositID, Retrieve retrieve) {
        HttpEntity<?> response = post(brokerURL + "/deposits/" + depositID + "/retrieve", Boolean.class, retrieve);
        return (Boolean)response.getBody();
    }

    public User addUser(User user) {
        HttpEntity<?> response = post(brokerURL + "/users/", User.class, user);
        return (User)response.getBody();
    }

    public User editUser(User user) {
        HttpEntity<?> response = put(brokerURL + "/admin/users/", User.class, user);
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

    public String addKeys() {
        // Bit odd to POST a null object, but a POST seems appropriate since it is a non-idempotent, create request
        HttpEntity<?> response = post(brokerURL + "/filestores/keys", String.class, null);
        return (String)response.getBody();
    }

    public Boolean userExists(ValidateUser validateUser) {
        // Using a POST because I read somewhere that its somehow more secure to POST credentials, could be nonsense
        HttpEntity<?> response = post(brokerURL + "/auth/users/exists", Boolean.class, validateUser);
        return (Boolean)response.getBody();
    }

    public Boolean isValid(ValidateUser validateUser) {
        // Using a POST because I read somewhere that its somehow more secure to POST credentials, could be nonsense
        HttpEntity<?> response = post(brokerURL + "/auth/users/isvalid", Boolean.class, validateUser);
        return (Boolean)response.getBody();
    }

    public Boolean isAdmin(ValidateUser validateUser) {
        // Using a POST because I read somewhere that its somehow more secure to POST credentials, could be nonsense
        HttpEntity<?> response = post(brokerURL + "/auth/users/isadmin", Boolean.class, validateUser);
        return (Boolean)response.getBody();
    }
    
    public String notifyLogin(CreateClientEvent clientEvent) {
        HttpEntity<?> response = put(brokerURL + "/notify/login", String.class, clientEvent);
        return (String)response.getBody();
    }
    
    public String notifyLogout(CreateClientEvent clientEvent) {
        HttpEntity<?> response = put(brokerURL + "/notify/logout", String.class, clientEvent);
        return (String)response.getBody();
    }
    
    public String enableGroup(String groupId) {
        HttpEntity<?> response = put(brokerURL + "/groups/" + groupId + "/enable", String.class, null);
        return (String)response.getBody();
    }
    
    public String disableGroup(String groupId) {
        HttpEntity<?> response = put(brokerURL + "/groups/" + groupId + "/disable", String.class, null);
        return (String)response.getBody();
    }
    
    public String addGroupOwner(String groupId, String userId) {
        HttpEntity<?> response = put(brokerURL + "/groups/" + groupId + "/users/" + userId, String.class, null);
        return (String)response.getBody();
    }
    
    /* DELETE requests */
    public void removeGroupOwner(String groupId, String userId) {
        delete(brokerURL + "/groups/" + groupId + "/users/" + userId, String.class);
    }
}
