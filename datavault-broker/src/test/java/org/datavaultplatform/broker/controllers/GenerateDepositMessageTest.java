package org.datavaultplatform.broker.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@Slf4j
public class GenerateDepositMessageTest extends BaseGenerateMessageTest {

  public static final String PATH_BAG_ID = "$.properties.bagId";
  public static final String PATH_SRC_ROOT = "$.userFileStoreProperties.FILE-STORE-SRC-ID.rootPath";
  public static final String PATH_DEST_ROOT = "$.archiveFileStores[0].properties['rootPath']";
  public static final String BAG_ID = "bag-id-123";
  private static final String FILE_STORE_SRC_ID = "FILE-STORE-SRC-ID";
  private static final String FILE_STORE_SRC_LABEL = "FILE_STORE-SRC-LABEL";

  final File srcDir = new File(baseDir, "src");

  @Captor
  ArgumentCaptor<String> argMessage;
  private DepositsController dc;

  @BeforeEach
  void setup() {
    srcDir.mkdir();
    dc = getDepositController();
  }


  @SneakyThrows
  private FileStore getSourceFileStore() {
    HashMap<String, String> map = new HashMap<>();
    map.put(LocalFileSystem.ROOT_PATH, srcDir.getAbsolutePath());
    FileStore store = new FileStore(LocalFileSystem.class.getName(), map, FILE_STORE_SRC_LABEL);
    Field fID = FileStore.class.getDeclaredField("id");
    fID.setAccessible(true);
    fID.set(store, FILE_STORE_SRC_ID);
    return store;
  }

