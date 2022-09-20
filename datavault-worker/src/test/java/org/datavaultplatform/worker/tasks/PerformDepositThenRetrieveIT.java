package org.datavaultplatform.worker.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.rabbitmq.client.Channel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.awaitility.Awaitility;
import org.datavaultplatform.common.bagish.Checksummer;
import org.datavaultplatform.common.bagish.SupportedAlgorithm;
import org.datavaultplatform.common.config.BaseQueueConfig;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.event.deposit.UploadComplete;
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.io.FileUtils;
import org.datavaultplatform.common.task.Context.AESMode;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.rabbit.BaseRabbitTCTest;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@DirtiesContext
@Slf4j
public class PerformDepositThenRetrieveIT extends BaseRabbitTCTest {

  public static final String RETRIEVE_PATH_1_BAGIT_ID = "$.properties.bagId";
  public static final String RETRIEVE_PATH_2_NUM_CHUNKS = "$.properties.numOfChunks";
  public static final String RETRIEVE_PATH_3_RETRIEVE_PATH = "$.properties.retrievePath";
  public static final String RETRIEVE_PATH_4_ARCHIVE_SIZE = "$.properties.archiveSize";
  public static final String RETRIEVE_PATH_5_ARCHIVE_DIGEST = "$.properties.archiveDigest";
  public static final String RETRIEVE_PATH_6_ARCHIVE_ID = "$.properties.archiveId";
  public static final String RETRIEVE_PATH_7_CHUNK_FILES_DIGEST_1 = "$.chunkFilesDigest['1']";
  public static final String RETRIEVE_PATH_8_CHUNKS_IV_1 = "$.chunksIVs['1']";
  public static final String RETRIEVE_PATH_9_ENC_CHUNKS_DIGEST_1 = "$.encChunksDigest['1']";
  public static final String RETRIEVE_PATH_10_ARCHIVE_STORE_ROOT_PATH = "$.archiveFileStores[0].properties.rootPath";
  public static final String RETRIEVE_PATH_11_RETRIEVE_ROOT_PATH = "$.userFileStoreProperties.FILE-STORE-SRC-ID.rootPath";

  static final String KEY_NAME_FOR_SSH = "key-name-for-ssh";
  static final String KEY_NAME_FOR_DATA = "key-name-for-data";
  static final String KEY_STORE_PASSWORD = "testPassword";
  static final File baseTemp;

