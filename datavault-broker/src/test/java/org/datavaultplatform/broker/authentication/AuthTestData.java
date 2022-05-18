package org.datavaultplatform.broker.authentication;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.DataManager;
import org.datavaultplatform.common.model.Dataset;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.FileFixity;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.PendingVault.Billing_Type;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.request.TransferVault;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.common.response.AuditInfo;
import org.datavaultplatform.common.response.BillingInformation;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.DepositSize;
import org.datavaultplatform.common.response.DepositsData;
import org.datavaultplatform.common.response.EventInfo;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;

public class AuthTestData {

  public static final VaultsData VAULTS_DATA;
  public static final ReviewInfo REVIEW_INFO_1;
  public static final ReviewInfo REVIEW_INFO_2;

  public static final VaultReview VAULT_REVIEW_1;
  public static final VaultReview VAULT_REVIEW_2;

  public static final DepositReview DEPOSIT_REVIEW_1;

  public static final DepositReview DEPOSIT_REVIEW_2;

  public static final ArchiveStore ARCHIVE_STORE_1;
  public static final ArchiveStore ARCHIVE_STORE_2;
  public static final AuditInfo AUDIT_INFO_1;
  public static final AuditInfo AUDIT_INFO_2;
  public static final DepositInfo DEPOSIT_INFO_1;
  public static final DepositInfo DEPOSIT_INFO_2;
  public static final DepositsData DEPOSIT_DATA_1;
  public static final EventInfo EVENT_INFO_1;
  public static final EventInfo EVENT_INFO_2;
  public static final Retrieve RETRIEVE_1;
  public static final Retrieve RETRIEVE_2;
  public static final DataManager DATA_MANAGER_1;
  public static final DataManager DATA_MANAGER_2;
  public static final VaultInfo VAULT_INFO_1;
  public static final VaultInfo VAULT_INFO_2;

  public static final Vault VAULT_1;
  public static final PendingVault PENDING_VAULT_1;
  public static final CreateVault CREATE_VAULT;
  public static final TransferVault TRANSFER_VAULT_1;

  public static final RoleModel ROLE_MODEL;
  public static final RoleAssignment ROLE_ASSIGNMENT;
  public static final PermissionModel PERMISSION_MODEL;
  public static final Deposit DEPOSIT_1;
  public static final Deposit DEPOSIT_2;
  public static final User USER_1;
  public static final User USER_2;
  public static final ValidateUser VALIDATE_USER;

  public static final CreateDeposit CREATE_DEPOSIT_1;
  public static final Job JOB_1;
  public static final Job JOB_2;
  public static final FileFixity FILE_FIXITY_1;
  public static final FileFixity FILE_FIXITY_2;
  public static final FileStore FILESTORE_1;
  public static final FileStore FILESTORE_2;
  public static final DepositSize DEPOSIT_SIZE;
  public static final FileInfo FILE_INFO_1;
  public static final FileInfo FILE_INFO_2;
  public static final Group GROUP_1;
  public static final Group GROUP_2;
  public static final Dataset DATASET_1;
  public static final Dataset DATASET_2;
  public static final CreateClientEvent CREATE_CLIENT_EVENT;
  public static final RetentionPolicy RETENTION_1;
  public static final RetentionPolicy RETENTION_2;
  public static final BillingInformation BILLING_INFO_1;
  public static final CreateRetentionPolicy CREATE_RETENTION_POLICY;