  @Test
  @SneakyThrows
  void testAddDepositToGenerateDepositMessage() {
    CreateDeposit cd = new CreateDeposit();
    cd.setName("test-deposit");
    cd.setDescription("a-test-deposit");
    cd.setVaultID("vault-123");
    cd.setPersonalDataStatement("personal-data-statement");
    cd.setHasPersonalData("yes");
    cd.setDepositPaths(List.of(FILE_STORE_SRC_ID + "/src-path-1"));
    cd.setFileUploadHandle("src-file-upload-handle");

    User mockUser = mock(User.class);
    when(mockUser.getID()).thenReturn("used-id-one");
    when(sender.send(argMessage.capture())).thenReturn("MESSAGE_ID");

    Vault mockVault = mock(Vault.class);
    VaultInfo mockVaultInfo = mock(VaultInfo.class);
    when(mockVault.convertToResponse()).thenReturn(mockVaultInfo);
    when(mockVault.getID()).thenReturn("vault-123");

    when(usersService.getUser("user123")).thenReturn(mockUser);

    when(mockUser.getFileStores()).thenReturn(List.of(getSourceFileStore()));

    when(vaultsService.getUserVault(mockUser, "vault-123")).thenReturn(mockVault);

    List<ArchiveStore> destStores = List.of(getDestinationArchiveStore());
    when(archiveStoreService.getArchiveStores()).thenReturn(destStores);

    when(depositDao.save(any())).thenAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      Deposit deposit = (Deposit) args[0];
      Field fID = Deposit.class.getDeclaredField("id");
      fID.setAccessible(true);
      fID.set(deposit, "deposit-id-123");
      return null;
    });

    doAnswer(invocationOnMock -> {
      Object arg = invocationOnMock.getArguments()[0];
      if (arg instanceof VaultInfo) {
        return "\"VAULT\":\"META-DATA\"";
      } else if (arg instanceof DepositInfo) {
        return "\"DEPOSIT\":\"META-DATA\"";
      } else {
        return invocationOnMock.callRealMethod();
      }
    }).when(mapper).writeValueAsString(any());

    dc.addDeposit("user123", cd);

    String sentMessage = argMessage.getValue();
    log.info("START SENT MESSAGE");
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    DocumentContext generated = JsonPath.parse(sentMessage);

    //overwrite the random bagId with the one we know
    String bagId = BAG_ID;
    String srcPath = srcDir.getAbsolutePath();
    String destPath = destDir.getAbsolutePath();

    generated.set(PATH_BAG_ID, bagId);
    generated.set(PATH_DEST_ROOT, destPath);
    generated.set(PATH_SRC_ROOT, srcPath);

    String actualBagId = generated.read(PATH_BAG_ID).toString();
    log.info("actualBagId [{}]", actualBagId);
    assertEquals(bagId, actualBagId);

    String actualSrcPath = generated.read(PATH_SRC_ROOT).toString();
    log.info("actualSrcPath [{}]", actualSrcPath);
    assertEquals(srcPath, actualSrcPath);

    String actualDestPath = generated.read(PATH_DEST_ROOT).toString();
    log.info("actualDestPath [{}]", actualDestPath);
    assertEquals(destPath, actualDestPath);

    JsonNode expected = mapper.readTree(getExpectedJson(bagId, srcPath, destPath));
    assertEquals(expected, convert(generated));
    log.info("Generated Message {}", expected.toPrettyString());
    log.info("END SENT MESSAGE");
  }

  JsonNode convert(DocumentContext ctx) {
    return mapper.valueToTree(ctx.json());
  }

  private String getExpectedJson(String bagId, String srcRoot, String destRoot) {
    return "{"
        + "  \"taskClass\" : \"org.datavaultplatform.worker.tasks.Deposit\","
        + "  \"jobID\" : null,"
        + "  \"properties\" : {"
        + "    \"bagId\" : \"" + bagId + "\"," //overwritten
        + "    \"vaultMetadata\" : \"\\\"VAULT\\\":\\\"META-DATA\\\"\","
        + "    \"depositId\" : \"deposit-id-123\","
        + "    \"depositMetadata\" : \"\\\"DEPOSIT\\\":\\\"META-DATA\\\"\","
        + "    \"userId\" : \"used-id-one\","
        + "    \"userFsRetryMaxAttempts\": \"10\","
        + "    \"userFsRetryDelayMs1\": \"60\","
        + "    \"userFsRetryDelayMs2\": \"300\","
        + "    \"depositChunksStored\": \"\"" 
        + "  },"
        + "  \"fileStorePaths\" : [ \"FILE-STORE-SRC-ID/src-path-1\" ],"
        + "  \"fileUploadPaths\" : [ \"src-file-upload-handle\" ],"
        + "  \"archiveFileStores\" : [ {"
        + "    \"id\" : \"ARCHIVE-STORE-DST-ID\","
        + "    \"storageClass\" : \"org.datavaultplatform.common.storage.impl.LocalFileSystem\","
        + "    \"label\" : \"ARCHIVE-STORE-DST-LABEL\","
        + "    \"retrieveEnabled\" : true,"
        + "    \"properties\" : {"
        + "      \"rootPath\" : \"" + destRoot + "\""//overwritten
        + "    }"
        + "  } ],"
        + "  \"userFileStoreProperties\" : {"
        + "    \"FILE-STORE-SRC-ID\" : {"
        + "      \"rootPath\" : \"" + srcRoot + "\""//overwritten
        + "    }"
        + "  },"
        + "  \"userFileStoreClasses\" : {"
        + "    \"FILE-STORE-SRC-ID\" : \"org.datavaultplatform.common.storage.impl.LocalFileSystem\""
        + "  },"
        + "  \"chunkFilesDigest\" : null,"
        + "  \"tarIV\" : null,"
        + "  \"chunksIVs\" : null,"
        + "  \"encTarDigest\" : null,"
        + "  \"encChunksDigest\" : null,"
        + "  \"lastEvent\" : null,"
        + "  \"chunksToAudit\" : null,"
        + "  \"archiveIds\" : null,"
        + "  \"restartArchiveIds\" : { },"
        + "  \"redeliver\" : false"
        + "}";
  }
}
