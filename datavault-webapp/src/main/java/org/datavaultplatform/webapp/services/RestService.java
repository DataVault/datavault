package org.datavaultplatform.webapp.services;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.*;
import org.datavaultplatform.common.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * User: Robin Taylor
 * Date: 01/05/2015
 * Time: 14:04
 */
@Service("restService")
@Profile("!standalone")
@Slf4j
public class RestService implements NotifyLogoutService, NotifyLoginService, EvaluatorService {

    private static final Logger logger = LoggerFactory.getLogger(RestService.class);

    private final String brokerURL;
    private final String brokerApiKey;

    private final RestTemplate restTemplate;

    @Autowired
    public RestService(
        @Value("${broker.url}")String brokerURL,
        @Value("${broker.api.key}") String brokerApiKey, RestTemplate restTemplate) {
        this.brokerURL = brokerURL;
        this.brokerApiKey = brokerApiKey;
        this.restTemplate = restTemplate;
    }

    private <T> ResponseEntity<T> exchange(String url, Class<T> clazz, HttpMethod method, Object payload) {

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

        // todo : check the http status code before returning?

        log.info("broker.url [{}]",url);
        return restTemplate.exchange(url, method, entity, clazz);

    }

    public <T> ResponseEntity<T> get(String url, Class<T> clazz) {
        return exchange(url, clazz, HttpMethod.GET, null);
    }

    public <T> ResponseEntity<T> put(String url, Class<T> clazz, Object payload) {
        return exchange(url, clazz, HttpMethod.PUT, payload);
    }

    public <T> ResponseEntity<T> post(String url, Class<T> clazz, Object payload) {
        return exchange(url, clazz, HttpMethod.POST, payload);
    }

    public <T> ResponseEntity<T> delete(String url, Class<T> clazz) {
        return exchange(url, clazz, HttpMethod.DELETE, null);
    }

    /* GET requests */

    public FileStore[] getFileStoreListing() {
        ResponseEntity<FileStore[]> response = get(brokerURL + "/filestores", FileStore[].class);
        return response.getBody();
    }

    public FileStore[] getFileStoresLocal() {
        ResponseEntity<FileStore[]> response = get(brokerURL + "/filestores/local", FileStore[].class);
        return response.getBody();
    }

    public FileStore[] getFileStoresSFTP() {
        ResponseEntity<FileStore[]> response = get(brokerURL + "/filestores/sftp", FileStore[].class);
        return response.getBody();
    }

    public ArchiveStore[] getArchiveStores() {
        ResponseEntity<ArchiveStore[]> response = get(brokerURL + "/admin/archivestores", ArchiveStore[].class);
        return response.getBody();
    }

    public ArchiveStore getArchiveStore(String archiveStoreID) {
        ResponseEntity<ArchiveStore> response = get(brokerURL + "/admin/archivestores/" + archiveStoreID, ArchiveStore.class);
        return response.getBody();
    }




    public FileInfo[] getFilesListing(String filePath) {

        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }

