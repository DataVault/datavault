package org.datavaultplatform.broker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.dao.AuditChunkStatusDAO;
import org.datavaultplatform.common.model.dao.DepositChunkDAO;
import org.datavaultplatform.common.model.dao.DepositDAO;
import org.datavaultplatform.common.model.dao.EventDAO;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@Slf4j
public abstract class BaseGenerateMessageTest {

  private static final String ARCHIVE_STORE_DEST_ID = "ARCHIVE-STORE-DST-ID";
  private static final String ARCHIVE_STORE_DEST_LABEL = "ARCHIVE-STORE-DST-LABEL";

  final String bucketName = "BUCKET_NAME";
  final String region = "Region";
  final String awsAccessKey = "AWS_ACCESS_KEY";
  final String awsSecretKey = "AWS_SECRET_KEY";
  final String tsmRetryTime = "TSM_RETRY_TIME";
  final String occRetryTime = "OCI_RETRY_TIME";
  final String tsmMaxRetries = "TSM_MAX_RETRIES";
  final String occMaxRetries = "OCI-MAX-RETRIES";
  final String ociNameSpace = "OCI-NAMESPACE";
  final String ociBucketName = "OCI-BUCKET-NAME";

  final String tsmReverse = "false";

  @Mock
  VaultsService vaultsService;
  @Mock
  DepositDAO depositDao;
  @Mock
  DepositChunkDAO depositChunkDao;
  @Mock
  AuditChunkStatusDAO auditChunkStatusDAO;
  @Mock
  RetrievesService retrievesService;
  @Mock
  MetadataService metadataService;
  @Mock
  ExternalMetadataService externalMetadataService;
  final FilesService filesService = new FilesService(new StorageClassNameResolver(true));
  @Mock
  UsersService usersService;
  @Mock
  ArchivesService archiveService;
  @Mock
  ArchiveStoreService archiveStoreService;
  @Mock
  JobsService jobsService;
  @Mock
  AdminService adminService;
  @Mock
  EventDAO eventDAO;
  @Mock
  Sender sender;
  @TempDir
  File baseDir;

  final File tempDir = new File(baseDir, "temp");
  final File destDir = new File(baseDir, "dest");

  final File optionsDir = new File(baseDir, "options");

  final int userFsRetrieveMaxAttempts = 10;
  final long userFsRetrieveDelaySeconds1 = TimeUnit.MINUTES.toSeconds(1);
  final long userFsRetrieveDelaySeconds2 = TimeUnit.MINUTES.toSeconds(5);

  @Spy
  final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    tempDir.mkdir();
    destDir.mkdir();
    optionsDir.mkdir();
  }

  protected DepositsController getDepositController() {
    DepositsService depositsService = new DepositsService(depositDao, depositChunkDao,
        auditChunkStatusDAO, eventDAO,0, 0, 0, 0, 0, 0, 0);
    DepositsController dc = new DepositsController(vaultsService,
        depositsService,
        retrievesService,
        metadataService,
        externalMetadataService,
        filesService,
        usersService,
        archiveStoreService,
        jobsService,
        adminService,
        sender,
        optionsDir.getAbsolutePath(),
        tempDir.getAbsolutePath(),
        bucketName,
        region,
        awsAccessKey,
        awsSecretKey,
        tsmRetryTime,
        occRetryTime,
        tsmMaxRetries,
        occMaxRetries,
        ociNameSpace,
        ociBucketName,
        tsmReverse,
        userFsRetrieveMaxAttempts,
        userFsRetrieveDelaySeconds1,
        userFsRetrieveDelaySeconds2,
        mapper
    );
    return dc;
  }

  @SneakyThrows
  protected final ArchiveStore getDestinationArchiveStore() {
    HashMap<String, String> map = new HashMap<>();
    map.put(LocalFileSystem.ROOT_PATH, destDir.getAbsolutePath());
    ArchiveStore result = new ArchiveStore(LocalFileSystem.class.getName(), map,
        ARCHIVE_STORE_DEST_LABEL, true);
    Field fID = ArchiveStore.class.getDeclaredField("id");
    fID.setAccessible(true);
    fID.set(result, ARCHIVE_STORE_DEST_ID);
    return result;
  }


  @AfterEach
  final void tearDown() {
    baseDir.delete();
  }


}
