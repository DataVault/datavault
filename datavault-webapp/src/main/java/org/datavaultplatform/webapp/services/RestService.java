package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.request.TransferVault;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.common.response.BillingInformation;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.DepositsData;
import org.datavaultplatform.common.response.EventInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    
    private <T> HttpEntity<T> exchange(String url, Class<T> clazz, HttpMethod method, Object payload) {

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setErrorHandler(new ApiErrorHandler());

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

        logger.debug("Calling Broker with url:" + url + " Method:" + method);
        System.out.println("Calling Broker with url:" + url + " Method:" + method);

        return restTemplate.exchange(url, method, entity, clazz);
        
    }
    
    public <T> HttpEntity<T> get(String url, Class<T> clazz) {
        return exchange(url, clazz, HttpMethod.GET, null);
    }

    public <T> HttpEntity<T> put(String url, Class<T> clazz, Object payload) {
        return exchange(url, clazz, HttpMethod.PUT, payload);
    }
    
    public <T> HttpEntity<T> post(String url, Class<T> clazz, Object payload) {
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

    public FileStore[] getFileStoresLocal() {
        HttpEntity<?> response = get(brokerURL + "/filestores/local", FileStore[].class);
        return (FileStore[])response.getBody();
    }

    public FileStore[] getFileStoresSFTP() {
        HttpEntity<?> response = get(brokerURL + "/filestores/sftp", FileStore[].class);
        return (FileStore[])response.getBody();
    }

    public ArchiveStore[] getArchiveStores() {
        HttpEntity<?> response = get(brokerURL + "/admin/archivestores", ArchiveStore[].class);
        return (ArchiveStore[])response.getBody();
    }

    public ArchiveStore getArchiveStore(String archiveStoreID) {
        HttpEntity<?> response = get(brokerURL + "/admin/archivestores/" + archiveStoreID, ArchiveStore.class);
        return (ArchiveStore)response.getBody();
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

    public Boolean checkDepositSize(String[] filePaths) {

        String parameters = "?";

        for (int i=0; i< filePaths.length; i++){
            String filePath = filePaths[i];
            if (!filePath.startsWith("/")) {
                filePath = "/" + filePath;
            }
            parameters += "filepath=" + filePath + "&";
        }

        System.out.println("parameters: " + parameters);

        HttpEntity<?> response = get(brokerURL + "/checkdepositsize" + parameters, Boolean.class);

        System.out.println("return: " + response.getBody());

        return (Boolean)response.getBody();
    }

    public VaultInfo[] getVaultsListing() {
        HttpEntity<?> response = get(brokerURL + "/vaults", VaultInfo[].class);
        return (VaultInfo[])response.getBody();
    }

    public VaultInfo[] getVaultsListingForGroup(String groupID) {
        HttpEntity<?> response = get(brokerURL + "/groups/" + groupID + "/vaults", VaultInfo[].class);
        return (VaultInfo[])response.getBody();
    }
    public VaultInfo[] getVaultsListingAll(String userID) {
        HttpEntity<?> response = get(brokerURL + "/vaults/user?userID=" + userID, VaultInfo[].class);
        return (VaultInfo[])response.getBody();
    }
    public VaultsData getBillingVaultsAll(String sort, String order, String offset, String maxResult) {
    	HttpEntity<?> response = get(brokerURL + "/admin/billing?sort=" + sort + "&order=" + order+ "&offset=" + offset+ "&maxResult=" + maxResult, VaultsData.class);
        return (VaultsData)response.getBody();
	}

    public BillingInformation getVaultBillingInfo(String vaultId) {
    	HttpEntity<?> response = get(brokerURL + "/admin/billing/" + vaultId , BillingInformation.class);
        return (BillingInformation)response.getBody();
	}

    public VaultsData getVaultsListingAll(String sort, String order, String offset, String maxResult) {
        HttpEntity<?> response = get(brokerURL + "/admin/vaults?sort=" + sort + "&order=" + order+ "&offset=" + offset+ "&maxResult=" + maxResult, VaultsData.class);
        return (VaultsData)response.getBody();
    }

    public VaultInfo[] searchVaults(String query) {
        HttpEntity<?> response = get(brokerURL + "/vaults/search?query=" + query, VaultInfo[].class);
        return (VaultInfo[])response.getBody();
    }

    public VaultsData searchVaultsForBilling(String query, String sort, String order, String offset, String maxResult) {
        HttpEntity<?> response = get(brokerURL + "/admin/billing/search?query=" + query + "&sort=" + sort + "&order=" + order+ "&offset=" + offset+ "&maxResult=" + maxResult, VaultsData.class);
        return (VaultsData)response.getBody();
    }

    public VaultsData searchVaults(String query, String sort, String order, String offset, String maxResult) {
        HttpEntity<?> response = get(brokerURL + "/vaults/search?query=" + query + "&sort=" + sort + "&order=" + order+ "&offset=" + offset+ "&maxResult=" + maxResult, VaultsData.class);
        return (VaultsData)response.getBody();
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
    public DepositInfo[] searchDepositsQuery(String query) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits/search/Query?query=" + query, DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }
    public DepositsData searchDepositsData(String query) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits/data/search?query=" + query, DepositsData.class);
        return (DepositsData)response.getBody();
    }

    public DepositInfo[] searchDeposits(String query, String sort) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits/search?query=" + query + "&sort=" + sort, DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }
    public DepositsData searchDepositsData(String query, String sort) {
        HttpEntity<?> response = get(brokerURL + "/vaults/deposits/data/search?query=" + query + "&sort=" + sort, DepositsData.class);
        return (DepositsData)response.getBody();
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

    public Vault getVaultRecord(String id) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + id + "/record", Vault.class);
        return (Vault)response.getBody();
    }

    public VaultInfo getVault(String id) {        
        HttpEntity<?> response = get(brokerURL + "/vaults/" + id, VaultInfo.class);
        return (VaultInfo)response.getBody();
    }

    public Vault checkVaultRetentionPolicy(String vaultId) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/checkretentionpolicy", Vault.class);
        return (Vault)response.getBody();
    }

    public int getRetentionPolicyStatusCount(int status) {
        HttpEntity<?> response = get(brokerURL + "/vaults/retentionpolicycount/" + status, Integer.class);
        return (Integer)response.getBody();
    }

    public DepositInfo[] getDepositsListing(String vaultId) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/deposits", DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }
    
    
    public DataManager[] getDataManagers(String vaultId) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/dataManagers", DataManager[].class);
        return (DataManager[])response.getBody();
    }

    public DataManager getDataManager(String vaultId, String uun) {
        HttpEntity<?> response = get(brokerURL + "/vaults/" + vaultId + "/dataManager/" + uun, DataManager.class);
        return (DataManager)response.getBody();
    }

    public DepositsData getDepositsListingAllData() {
        HttpEntity<?> response = get(brokerURL + "/admin/deposits/data", DepositsData.class);
        return (DepositsData)response.getBody();
    }
    
    public DepositInfo[] getDepositsListingAll() {
        HttpEntity<?> response = get(brokerURL + "/admin/deposits", DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }
    
    public VaultsData getDepositsListingAll(String sort, String order, String offset, String maxResult) {
        HttpEntity<?> response = get(brokerURL + "/admin/vaults?sort=" + sort + "&order=" + order+ "&offset=" + offset+ "&maxResult=" + maxResult, VaultsData.class);
        return (VaultsData)response.getBody();
    }


    public DepositInfo[] getDepositsListingAll(String sort) {
        HttpEntity<?> response = get(brokerURL + "/admin/deposits?sort=" + sort, DepositInfo[].class);
        return (DepositInfo[])response.getBody();
    }
    
    public DepositsData getDepositsListingAllData(String sort) {
        HttpEntity<?> response = get(brokerURL + "/admin/deposits/data?sort=" + sort, DepositsData.class);
        return (DepositsData)response.getBody();
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

    public RetentionPolicy updateRententionPolicy(RetentionPolicy policy) {
        HttpEntity<?> response = post(brokerURL + "/retentionpolicies/update", RetentionPolicy.class, policy);
        return (RetentionPolicy)response.getBody();
    }

    public User getUser(String userId) {
        HttpEntity<?> response = get(brokerURL + "/users/" + userId, User.class);
        return (User)response.getBody();
    }

    public User[] getUsers() {
        HttpEntity<?> response = get(brokerURL + "/users", User[].class);
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

    public Group[] getGroupsByScopedPermissions() {
        HttpEntity<?> response = get(brokerURL + "/groups/byScopedPermissions", Group[].class);
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

    public boolean deleteGroup(String groupID) {
        HttpEntity<?> response = delete(brokerURL + "/groups/" + groupID, Boolean.class);
        return (Boolean)response.getBody();
    }

    public Group updateGroup(Group group) {
        HttpEntity<?> response = post(brokerURL + "/groups/update", Group.class, group);
        return (Group)response.getBody();
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
    
    public String addFileChunk(String fileUploadHandle, String filename, String encodedRelativePath, String chunkNumber, String totalChunks, String chunkSize, String totalSize, byte[] content) {
        
        String fileChunkURL = brokerURL + "/upload/" + fileUploadHandle + "/" + filename + "?" +
                "relativePath=" + encodedRelativePath + "&" +
                "chunkNumber=" + chunkNumber + "&" +
                "totalChunks=" + totalChunks + "&" +
                "chunkSize=" + chunkSize + "&" +
                "totalSize=" + totalSize + "&";
        
        HttpEntity<?> response = post(fileChunkURL, Byte[].class, content);
        return (String)response.getBody();
    }

    public FileStore addFileStore(FileStore fileStore) {
        HttpEntity<?> response = post(brokerURL + "/filestores/", FileStore.class, fileStore);
        return (FileStore)response.getBody();
    }

    public FileStore addFileStoreSFTP(FileStore fileStore) {
        HttpEntity<?> response = post(brokerURL + "/filestores/sftp", FileStore.class, fileStore);
        return (FileStore)response.getBody();
    }

    public ArchiveStore addArchiveStore(ArchiveStore archiveStore) {
        System.out.println("Post request to broker");
        HttpEntity<?> response = post(brokerURL + "/admin/archivestores/", ArchiveStore.class, archiveStore);
        System.out.println("Done");
        return (ArchiveStore)response.getBody();
    }

    public ArchiveStore editArchiveStore(ArchiveStore archiveStore) {
        HttpEntity<?> response = put(brokerURL + "/admin/archivestores/", ArchiveStore.class, archiveStore);
        return (ArchiveStore)response.getBody();
    }

    public VaultInfo addVault(CreateVault createVault) {
        HttpEntity<?> response = post(brokerURL + "/vaults/", VaultInfo.class, createVault);
        return (VaultInfo)response.getBody();
    }

    public void transferVault(String vaultId, TransferVault transfer) {
        post(brokerURL + "/vaults/" + vaultId + "/transfer", VaultInfo.class, transfer);
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

    public Deposit restartDeposit(String depositID) {
        HttpEntity<?> response = post(brokerURL + "/deposits/" + depositID + "/restart", Deposit.class, null);
        return (Deposit)response.getBody();
    }

    public User addUser(User user) {
        HttpEntity<?> response = post(brokerURL + "/users/", User.class, user);
        return (User)response.getBody();
    }
    
    public VaultInfo addDataManager(String vaultId, String dataManagerUUN) {
        HttpEntity<?> response = post(brokerURL + "/vaults/" + vaultId + "/addDataManager", VaultInfo.class, dataManagerUUN);
        return (VaultInfo)response.getBody();
    }
    
    public VaultInfo deleteDataManager(String vaultId, String dataManagerID) {
        HttpEntity<?> response = delete(brokerURL + "/vaults/" + vaultId + "/deleteDataManager/" + dataManagerID, VaultInfo.class);
        return (VaultInfo)response.getBody();
    }

    public User editUser(User user) {
        HttpEntity<?> response = put(brokerURL + "/admin/users/", User.class, user);
        return (User)response.getBody();
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

    public void deleteFileStore(String fileStoreId) {
        delete(brokerURL + "/filestores/" + fileStoreId, String.class);
    }

    public void deleteArchiveStore(String archiveStoreId) {
        delete(brokerURL + "/admin/archivestores/" + archiveStoreId, String.class);
    }

    public VaultInfo updateVaultDescription(String vaultId, String vaultDescription) {
        HttpEntity<?> response = post(brokerURL + "/vaults/" + vaultId + "/updateVaultDescription", VaultInfo.class, vaultDescription);
        return (VaultInfo)response.getBody();
    }
    
    public void deleteDeposit(String depositId) {
        delete(brokerURL + "/admin/deposits/" + depositId, String.class);
    }

    public BillingInformation updateBillingInfo(String vaultId,BillingInformation billingInfo) {
        HttpEntity<?> response = post(brokerURL + "/admin/billing/" + vaultId+ "/updateBilling" , BillingInformation.class,billingInfo);
	    return (BillingInformation)response.getBody();
    }

    public RoleModel createRole(RoleModel role) {
        return post(brokerURL + "/permissions/role", RoleModel.class, role).getBody();
    }

    public RoleAssignment createRoleAssignment(RoleAssignment roleAssignment) {
        return post(brokerURL + "/permissions/roleAssignment", RoleAssignment.class, roleAssignment).getBody();
    }

    public List<PermissionModel> getSchoolPermissions() {
        return Arrays.asList(get(brokerURL + "/permissions/school", PermissionModel[].class).getBody());
    }

    public List<PermissionModel> getVaultPermissions() {
        return Arrays.asList(get(brokerURL + "/permissions/vault", PermissionModel[].class).getBody());
    }

    public Optional<RoleModel> getRole(long id) {
        return Optional.ofNullable(get(brokerURL + "/permissions/role/" + id, RoleModel.class).getBody());
    }

    public RoleModel getIsAdmin() {
        return get(brokerURL + "/permissions/role/isAdmin", RoleModel.class).getBody();
    }

    public List<RoleModel> getEditableRoles() {
        return Arrays.asList(get(brokerURL + "/permissions/roles", RoleModel[].class).getBody());
    }

    public List<RoleModel> getViewableRoles() {
        return Arrays.asList(get(brokerURL + "/permissions/roles/readOnly", RoleModel[].class).getBody());
    }

    public List<RoleModel> getSchoolRoles() {
        return Arrays.asList(get(brokerURL + "/permissions/roles/school", RoleModel[].class).getBody());
    }

    public List<RoleModel> getVaultRoles() {
        return Arrays.asList(get(brokerURL + "/permissions/roles/vault", RoleModel[].class).getBody());
    }

    public Optional<RoleAssignment> getRoleAssignment(Long id) {
        return Optional.ofNullable(get(brokerURL + "/permissions/roleAssignment/" + id, RoleAssignment.class).getBody());
    }

    public List<RoleAssignment> getRoleAssignmentsForSchool(String schoolId) {
        return Arrays.asList(get(brokerURL + "/permissions/roleAssignments/school/" + schoolId, RoleAssignment[].class).getBody());
    }

    public List<RoleAssignment> getRoleAssignmentsForVault(String vaultId) {
        return Arrays.asList(get(brokerURL + "/permissions/roleAssignments/vault/" + vaultId, RoleAssignment[].class).getBody());
    }

    public List<RoleAssignment> getRoleAssignmentsForUser(String userId) {
        return Arrays.asList(get(brokerURL + "/permissions/roleAssignments/user/" + userId, RoleAssignment[].class).getBody());
    }

    public List<RoleAssignment> getRoleAssignmentsForRole(long roleId) {
        return Arrays.asList(get(brokerURL + "/permissions/roleAssignments/role/" + roleId, RoleAssignment[].class).getBody());
    }

    public RoleModel updateRole(RoleModel role) {
        return put(brokerURL + "/permissions/role", RoleModel.class, role).getBody();
    }

    public RoleAssignment updateRoleAssignment(RoleAssignment roleAssignment) {
        return put(brokerURL + "/permissions/roleAssignment", RoleAssignment.class, roleAssignment).getBody();
    }

    public void deleteRole(Long roleId) {
        delete(brokerURL + "/permissions/role/" + roleId, Void.class);
    }

    public void deleteRoleAssignment(Long roleAssignmentId) {
        delete(brokerURL + "/permissions/roleAssignment/" + roleAssignmentId, Void.class);
    }

}