        ResponseEntity<FileInfo[]> response = get(brokerURL + "/files" + filePath, FileInfo[].class);
        return response.getBody();
    }

    public String getFilesize(String filePath) {

        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }

        ResponseEntity<String> response = get(brokerURL + "/filesize" + filePath, String.class);
        return response.getBody();
    }

    public DepositSize checkDepositSize(String[] filePaths) {

        String parameters = "?";

        for (int i=0; i< filePaths.length; i++){
            String filePath = filePaths[i];
            if (!filePath.startsWith("/")) {
                filePath = "/" + filePath;
            }
            parameters += "filepath=" + filePath + "&";
        }

        logger.debug("parameters: " + parameters);

        ResponseEntity<DepositSize> response = get(brokerURL + "/checkdepositsize" + parameters, DepositSize.class);

        logger.debug("return: " + response.getBody());

        return response.getBody();
    }

    public VaultInfo[] getVaultsListing() {
        ResponseEntity<VaultInfo[]> response = get(brokerURL + "/vaults", VaultInfo[].class);
        return response.getBody();
    }

    public VaultInfo[] getPendingVaultsListing() {
        ResponseEntity<VaultInfo[]> response = get(brokerURL + "/pendingVaults", VaultInfo[].class);
        return response.getBody();
    }

    public VaultInfo[] getVaultsListingForGroup(String groupID) {
        ResponseEntity<VaultInfo[]> response = get(brokerURL + "/groups/" + groupID + "/vaults", VaultInfo[].class);
        return response.getBody();
    }
    public VaultInfo[] getVaultsListingAll(String userID) {
        ResponseEntity<VaultInfo[]> response = get(brokerURL + "/vaults/user?userID=" + userID, VaultInfo[].class);
        return response.getBody();
    }

    public BillingInformation getVaultBillingInfo(String vaultId) {
        ResponseEntity<BillingInformation> response = get(brokerURL + "/admin/billing/" + vaultId , BillingInformation.class);
        return response.getBody();
    }

    public VaultsData getVaultsListingAll(String sort, String order, int offset, int maxResult) {
        ResponseEntity<VaultsData> response = get(brokerURL + "/admin/vaults?sort=" + sort + "&order=" + order+ "&offset=" + offset+ "&maxResult=" + maxResult, VaultsData.class);
        return response.getBody();
    }

    public VaultsData getVaultsForReview() {
        ResponseEntity<VaultsData> response = get(brokerURL + "/admin/vaultsForReview", VaultsData.class);
        return response.getBody();
    }

    public ReviewInfo[] getReviewsListing(String vaultId) {
        ResponseEntity<ReviewInfo[]> response = get(brokerURL +"/vaults/" + vaultId + "/vaultreviews", ReviewInfo[].class);
        return response.getBody();
    }

    public ReviewInfo getCurrentReview(String vaultId) {
        ResponseEntity<ReviewInfo> response = get(brokerURL +"/admin/vaults/" + vaultId + "/vaultreviews/current", ReviewInfo.class);
        return response.getBody();

    }

    public VaultReview getVaultReview(String vaultReviewId) {
        ResponseEntity<VaultReview> response = get(brokerURL + "/vaults/vaultreviews/" + vaultReviewId, VaultReview.class);
        return response.getBody();
    }

    public DepositReview getDepositReview(String depositReviewId) {
        ResponseEntity<DepositReview> response = get(brokerURL +"/vaultreviews/depositreviews/" +  depositReviewId, DepositReview.class);
        return response.getBody();
    }

    public VaultsData searchVaultsForBilling(String query, String sort, String order, int offset, int maxResult) {
        ResponseEntity<VaultsData> response = get(brokerURL + "/admin/billing/search?query=" + query + "&sort=" + sort + "&order=" + order+ "&offset=" + offset+ "&maxResult=" + maxResult, VaultsData.class);
        return response.getBody();
    }

    public VaultsData searchVaults(String query, String sort, String order, int offset, int maxResult) {
        ResponseEntity<VaultsData> response = get(brokerURL + "/vaults/search?query=" + query + "&sort=" + sort + "&order=" + order+ "&offset=" + offset+ "&maxResult=" + maxResult, VaultsData.class);
        return response.getBody();
    }
    
    public int getVaultsCount() {
        ResponseEntity<Integer> response = get(brokerURL + "/statistics/count", Integer.class);
        return response.getBody();
    }

    public Long getVaultsSize() {
        ResponseEntity<Long> response = get(brokerURL + "/statistics/size", Long.class);
        return response.getBody();
    }
    
    public VaultsData searchPendingVaults(String query, String sort, String order, int offset, int maxResult, Boolean confirmed) {
        HttpEntity<?> response = get(brokerURL + "/pendingVaults/search?query=" + query + "&sort=" + sort + "&order=" + order+ "&offset=" + offset+ "&maxResult=" + maxResult + "&confirmed=" + confirmed, VaultsData.class);
        return (VaultsData)response.getBody();
    }
    
    public int getTotalNumberOfPendingVaults() {
        HttpEntity<?> response = get(brokerURL + "/statistics/pendingVaultsTotal", Integer.class);
    	return (Integer)response.getBody();
    }
    
    public int getDepositsCount() {
        ResponseEntity<Integer> response = get(brokerURL + "/statistics/depositcount", Integer.class);
        return response.getBody();
    }

    public int getDepositsQueue() {
        ResponseEntity<Integer> response = get(brokerURL + "/vaults/depositqueuecount", Integer.class);
        return response.getBody();
    }

    public int getDepositsInProgressCount() {
        ResponseEntity<Integer> response = get(brokerURL + "/statistics/depositinprogresscount", Integer.class);
        return response.getBody();
    }

    public Deposit[] getDepositsInProgress() {
        ResponseEntity<Deposit[]> response = get(brokerURL + "/statistics/depositinprogress", Deposit[].class);
        return response.getBody();
    }
    public DepositInfo[] searchDepositsQuery(String query) {
        ResponseEntity<DepositInfo[]> response = get(brokerURL + "/vaults/deposits/search/Query?query=" + query, DepositInfo[].class);
        return response.getBody();
    }
    public DepositsData searchDepositsData(String query) {
        ResponseEntity<DepositsData> response = get(brokerURL + "/vaults/deposits/data/search?query=" + query, DepositsData.class);
        return response.getBody();
    }

    public DepositInfo[] searchDeposits(String query, String sort) {
        ResponseEntity<DepositInfo[]> response = get(brokerURL + "/vaults/deposits/search?query=" + query + "&sort=" + sort, DepositInfo[].class);
        return response.getBody();
    }
    public DepositsData searchDepositsData(String query, String sort, String order) {
        ResponseEntity<DepositsData> response = get(brokerURL + "/vaults/deposits/data/search?query=" + query + "&sort=" + sort + "&order=" + order, DepositsData.class);
        return response.getBody();
    }

    public int getRetrievesCount() {
        ResponseEntity<Integer> response = get(brokerURL + "/statistics/retrievecount", Integer.class);
        return response.getBody();
    }

    public int getRetrievesQueue() {
        ResponseEntity<Integer> response = get(brokerURL + "/vaults/retrievequeuecount", Integer.class);
        return response.getBody();
    }

    public int getRetrievesInProgressCount() {
        ResponseEntity<Integer> response = get(brokerURL + "/statistics/retrieveinprogresscount", Integer.class);
        return response.getBody();
    }

    public Retrieve[] getRetrievesInProgress() {
        ResponseEntity<Retrieve[]> response = get(brokerURL + "/vaults/retrieveinprogress", Retrieve[].class);
        return response.getBody();
    }

    public Retrieve[] getRetrievesListingAll() {
        ResponseEntity<Retrieve[]> response = get(brokerURL + "/admin/retrieves", Retrieve[].class);
        return response.getBody();
    }

    public Vault getVaultRecord(String id) {
        ResponseEntity<Vault> response = get(brokerURL + "/vaults/" + id + "/record", Vault.class);
        return response.getBody();
    }
    
    public PendingVault getPendingVaultRecord(String id) {
        ResponseEntity<PendingVault> response = get(brokerURL + "/pendingVaults/" + id + "/record", PendingVault.class);
        return response.getBody();
    }

    public VaultInfo getVault(String id) {
        ResponseEntity<VaultInfo> response = get(brokerURL + "/vaults/" + id, VaultInfo.class);
        return response.getBody();
    }

    public VaultInfo getPendingVault(String id) {
        HttpEntity<VaultInfo> response = get(brokerURL + "/pendingVaults/" + id, VaultInfo.class);
        return response.getBody();
    }

    public Vault checkVaultRetentionPolicy(String vaultId) {
        ResponseEntity<Vault> response = get(brokerURL + "/vaults/" + vaultId + "/checkretentionpolicy", Vault.class);
        return response.getBody();
    }

    public int getRetentionPolicyStatusCount(int status) {
        ResponseEntity<Integer> response = get(brokerURL + "/vaults/retentionpolicycount/" + status, Integer.class);
        return response.getBody();
    }

    public DepositInfo[] getDepositsListing(String vaultId) {
        ResponseEntity<DepositInfo[]> response = get(brokerURL + "/vaults/" + vaultId + "/deposits", DepositInfo[].class);
        return response.getBody();
    }


    public DataManager[] getDataManagers(String vaultId) {
        ResponseEntity<DataManager[]> response = get(brokerURL + "/vaults/" + vaultId + "/dataManagers", DataManager[].class);
        return response.getBody();
    }

    public DataManager getDataManager(String vaultId, String uun) {
        ResponseEntity<DataManager> response = get(brokerURL + "/vaults/" + vaultId + "/dataManager/" + uun, DataManager.class);
        return response.getBody();
    }

    public DepositsData getDepositsListingAllData() {
        ResponseEntity<DepositsData> response = get(brokerURL + "/admin/deposits/data", DepositsData.class);
        return response.getBody();
    }

    public DepositInfo[] getDepositsListingAll(String query, String sort, String order, int offset, int maxResult) {
        ResponseEntity<DepositInfo[]> response = get(brokerURL + "/admin/deposits?query=" + query + "&sort=" + sort + "&order=" + order + "&offset=" + offset + "&maxResult=" + maxResult, DepositInfo[].class);
        return response.getBody();
    }

    public Integer getTotalDepositsCount(String query) {
        ResponseEntity<Integer> response = get(brokerURL + "/admin/deposits/count?query=" + query, Integer.class);
        return response.getBody();
    }

    public DepositsData getDepositsListingAllData(String sort) {
        ResponseEntity<DepositsData> response = get(brokerURL + "/admin/deposits/data?sort=" + sort, DepositsData.class);
        return response.getBody();
    }

    public DepositInfo getDeposit(String depositID) {
        ResponseEntity<DepositInfo> response = get(brokerURL + "/deposits/" + depositID, DepositInfo.class);
        return response.getBody();
    }

    public FileFixity[] getDepositManifest(String depositID) {
        ResponseEntity<FileFixity[]> response = get(brokerURL + "/deposits/" + depositID + "/manifest", FileFixity[].class);
        return response.getBody();
    }

    public EventInfo[] getDepositEvents(String depositID) {
        ResponseEntity<EventInfo[]> response = get(brokerURL + "/deposits/" + depositID + "/events", EventInfo[].class);
        return response.getBody();
    }

    public Job[] getDepositJobs(String depositID) {
        ResponseEntity<Job[]> response = get(brokerURL + "/deposits/" + depositID + "/jobs", Job[].class);
        return response.getBody();
    }

    public Retrieve[] getDepositRetrieves(String depositID) {
        ResponseEntity<Retrieve[]> response = get(brokerURL + "/deposits/" + depositID + "/retrieves", Retrieve[].class);
        return response.getBody();
    }

    public RetentionPolicy[] getRetentionPolicyListing() {
        ResponseEntity<RetentionPolicy[]> response = get(brokerURL + "/retentionpolicies", RetentionPolicy[].class);
        return response.getBody();
    }

    public CreateRetentionPolicy getRetentionPolicy(String retentionPolicyId) {
        ResponseEntity<CreateRetentionPolicy> response = get(brokerURL + "/admin/retentionpolicies/" + retentionPolicyId, CreateRetentionPolicy.class);
        return response.getBody();
    }

    public User getUser(String userId) {
        ResponseEntity<User> response = get(brokerURL + "/users/" + userId, User.class);
        return response.getBody();
    }

    public User[] getUsers() {
        ResponseEntity<User[]> response = get(brokerURL + "/users", User[].class);
        return response.getBody();
    }

    public User[] searchUsers(String query) {
        ResponseEntity<User[]> response = get(brokerURL + "/admin/users/search?query=" + query, User[].class);
        return response.getBody();
    }

    public int getUsersCount() {
        ResponseEntity<Integer> response = get(brokerURL + "/admin/users/count", Integer.class);
        return response.getBody();
    }

    public Group getGroup(String groupId) {
        ResponseEntity<Group> response = get(brokerURL + "/groups/" + groupId, Group.class);
        return response.getBody();
    }

    public Group[] getGroups() {
        ResponseEntity<Group[]> response = get(brokerURL + "/groups", Group[].class);
        return response.getBody();
    }

    public Group[] getGroupsByScopedPermissions() {
        ResponseEntity<Group[]> response = get(brokerURL + "/groups/byScopedPermissions", Group[].class);
        return response.getBody();
    }

    public int getGroupsCount() {
        ResponseEntity<Integer> response = get(brokerURL + "/groups/count", Integer.class);
        return response.getBody();
    }

    public int getGroupVaultCount(String vaultid) {
        ResponseEntity<Integer> response = get(brokerURL + "/groups/" + vaultid + "/count", Integer.class);
        return response.getBody();
    }

    public boolean deleteGroup(String groupID) {
        ResponseEntity<Boolean> response = delete(brokerURL + "/groups/" + groupID, Boolean.class);
        return response.getBody();
    }

    public Group updateGroup(Group group) {
        ResponseEntity<Group> response = post(brokerURL + "/groups/update", Group.class, group);
        return response.getBody();
    }

    public Dataset[] getDatasets() {
        ResponseEntity<Dataset[]> response = get(brokerURL + "/metadata/datasets", Dataset[].class);
        return response.getBody();
    }

    public EventInfo[] getEvents() {
        ResponseEntity<EventInfo[]> response = get(brokerURL + "/admin/events?sort=timestamp", EventInfo[].class);
        return response.getBody();
    }

    public int getEventCount() {
        ResponseEntity<Integer> response = get(brokerURL + "/statistics/eventcount", Integer.class);
        return response.getBody();
    }

    /* POST requests */

    public String addFileChunk(String fileUploadHandle, String filename, String encodedRelativePath, String chunkNumber, String totalChunks, String chunkSize, String totalSize, byte[] content) {

        String fileChunkURL = brokerURL + "/upload/" + fileUploadHandle + "/" + filename + "?" +
                "relativePath=" + encodedRelativePath + "&" +
                "chunkNumber=" + chunkNumber + "&" +
                "totalChunks=" + totalChunks + "&" +
                "chunkSize=" + chunkSize + "&" +
                "totalSize=" + totalSize + "&";

        /*
          TODO - this looks like a bug
          The broker's '/upload' handler
          org/datavaultplatform/broker/controllers/FilesController#postFileChunk/
          returns a String - not Byte[]
        */
        /*
        ResponseEntity<?> response = post(fileChunkURL, Byte[].class, content);
        return (String)response.getBody();
         */
        ResponseEntity<String> response = post(fileChunkURL, String.class, content);
        return response.getBody();
    }

    public FileStore addFileStore(FileStore fileStore) {
        ResponseEntity<FileStore> response = post(brokerURL + "/filestores/", FileStore.class, fileStore);
        return response.getBody();
    }

    public FileStore addFileStoreSFTP(FileStore fileStore) {
        ResponseEntity<FileStore> response = post(brokerURL + "/filestores/sftp", FileStore.class, fileStore);
        return response.getBody();
    }

    public ArchiveStore addArchiveStore(ArchiveStore archiveStore) {
        logger.debug("Post request to broker");
        ResponseEntity<ArchiveStore> response = post(brokerURL + "/admin/archivestores/", ArchiveStore.class, archiveStore);
        logger.debug("Done");
        return response.getBody();
    }

    public ArchiveStore editArchiveStore(ArchiveStore archiveStore) {
        ResponseEntity<ArchiveStore> response = put(brokerURL + "/admin/archivestores/", ArchiveStore.class, archiveStore);
        return response.getBody();
    }

    public VaultInfo addVault(CreateVault createVault) {
        ResponseEntity<VaultInfo> response = post(brokerURL + "/vaults/", VaultInfo.class, createVault);
        return response.getBody();
    }

    public VaultInfo addPendingVault(CreateVault createVault) {
        ResponseEntity<VaultInfo> response = post(brokerURL + "/pendingVaults/", VaultInfo.class, createVault);
        return response.getBody();
    }

    public VaultInfo updatePendingVault(CreateVault createVault) {
        ResponseEntity<VaultInfo> response = post(brokerURL + "/pendingVaults/update", VaultInfo.class, createVault);
        return response.getBody();
    }

    public  Boolean addVaultForPendingVault(String pendingVaultId, Date reviewDate) {
        ResponseEntity<Boolean> response = post(brokerURL + "/admin/pendingVaults/addVault/" + pendingVaultId, Boolean.class, reviewDate);
        return response.getBody();
    }

    public void transferVault(String vaultId, TransferVault transfer) {
        post(brokerURL + "/vaults/" + vaultId + "/transfer", VaultInfo.class, transfer);
    }

    public ReviewInfo createCurrentReview(String vaultId) {
        ResponseEntity<ReviewInfo> response = post(brokerURL + "/admin/vaults/vaultreviews/current", ReviewInfo.class, vaultId);
        return response.getBody();
    }

    public VaultReview editVaultReview(VaultReview vaultReview) {
        ResponseEntity<VaultReview> response = put(brokerURL + "/admin/vaults/vaultreviews", VaultReview.class, vaultReview);
        return response.getBody();
    }

    public DepositInfo addDeposit(CreateDeposit createDeposit) {
        ResponseEntity<DepositInfo> response = post(brokerURL + "/deposits", DepositInfo.class, createDeposit);
        return response.getBody();
    }

    public DepositReview addDepositReview(String depositId, String vaultReviewId) {
        ResponseEntity<DepositReview> response = post(brokerURL + "/admin/vaultreviews/" + vaultReviewId + "/depositreviews", DepositReview.class, depositId);
        return response.getBody();
    }

    public DepositReview editDepositReview(DepositReview depositReview) {
        ResponseEntity<DepositReview> response = put(brokerURL + "/admin/vaultreviews/depositreviews", DepositReview.class, depositReview);
        return response.getBody();
    }


    public Group addGroup(Group group) {
        ResponseEntity<Group> response = post(brokerURL + "/groups/", Group.class, group);
        return response.getBody();
    }

    public Boolean retrieveDeposit(String depositID, Retrieve retrieve) {
        ResponseEntity<Boolean> response = post(brokerURL + "/deposits/" + depositID + "/retrieve", Boolean.class, retrieve);
        return response.getBody();
    }

    public Deposit restartDeposit(String depositID) {
        ResponseEntity<Deposit> response = post(brokerURL + "/deposits/" + depositID + "/restart", Deposit.class, null);
        return response.getBody();
    }

    public User addUser(User user) {
        ResponseEntity<User> response = post(brokerURL + "/users/", User.class, user);
        return response.getBody();
    }

    public VaultInfo addDataManager(String vaultId, String dataManagerUUN) {
        ResponseEntity<VaultInfo> response = post(brokerURL + "/vaults/" + vaultId + "/addDataManager", VaultInfo.class, dataManagerUUN);
        return response.getBody();
    }

    public VaultInfo deleteDataManager(String vaultId, String dataManagerID) {
        ResponseEntity<VaultInfo> response = delete(brokerURL + "/vaults/" + vaultId + "/deleteDataManager/" + dataManagerID, VaultInfo.class);
        return response.getBody();
    }

    public User editUser(User user) {
        ResponseEntity<User> response = put(brokerURL + "/admin/users/", User.class, user);
        return response.getBody();
    }

    public Boolean userExists(ValidateUser validateUser) {
        // Using a POST because I read somewhere that its somehow more secure to POST credentials, could be nonsense
        ResponseEntity<Boolean> response = post(brokerURL + "/auth/users/exists", Boolean.class, validateUser);
        return response.getBody();
    }

    public Boolean isValid(ValidateUser validateUser) {
        // Using a POST because I read somewhere that its somehow more secure to POST credentials, could be nonsense
        ResponseEntity<Boolean> response = post(brokerURL + "/auth/users/isvalid", Boolean.class, validateUser);
        return response.getBody();
    }

    public Boolean isAdmin(ValidateUser validateUser) {
        // Using a POST because I read somewhere that its somehow more secure to POST credentials, could be nonsense
        ResponseEntity<Boolean> response = post(brokerURL + "/auth/users/isadmin", Boolean.class, validateUser);
        return response.getBody();
    }

    public String notifyLogin(CreateClientEvent clientEvent) {
        ResponseEntity<String> response = put(brokerURL + "/notify/login", String.class, clientEvent);
        return response.getBody();
    }

    public String notifyLogout(CreateClientEvent clientEvent) {
        ResponseEntity<String> response = put(brokerURL + "/notify/logout", String.class, clientEvent);
        return response.getBody();
    }

    public String enableGroup(String groupId) {
        ResponseEntity<String> response = put(brokerURL + "/groups/" + groupId + "/enable", String.class, null);
        return response.getBody();
    }

    public String disableGroup(String groupId) {
        ResponseEntity<String> response = put(brokerURL + "/groups/" + groupId + "/disable", String.class, null);
        return response.getBody();
    }

    public String addGroupOwner(String groupId, String userId) {
        ResponseEntity<String> response = put(brokerURL + "/groups/" + groupId + "/users/" + userId, String.class, null);
        return response.getBody();
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
        ResponseEntity<VaultInfo> response = post(brokerURL + "/vaults/" + vaultId + "/updateVaultDescription", VaultInfo.class, vaultDescription);
        return response.getBody();
    }

    public VaultInfo updateVaultName(String vaultId, String vaultName) {
        ResponseEntity<VaultInfo> response = post(brokerURL + "/vaults/" + vaultId + "/updateVaultName", VaultInfo.class, vaultName);
        return response.getBody();
    }

    public VaultInfo updateVaultReviewDate(String vaultId, String reviewDate) {
        ResponseEntity<VaultInfo> response = post(brokerURL + "/vaults/" + vaultId + "/updatereviewdate", VaultInfo.class, reviewDate);
        return response.getBody();
    }

    public void deleteDeposit(String depositId) {
        delete(brokerURL + "/admin/deposits/" + depositId, String.class);
    }

    public void deletePendingVault(String pendingVaultId) {
        delete(brokerURL + "/admin/pendingVaults/" + pendingVaultId, Void.class);
    }

    public BillingInformation updateBillingInfo(String vaultId, BillingInformation billingInfo) {
        ResponseEntity<BillingInformation> response = post(brokerURL + "/admin/billing/" + vaultId + "/updateBilling", BillingInformation.class, billingInfo);
        return response.getBody();
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

    public String auditDeposits() {
        ResponseEntity<String> response = get(brokerURL + "/admin/deposits/audit", String.class);
        return response.getBody();
    }

    public AuditInfo[] getAuditsListingAll() {
        ResponseEntity<AuditInfo[]> response = get(brokerURL + "/admin/audits", AuditInfo[].class);
        return response.getBody();
    }

    public EventInfo[] getVaultsRoleEvents(String vaultId) {
        ResponseEntity<EventInfo[]> response = get(brokerURL + "/vaults/" + vaultId + "/roleEvents", EventInfo[].class);
        return response.getBody();
    }

    public void deleteRetentionPolicy(String policyId) {
        ResponseEntity<Void> response = delete(brokerURL + "/admin/retentionpolicies/delete/" + policyId, Void.class);
    }

    public CreateRetentionPolicy addRetentionPolicy(CreateRetentionPolicy createRetentionPolicy) {
        ResponseEntity<CreateRetentionPolicy> response = post(brokerURL + "/admin/retentionpolicies", CreateRetentionPolicy.class, createRetentionPolicy);
        return response.getBody();
    }

    public CreateRetentionPolicy editRetentionPolicy(CreateRetentionPolicy createRetentionPolicy) {
        ResponseEntity<CreateRetentionPolicy> response = put(brokerURL + "/admin/retentionpolicies", CreateRetentionPolicy.class, createRetentionPolicy);
        return response.getBody();
    }


}
