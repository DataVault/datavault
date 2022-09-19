package org.datavaultplatform.broker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.broker.services.ArchivesService;
import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.ExternalMetadataService;
import org.datavaultplatform.broker.services.FilesService;
import org.datavaultplatform.broker.services.JobsService;
import org.datavaultplatform.broker.services.MetadataService;
import org.datavaultplatform.broker.services.RetrievesService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.dao.AuditChunkStatusDAO;
import org.datavaultplatform.common.model.dao.DepositChunkDAO;
import org.datavaultplatform.common.model.dao.DepositDAO;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
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

  String bucketName = "BUCKET_NAME";
  String region = "Region";
  String awsAccessKey = "AWS_ACCESS_KEY";
  String awsSecretKey = "AWS_SECRET_KEY";
  String tsmRetryTime = "TSM_RETRY_TIME";
  String occRetryTime = "OCI_RETRY_TIME";
  String tsmMaxRetries = "TSM_MAX_RETRIES";
  String occMaxRetries = "OCI-MAX-RETRIES";
  String ociNameSpace = "OCI-NAMESPACE";
  String ociBucketName = "OCI-BUCKET-NAME";

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
  FilesService filesService = new FilesService();
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
  Sender sender;
  @TempDir
  File baseDir;

  File tempDir = new File(baseDir, "temp");
  File destDir = new File(baseDir, "dest");

  @Spy
  ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    tempDir.mkdir();
    destDir.mkdir();
  }

  protected DepositsController getDepositController() {
    DepositsService depositsService = new DepositsService(depositDao, depositChunkDao,
        auditChunkStatusDAO, 0, 0, 0, 0, 0, 0, 0);
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
