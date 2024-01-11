package org.datavaultplatform.broker.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.Archive;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.Retrieve.Status;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@Slf4j
public class GenerateRetrieveMessageTest extends BaseGenerateMessageTest {

  private static final String FILE_STORE_RETRIEVE_ID = "FILE-STORE-SRC-ID";
  private static final String FILE_STORE_RETRIEVE_LABEL = "FILE_STORE-SRC-LABEL";
  private static final String ARCHIVE_STORE_DEST_ID = "ARCHIVE-STORE-DST-ID";
  private static final String ARCHIVE_STORE_DEST_LABEL = "ARCHIVE-STORE-DST-LABEL";
  final File retrieveDir = new File(baseDir, "retrieved");
  final File destDir = new File(baseDir, "dest");
  @Captor
  ArgumentCaptor<String> argMessage;
  private DepositsController dc;

  @BeforeEach
  void setup() {
    retrieveDir.mkdir();
    destDir.mkdir();
    dc = getDepositController();
  }

  @SneakyThrows
  private FileStore getRetrieveFileStore() {
    HashMap<String, String> map = new HashMap<>();
    map.put(LocalFileSystem.ROOT_PATH, retrieveDir.getAbsolutePath());
    FileStore store = new FileStore(LocalFileSystem.class.getName(), map,
        FILE_STORE_RETRIEVE_LABEL);
    Field fID = FileStore.class.getDeclaredField("id");
    fID.setAccessible(true);
    fID.set(store, FILE_STORE_RETRIEVE_ID);
    return store;
  }

  @Test
  @SneakyThrows
  void testRetrieveDepositToGenerateRetrieveMessage() {
    User mockUser = mock(User.class);
    when(mockUser.getID()).thenReturn("used-id-one");
    when(mockUser.getFileStores()).thenReturn(Arrays.asList(getRetrieveFileStore()));

    Vault mockVault = mock(Vault.class);
    when(mockVault.getID()).thenReturn("vault-123");

    Deposit deposit = new Deposit();
    Field fDepositId = Deposit.class.getDeclaredField("id");
    fDepositId.setAccessible(true);
    fDepositId.set(deposit, "deposit-id-123");
    deposit.setName("test-deposit");
    deposit.setBagId("bag-id-123");
    deposit.setUser(mockUser);
    deposit.setNumOfChunks(1);
    deposit.setVault(mockVault);
    deposit.setCreationTime(new Date());
    DepositChunk chunk1 = new DepositChunk();
    chunk1.setDeposit(deposit);
    chunk1.setChunkNum(1);
    chunk1.setEncIV(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
    chunk1.setEcnArchiveDigest("enc-chunk-1-digest");
    chunk1.setArchiveDigestAlgorithm(Verify.SHA_1_ALGORITHM);
    deposit.setDepositChunks(Arrays.asList(chunk1));

    Archive archive = new Archive();
    archive.setArchiveStore(getDestinationArchiveStore());
    Field fArchiveID = Archive.class.getDeclaredField("archiveId");
    fArchiveID.setAccessible(true);
    fArchiveID.set(archive, getDestinationArchiveStore().getID());

    Field fArchives = Deposit.class.getDeclaredField("archives");
    fArchives.setAccessible(true);
    fArchives.set(deposit, Arrays.asList(archive));

    when(depositDao.findById("deposit-id-123")).thenReturn(Optional.of(deposit));

    Field fJobs = Deposit.class.getDeclaredField("jobs");
    fJobs.setAccessible(true);
    fJobs.set(deposit, new ArrayList<>());

    Retrieve retrieve = new Retrieve();
    Field retrieveId = Retrieve.class.getDeclaredField("id");
    retrieveId.setAccessible(true);
    retrieveId.set(retrieve, "retrieve-id-123");
    retrieve.setDeposit(deposit);
    retrieve.setUser(mockUser);
    retrieve.setRetrievePath(FILE_STORE_RETRIEVE_ID + "/some-path-to-file");
    retrieve.setStatus(Status.NOT_STARTED);

    when(archiveStoreService.getForRetrieval()).thenReturn(this.getDestinationArchiveStore());

    when(usersService.getUser("user123")).thenReturn(mockUser);
    when(depositDao.findById("deposit-id-123")).thenReturn(Optional.of(deposit));

    when(sender.send(argMessage.capture())).thenReturn("message-id");

    dc.retrieveDeposit("user123", "deposit-id-123", retrieve);

    String sentMessage = argMessage.getValue();
    log.info("START SENT MESSAGE");
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    JsonNode tree = mapper.readTree(sentMessage);
    log.info("Generated Message {}", tree.toPrettyString());

  }

}