  static {
    try {
      Path dir = Paths.get("/tmp/TEST");
      if (Files.exists(dir)) {
        assertTrue(Files.isDirectory(dir));
        FileUtils.deleteDirectory(dir.toFile());
      }
      baseTemp = Files.createDirectory(dir).toFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  final Resource depositMessage = new ClassPathResource("sampleMessages/sampleDepositMessage.json");
  final Resource retrieveMessage = new ClassPathResource(
      "sampleMessages/sampleRetrieveMessage.json");
  final List<Event> events = new ArrayList<>();
  @Autowired
  protected AmqpAdmin rabbitAdmin;
  @Autowired
  protected RabbitTemplate template;
  @Autowired
  @Qualifier("workerQueue") //the name of the bean, not the Q
  protected Queue workerQueue;
  String keyStorePath;
  @Value("${tempDir}")
  String tempDir;
  @Value("${metaDir}")
  String metaDir;
  File sourceDir;
  File destDir;
  File retrieveBaseDir;
  File retrieveDir;
  @Autowired
  ObjectMapper mapper;

  @SneakyThrows
  static Set<Path> getPathsWithinTarFile(File tarFile) {
    Set<Path> paths = new HashSet<>();
    try (TarArchiveInputStream tarIn = new TarArchiveInputStream(new FileInputStream(tarFile))) {
      TarArchiveEntry entry;
      while ((entry = tarIn.getNextTarEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        paths.add(Paths.get(entry.getName()));
      }
    }
    return paths;
  }

  @DynamicPropertySource
  @SneakyThrows
  static void setupProperties(DynamicPropertyRegistry registry) {
    File tempDir = new File(baseTemp, "temp");
    assertTrue(tempDir.mkdir());

    File metaDir = new File(baseTemp, "meta");
    assertTrue(metaDir.mkdir());

    String tempDirValue = tempDir.getCanonicalPath();
    String metaDirValue = metaDir.getCanonicalPath();

    registry.add("tempDir", () -> tempDirValue);
    registry.add("metaDir", () -> metaDirValue);
  }

  protected String sendNormalMessage(String msgBody) {
    MessageProperties props = new MessageProperties();
    props.setMessageId(UUID.randomUUID().toString());
    props.setPriority(NORMAL_PRIORITY);
    Message msg = new Message(msgBody.getBytes(StandardCharsets.UTF_8), props);
    template.send(workerQueue.getActualName(), msg);
    return props.getMessageId();
  }

  @BeforeEach
  @SneakyThrows
  void setup() {
    purgeQueues();
    setupKeystore();
    setupDirectoriesAndFiles();
  }

  @SneakyThrows
  private void setupDirectoriesAndFiles() {
    sourceDir = baseTemp.toPath().resolve("source").toFile();
    assertTrue(sourceDir.mkdir());
    destDir = baseTemp.toPath().resolve("dest").toFile();
    assertTrue(destDir.mkdir());
    retrieveBaseDir = baseTemp.toPath().resolve("retrieve").toFile();
    assertTrue(retrieveBaseDir.mkdir());
    retrieveDir = retrieveBaseDir.toPath().resolve("ret-folder").toFile();
    assertTrue(retrieveDir.mkdir());
    log.info("meta.dir   [{}]", metaDir);
    log.info("temp.dir   [{}]", tempDir);
    log.info("source dir [{}]", sourceDir);
    log.info("dest   dir [{}]", destDir);
    log.info("retrieve base dir [{}]", retrieveBaseDir);
    log.info("retrieve dir [{}]", retrieveDir);

    File sourceFileDir = new File(sourceDir, "src-path-1");
    assertTrue(sourceFileDir.mkdir());
    assertTrue(sourceFileDir.exists() && sourceFileDir.isDirectory());

    File sourceFile = new File(sourceFileDir, "src-file-1.txt");
    try (FileWriter fw = new FileWriter(sourceFile)) {
      fw.write("This is a test file");
    }
    assertTrue(sourceFile.exists() && sourceFile.isFile());
  }

  @SneakyThrows
  void setupKeystore() {
    Encryption.addBouncyCastleSecurityProvider();
    keyStorePath = baseTemp.toPath().resolve("test.ks").toFile().getCanonicalPath();
    log.info("BASE TEMP IS AT [{}]", baseTemp.getCanonicalPath());
    log.info("TEMP KEY  IS AT [{}]", keyStorePath);
    Encryption enc = new Encryption();
    enc.setVaultEnable(false);
    enc.setVaultPrivateKeyEncryptionKeyName(KEY_NAME_FOR_SSH);
    enc.setVaultDataEncryptionKeyName(KEY_NAME_FOR_DATA);

    enc.setKeystoreEnable(true);
    enc.setKeystorePath(keyStorePath);
    enc.setKeystorePassword(KEY_STORE_PASSWORD);

    SecretKey keyForSSH = Encryption.generateSecretKey();
    SecretKey keyForData = Encryption.generateSecretKey();

    assertFalse(new File(keyStorePath).exists());

    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultPrivateKeyEncryptionKeyName(),
        keyForSSH);
    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultDataEncryptionKeyName(),
        keyForData);
    assertTrue(new File(keyStorePath).exists());

    assertTrue(new File(keyStorePath).exists());
  }

  void purgeQueues() {
    rabbitAdmin.purgeQueue(workerQueue.getActualName(), false);
    rabbitAdmin.purgeQueue(workerQueue.getActualName(), false);
    assertEquals(0, rabbitAdmin.getQueueInfo(workerQueue.getActualName()).getMessageCount());
    assertEquals(0, rabbitAdmin.getQueueInfo(workerQueue.getActualName()).getMessageCount());
  }

  @Test
  @SneakyThrows
  void testDepositThenRetrieve() {
    assertEquals(0, destDir.listFiles().length);
    String depositMessage = getSampleDepositMessage();
    log.info("depositMessage {}", getSampleDepositMessage());
    sendNormalMessage(depositMessage);
    Awaitility.await()
        .atMost(10, TimeUnit.MINUTES)
        .pollInterval(10, TimeUnit.SECONDS)
        .until(() -> events.size() == 17L);

    String bagId = checkDepositWorkedOkay(depositMessage);

    buildAndSendRetrieveMessage(bagId);
    checkRetrieve();
  }

  @SneakyThrows
  private void checkRetrieve() {
    log.info("FIN {}", retrieveDir.getCanonicalPath());
    Awaitility.await().atMost(5, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(10))
        .until(this::foundRetrieveComplete);
    log.info("FIN {}", retrieveDir.getCanonicalPath());
    File file = new File("/tmp/TEST/retrieve/ret-folder/src-path-1/src-file-1.txt");
    String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    assertEquals("This is a test file", content);
  }

  boolean foundRetrieveComplete() {
    return events.stream()
        .anyMatch(e -> e.getClass().equals(RetrieveComplete.class));
  }

  @SneakyThrows
  private String checkDepositWorkedOkay(String depositMessage) {
    Deposit deposit = mapper.readValue(depositMessage, Deposit.class);
    String bagId = deposit.getProperties().get("bagId");

    log.info("BROKER MSG COUNT {}", events.size());
    File[] destFiles = destDir.listFiles();
    assertEquals(1, destFiles.length);
    File expectedEncChunk1 = destDir.toPath().resolve(bagId + ".tar.1").toFile();
    assertEquals(expectedEncChunk1, destFiles[0]);

    ComputedEncryption computedEncryption = getComputedEncryption();

    String encHash = computedEncryption.getEncChunkDigests().get(1);
    assertEquals(encHash, getSha1Hash(expectedEncChunk1));

    AESMode aesMode = AESMode.valueOf(computedEncryption.getAesMode());
    assertEquals(AESMode.GCM, aesMode);

    byte[] iv = computedEncryption.getChunkIVs().get(1);

    File decryptedTarFile = Files.createTempFile("decrypted", ".plain").toFile();
    FileUtils.copyFile(expectedEncChunk1, decryptedTarFile);
    Encryption.decryptFile(aesMode, decryptedTarFile, iv);
    assertTrue(decryptedTarFile.length() > 0);
    assertTrue(decryptedTarFile.length() != expectedEncChunk1.length());

    ComputedDigest computedDigest = getComputedDigest();

    Set<Path> tarEntryPaths = getPathsWithinTarFile(decryptedTarFile);

    Path base = Paths.get(bagId);
    assertThat(tarEntryPaths).containsExactlyInAnyOrder(
        base.resolve("bagit.txt"),
        base.resolve("manifest-md5.txt"),
        base.resolve("tagmanifest-md5.txt"),

        base.resolve("data/src-path-1/src-file-1.txt"),

        base.resolve("metadata/filetype.json"),
        base.resolve("metadata/vault.json"),
        base.resolve("metadata/external.txt"),
        base.resolve("metadata/deposit.json"));

    assertEquals(computedDigest.getDigest(), getSha1Hash(decryptedTarFile));

    return bagId;
  }

  private ComputedEncryption getComputedEncryption() {
    return findEvent(ComputedEncryption.class);
  }

  private UploadComplete getUploadComplete() {
    return findEvent(UploadComplete.class);
  }

  private Complete getComplete() {
    return findEvent(Complete.class);
  }

  private ComputedDigest getComputedDigest() {
    return findEvent(ComputedDigest.class);
  }

  private <T> T findEvent(Class<T> clazz) {
    return events.stream()
        .filter(e -> clazz.isAssignableFrom(e.getClass()))
        .map(e -> (T) e)
        .findFirst()
        .get();
  }

  @SneakyThrows
  private void buildAndSendRetrieveMessage(String bagId) {
    String temp1 = FileUtils.readFileToString(this.retrieveMessage.getFile(),
        StandardCharsets.UTF_8);

    RetrieveInfo info = getRetrieveInfo(bagId);
    DocumentContext ctx = JsonPath.parse(temp1);

    ctx.set(RETRIEVE_PATH_1_BAGIT_ID, info.bagitId);
    ctx.set(RETRIEVE_PATH_2_NUM_CHUNKS, String.valueOf(info.numChunks));
    ctx.set(RETRIEVE_PATH_3_RETRIEVE_PATH, info.retrievePath);
    ctx.set(RETRIEVE_PATH_4_ARCHIVE_SIZE, String.valueOf(info.archiveSize));
    ctx.set(RETRIEVE_PATH_5_ARCHIVE_DIGEST, info.archiveDigest);
    ctx.set(RETRIEVE_PATH_6_ARCHIVE_ID, info.archiveId);
    ctx.set(RETRIEVE_PATH_7_CHUNK_FILES_DIGEST_1, info.chunk1digest);
    ctx.set(RETRIEVE_PATH_8_CHUNKS_IV_1, info.chunk1iv);
    ctx.set(RETRIEVE_PATH_9_ENC_CHUNKS_DIGEST_1, info.chunk1encDigest);
    ctx.set(RETRIEVE_PATH_10_ARCHIVE_STORE_ROOT_PATH, info.rootPathArchiveStore);
    ctx.set(RETRIEVE_PATH_11_RETRIEVE_ROOT_PATH, info.rootPathRetrieve);

    String retrieveMessage = ctx.jsonString();
    sendNormalMessage(retrieveMessage);
  }

  @SneakyThrows
  private RetrieveInfo getRetrieveInfo(String bagId) {
    RetrieveInfo info = new RetrieveInfo();
    info.bagitId = bagId;

    ComputedEncryption computedEncryption = getComputedEncryption();
    info.numChunks = computedEncryption.getChunkIVs().size();

    info.retrievePath = this.retrieveDir.getName();

    info.archiveSize = getComplete().getArchiveSize();

    info.archiveDigest = getComputedDigest().getDigest();

    info.archiveId = getUploadComplete().getArchiveIds().get("ARCHIVE-STORE-DST-ID");

    info.chunk1iv = base64Encode(computedEncryption.getChunkIVs().get(1));
    info.chunk1digest = computedEncryption.getChunksDigest().get(1);
    info.chunk1encDigest = computedEncryption.getEncChunkDigests().get(1);

    info.rootPathArchiveStore = this.destDir.getCanonicalPath();
    info.rootPathRetrieve = this.retrieveBaseDir.getCanonicalPath();

    return info;
  }

  @SneakyThrows
  private String getSampleDepositMessage() {
    String temp1 = FileUtils.readFileToString(this.depositMessage.getFile(),
        StandardCharsets.UTF_8);
    String temp2 = temp1.replaceAll("/tmp/dv/src", sourceDir.getCanonicalPath());
    return temp2.replaceAll("/tmp/dv/dest", destDir.getCanonicalPath());
  }

  @RabbitListener(queues = BaseQueueConfig.BROKER_QUEUE_NAME)
  @SneakyThrows
  void receiveBrokerMessage(Message message, Channel channel,
      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
    channel.basicAck(deliveryTag, false);

    String msgBody = new String(message.getBody(), StandardCharsets.UTF_8);
    events.add(extractEvent(msgBody));

    log.info("Received message for broker [{}]", events.size());
  }

  @SneakyThrows
  private Event extractEvent(String message) {
    Event event = mapper.readValue(message, Event.class);
    String eventClassName = event.getEventClass();
    Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName(eventClassName);
    return mapper.readValue(message, eventClass);
  }

  private String getSha1Hash(File file) throws Exception {
    return new Checksummer()
        .computeFileHash(file, SupportedAlgorithm.SHA1)
        .toUpperCase();
  }

  private String base64Encode(byte[] data) {
    return Base64.getEncoder().encodeToString(data);
  }

  static class RetrieveInfo {

    String bagitId;
    int numChunks;

    long archiveSize;
    String archiveDigest;
    String archiveId;

    String chunk1digest;
    String chunk1encDigest;
    String chunk1iv;

    String rootPathArchiveStore;
    String rootPathRetrieve;
    String retrievePath;
  }
}