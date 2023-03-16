package org.datavaultplatform.broker.scheduled;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.ZonedDateTime;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.config.MockRabbitConfig;
import org.datavaultplatform.broker.services.EmailService;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Deposit.Status;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.DepositReviewDeleteStatus;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.AuditChunkStatusDAO;
import org.datavaultplatform.common.model.dao.AuditDAO;
import org.datavaultplatform.common.model.dao.DepositChunkDAO;
import org.datavaultplatform.common.model.dao.DepositDAO;
import org.datavaultplatform.common.model.dao.DepositReviewDAO;
import org.datavaultplatform.common.model.dao.GroupDAO;
import org.datavaultplatform.common.model.dao.RetentionPolicyDAO;
import org.datavaultplatform.common.model.dao.VaultDAO;
import org.datavaultplatform.common.model.dao.VaultReviewDAO;
import org.datavaultplatform.common.services.LDAPService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.controllers.enabled=false",
    "broker.services.enabled=true",
    "broker.rabbit.enabled=false",
    "broker.ldap.enabled=false",
    "broker.initialise.enabled=true",
    "broker.email.enabled=false",
    "broker.database.enabled=true",
    "auditdeposit.schedule=-",
    "encryptioncheck.schedule=-",
    "review.schedule=-",
    "delete.schedule=-",
    "retentioncheck.schedule=-"})

@Import({MockRabbitConfig.class})
/**
 * This test is NOT checking the scheduling - it is testing the DB access from the scheduled tasks.
 * Before we added @Transactional to the Scheduled Task methods, we were seeing LazyInitializationExceptions,
 * in all Scheduled tasks except CheckRetentionPolicies.
 */
public class ScheduledTasksWithDbIT extends BaseReuseDatabaseTest {

  @MockBean
  LDAPService ldapService;

  @MockBean
  EmailService emailService;

  @Autowired
  AuditDepositsChunks scheduled1auditDepositsChunks;

  @Autowired
  CheckEncryptionData scheduled2checkEncryptionData;

  @Autowired
  CheckForDelete scheduled3checkForDelete;

  @Autowired
  CheckForReview scheduled4checkForReview;

  @Autowired
  CheckRetentionPolicies scheduled5checkRetentionPolicies;

  @Autowired
  DepositDAO depositDAO;

  @Autowired
  VaultDAO vaultDAO;

  @Autowired
  VaultReviewDAO vaultReviewDAO;

  @Autowired
  RetentionPolicyDAO retentionPolicyDAO;

  @Autowired
  DepositChunkDAO depositChunkDAO;

  @Autowired
  AuditChunkStatusDAO auditChunkStatusDao;

  @Autowired
  AuditDAO auditDAO;

  @Autowired
  DepositReviewDAO depositReviewDAO;

  @Autowired
  GroupDAO groupDAO;

  @Test
  void testScheduled1AuditDepositsChunks() {
    scheduled1auditDepositsChunks.execute();
  }

  @Test
  void testScheduled2CheckEncryptionDate() {
    scheduled2checkEncryptionData.execute();
  }

  @Test
  void testScheduled3CheckForDelete() throws Exception {
    scheduled3checkForDelete.execute();
  }

  @Test
  void testScheduled4CheckForReview() throws Exception {
    scheduled4checkForReview.execute();
  }

  @Test
  void testScheduled5CheckRetentionPolicies() {
    scheduled5checkRetentionPolicies.execute();
  }

  @BeforeEach
  void setup() {
    RetentionPolicy rp1 = new RetentionPolicy();
    rp1.setName("rp-1");
    rp1.setEngine("engine-1");
    rp1.setDescription("desc-1");
    rp1.setEndDate(new Date(ZonedDateTime.now().plusYears(1).toInstant().toEpochMilli()));
    retentionPolicyDAO.save(rp1);

    Vault vault = setupVault(rp1);
    Date moreThan2YearsAgo = new Date(
        ZonedDateTime.now().minusYears(2).minusDays(10).toInstant().toEpochMilli());
    setupDeposit(vault, moreThan2YearsAgo);
    Vault vaultForReview = setupVaultForReview(rp1);
  }
  private Vault setupVault(RetentionPolicy rp) {

    Vault vault = new Vault();
    vault.setContact("James Bond");
    vault.setName("test-vault");
    vault.setCreationTime(new Date());
    vault.setReviewDate(new Date(ZonedDateTime.now().plusYears(1).toInstant().toEpochMilli()));
    vault.setRetentionPolicy(rp);
    vaultDAO.save(vault);

    VaultReview vr1 = new VaultReview();
    vr1.setVault(vault);
    vr1.setId("vr-1");
    vr1.setCreationTime(new Date());

    VaultReview vr2 = new VaultReview();
    vr2.setVault(vault);
    vr2.setId("vr-2");
    vr2.setCreationTime(new Date());

    vaultReviewDAO.save(vr1);
    vaultReviewDAO.save(vr2);

    vaultDAO.save(vault);
    RetentionPolicy vaultRP = vault.getRetentionPolicy();
    assertNotNull(vaultRP);
    return vault;

  }

  private void setupDeposit(Vault vault, Date depositCreationTime) {
    Deposit deposit = new Deposit();
    deposit.setStatus(Status.COMPLETE);
    deposit.setHasPersonalData(false);
    deposit.setName("test-vault-1");
    deposit.setDescription("desc-test-vault-1");
    deposit.setCreationTime(depositCreationTime);
    deposit.setVault(vault);
    depositDAO.save(deposit);

    DepositChunk chunk1 = new DepositChunk();
    chunk1.setChunkNum(1);
    chunk1.setDeposit(deposit);

    depositChunkDAO.save(chunk1);

    DepositChunk chunk2 = new DepositChunk();
    chunk2.setChunkNum(2);
    chunk2.setDeposit(deposit);
    depositChunkDAO.save(chunk2);

    DepositChunk chunk3 = new DepositChunk();
    chunk3.setChunkNum(3);
    chunk3.setDeposit(deposit);
    depositChunkDAO.save(chunk3);

    VaultReview vr1 = new VaultReview();
    vr1.setVault(vault);
    vr1.setCreationTime(new Date());
    vaultReviewDAO.save(vr1);

    DepositReview dr = new DepositReview();
    dr.setVaultReview(vr1);
    dr.setDeposit(deposit);
    dr.setActionedDate(new Date());
    dr.setDeleteStatus(DepositReviewDeleteStatus.ONREVIEW);
    dr.setCreationTime(new Date());

    depositReviewDAO.save(dr);
  }

  private Vault setupVaultForReview(RetentionPolicy rp) {
    Group group = new Group();
    group.setID("group-1");
    group.setName("group-1-name");
    group.setEnabled(true);
    groupDAO.save(group);

    Vault vault = new Vault();
    vault.setRetentionPolicy(rp);
    vault.setContact("Bruce Wayne");
    vault.setName("test-vault-for-review");
    vault.setCreationTime(new Date());
    vault.setReviewDate(new Date(ZonedDateTime.now().plusMonths(5).toInstant().toEpochMilli()));
    vault.setGroup(group);

    vaultDAO.save(vault);
    return vault;
  }
}