  static {
    VaultInfo vaultInfo1 = new VaultInfo();
    vaultInfo1.setID("vaultInfo1");

    VaultInfo vaultInfo2 = new VaultInfo();
    vaultInfo2.setID("vaultInfo2");

    VAULTS_DATA = new VaultsData();
    VAULTS_DATA.setData(Arrays.asList(vaultInfo1, vaultInfo2));
    VAULTS_DATA.setRecordsTotal(2);
    VAULTS_DATA.setRecordsFiltered(123);

    REVIEW_INFO_1 = new ReviewInfo();
    REVIEW_INFO_1.setVaultReviewId("2112");
    REVIEW_INFO_1.setDepositIds(Arrays.asList("d1", "d2"));
    REVIEW_INFO_1.setDepositReviewIds(Arrays.asList("dr1", "dr2"));

    REVIEW_INFO_2 = new ReviewInfo();
    REVIEW_INFO_2.setVaultReviewId("2222");
    REVIEW_INFO_2.setDepositIds(Arrays.asList("d3", "d4"));
    REVIEW_INFO_2.setDepositReviewIds(Arrays.asList("dr3", "dr4"));

    VAULT_REVIEW_1 = new VaultReview();
    VAULT_REVIEW_1.setId("vault-review-1");
    VAULT_REVIEW_1.setComment("this is a comment1");

    VAULT_REVIEW_2 = new VaultReview();
    VAULT_REVIEW_2.setId("vault-review-2");
    VAULT_REVIEW_2.setComment("this is a comment2");

    DEPOSIT_REVIEW_1 = new DepositReview();
    DEPOSIT_REVIEW_1.setId("deposit-review-1");
    DEPOSIT_REVIEW_1.setComment("this is a comment1");

    DEPOSIT_REVIEW_2 = new DepositReview();
    DEPOSIT_REVIEW_2.setId("deposit-review-2");
    DEPOSIT_REVIEW_2.setComment("this is a comment2");

    ARCHIVE_STORE_1 = new ArchiveStore();
    ARCHIVE_STORE_1.setLabel("ARCHIVE STORE 1");

    ARCHIVE_STORE_2 = new ArchiveStore();
    ARCHIVE_STORE_2.setLabel("ARCHIVE STORE 2");

    AUDIT_INFO_1 = new AuditInfo();
    AUDIT_INFO_1.setId("audit-info-1");

    AUDIT_INFO_2 = new AuditInfo();
    AUDIT_INFO_2.setId("audit-info-2");

    DEPOSIT_INFO_1 = new DepositInfo();
    DEPOSIT_INFO_1.setID("deposit-info-1");

    DEPOSIT_INFO_2 = new DepositInfo();
    DEPOSIT_INFO_2.setID("deposit-info-2");

    DEPOSIT_DATA_1 = new DepositsData();
    DEPOSIT_DATA_1.setData(Arrays.asList(DEPOSIT_INFO_1, DEPOSIT_INFO_2));
    DEPOSIT_DATA_1.setRecordsTotal(1234);
    DEPOSIT_DATA_1.setRecordsFiltered(123);

    EVENT_INFO_1 = new EventInfo();
    EVENT_INFO_1.setId("event-info-1");

    EVENT_INFO_2 = new EventInfo();
    EVENT_INFO_2.setId("event-info-2");

    RETRIEVE_1 = new Retrieve();
    RETRIEVE_1.setNote("retrieve 1");

    RETRIEVE_2 = new Retrieve();
    RETRIEVE_2.setNote("retrieve 2");

    DATA_MANAGER_1 = new DataManager();
    DATA_MANAGER_1.setUUN("data-manager-1");

    DATA_MANAGER_2 = new DataManager();
    DATA_MANAGER_2.setUUN("data-manager-2");

    VAULT_INFO_1 = new VaultInfo();
    VAULT_INFO_1.setID("vault-info-1");

    VAULT_INFO_2 = new VaultInfo();
    VAULT_INFO_2.setID("vault-info-2");

    VAULT_1 = new Vault();
    VAULT_1.setName("VAULT_ONE");

    PENDING_VAULT_1 = new PendingVault();
    PENDING_VAULT_1.setId("vault-id-1");

    CREATE_VAULT = new CreateVault();
    CREATE_VAULT.setName("create-vault-1");

    TRANSFER_VAULT_1 = new TransferVault();
    TRANSFER_VAULT_1.setReason("transfer-vault-1");

    ROLE_MODEL = new RoleModel();
    ROLE_MODEL.setId(1111L);
    ROLE_MODEL.setName("role-model-one");

    ROLE_ASSIGNMENT = new RoleAssignment();
    ROLE_ASSIGNMENT.setId(1234L);

    PERMISSION_MODEL = new PermissionModel();
    PERMISSION_MODEL.setId("permission-model-1");

    DEPOSIT_1 = new Deposit();
    DEPOSIT_1.setName("deposit-one");

    DEPOSIT_2 = new Deposit();
    DEPOSIT_2.setName("deposit-two");

    USER_1 = new User();
    USER_1.setID("user-one");
    USER_1.setLastname("last1");
    USER_1.setFirstname("first1");
    USER_1.setEmail("user.one@test.com");

    USER_2 = new User();
    USER_2.setID("user-two");
    USER_2.setLastname("last2");
    USER_2.setFirstname("first2");
    USER_2.setEmail("user.two@test.com");
    USER_2.setID("002");

    VALIDATE_USER = new ValidateUser();
    VALIDATE_USER.setUserid("validate-user-id-one");

    CREATE_DEPOSIT_1 = new CreateDeposit();
    CREATE_DEPOSIT_1.setName("create-deposit-1");

    JOB_1 = new Job();
    JOB_1.setDeposit(DEPOSIT_1);
    JOB_1.setState(100);

    JOB_2 = new Job();
    JOB_2.setDeposit(DEPOSIT_2);
    JOB_1.setState(200);

    FILE_FIXITY_1 = new FileFixity();
    FILE_FIXITY_1.setFile("file-1");
    FILE_FIXITY_1.setFixity("fixity-1");
    FILE_FIXITY_1.setFileType("type-1");
    FILE_FIXITY_1.setAlgorithm("algo-1");

    FILE_FIXITY_2 = new FileFixity();
    FILE_FIXITY_2.setFile("file-2");
    FILE_FIXITY_2.setFixity("fixity-2");
    FILE_FIXITY_2.setFileType("type-2");
    FILE_FIXITY_2.setAlgorithm("algo-2");

    FILESTORE_1 = new FileStore();
    FILESTORE_1.setLabel("file-store-1");

    FILESTORE_2 = new FileStore();
    FILESTORE_2.setLabel("file-store-2");

    DEPOSIT_SIZE = new DepositSize();
    DEPOSIT_SIZE.setResult(true);
    DEPOSIT_SIZE.setMax(2112L);

    FILE_INFO_1 = new FileInfo();
    FILE_INFO_1.setName("file-info-one");

    FILE_INFO_2 = new FileInfo();
    FILE_INFO_2.setName("file-info-two");

    GROUP_1 = new Group();
    GROUP_1.setID("group-one");

    GROUP_2 = new Group();
    GROUP_2.setID("group-two");

    DATASET_1 = new Dataset();
    DATASET_1.setID("dataset-1");

    DATASET_2 = new Dataset();
    DATASET_2.setID("dataset-2");

    CREATE_CLIENT_EVENT = new CreateClientEvent();
    CREATE_CLIENT_EVENT.setSessionId("session-123");

    RETENTION_1 = new RetentionPolicy();
    RETENTION_1.setId(1);
    RETENTION_1.setName("retention-policy-1");

    RETENTION_2 = new RetentionPolicy();
    RETENTION_2.setId(2);
    RETENTION_2.setName("retention-policy-2");

    BILLING_INFO_1 = new BillingInformation();
    BILLING_INFO_1.setAmountBilled(new BigDecimal("12.34"));
    BILLING_INFO_1.setBillingType(Billing_Type.ORIG);
    BILLING_INFO_1.setCreationTime(new Date());
    BILLING_INFO_1.setBudgetCode(true);
    BILLING_INFO_1.setId("123");
    BILLING_INFO_1.setContactName("Joe Bloggs");
    BILLING_INFO_1.setProjectId("project-123");
    BILLING_INFO_1.setProjectSize(1234L);
    BILLING_INFO_1.setProjectTitle("project-title");
    BILLING_INFO_1.setReviewDate(new Date());
    BILLING_INFO_1.setSchool("school of medicine");
    BILLING_INFO_1.setSubUnit("ortho");
    BILLING_INFO_1.setSliceID("slice1");
    BILLING_INFO_1.setSpecialComments("special K");
    BILLING_INFO_1.setUserName("user ABC");
    BILLING_INFO_1.setVaultID("vault-id-1");
    BILLING_INFO_1.setVaultName("vault-1-name");
    BILLING_INFO_1.setVaultSize(9999L);

    CREATE_RETENTION_POLICY = new CreateRetentionPolicy();
    CREATE_RETENTION_POLICY.setDescription("desc");
    CREATE_RETENTION_POLICY.setId(123);
    CREATE_RETENTION_POLICY.setName("the-name");
    CREATE_RETENTION_POLICY.setUrl("https://ed.ac.uk");
    CREATE_RETENTION_POLICY.setMinRetentionPeriod(123);
    CREATE_RETENTION_POLICY.setMinDataRetentionPeriod("min date retention period");
    CREATE_RETENTION_POLICY.setEngine("V8");
    CREATE_RETENTION_POLICY.setSort("bubble");
    CREATE_RETENTION_POLICY.setExtendUponRetrieval(true);
    CREATE_RETENTION_POLICY.setDateGuidanceReviewed("guidance reviewed date");
    CREATE_RETENTION_POLICY.setEndDate("end date");
    CREATE_RETENTION_POLICY.setInEffectDate("ineffect date");

  }
}
