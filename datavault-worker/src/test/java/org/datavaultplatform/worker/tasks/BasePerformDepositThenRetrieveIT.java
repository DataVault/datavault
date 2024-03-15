package org.datavaultplatform.worker.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.worker.tasks.Retrieve.DATA_VAULT_HIDDEN_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.awaitility.Awaitility;
import org.datavaultplatform.common.config.BaseQueueConfig;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.io.FileUtils;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context.AESMode;
import org.datavaultplatform.worker.rabbit.BaseRabbitTCTest;
import org.datavaultplatform.worker.utils.DepositEvents;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Slf4j
public abstract class BasePerformDepositThenRetrieveIT extends BaseRabbitTCTest {

  static final String KEY_NAME_FOR_SSH = "key-name-for-ssh";
  static final String KEY_NAME_FOR_DATA = "key-name-for-data";
  static final String KEY_STORE_PASSWORD = "testPassword";

  final Resource depositMessage = new ClassPathResource("sampleMessages/sampleDepositMessage.json");

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

  @Value("classpath:big_data/50MB_file")
  Resource largeFile;

  @Value("${chunking.enabled:false}")
  private boolean chunkingEnabled;

  @Value("${chunking.size:0}")
  private String chunkingByteSize;

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
    File baseTemp = Files.createTempDirectory("test").toFile();
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
    checkChunkingProps(this.chunkingEnabled, this.chunkingByteSize);
    purgeQueues();
    setupKeystore();
    setupDirectoriesAndFiles();
  }

  abstract void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize);

  @SneakyThrows
  private void setupDirectoriesAndFiles() {
    Path baseTemp = Paths.get(this.tempDir);

    sourceDir = baseTemp.resolve("source").toFile();
    assertTrue(sourceDir.mkdir());
    destDir = baseTemp.resolve("dest").toFile();
    assertTrue(destDir.mkdir());
    retrieveBaseDir = baseTemp.resolve("retrieve").toFile();
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

    File sourceFile = new File(sourceFileDir, "src-file-1");
    Files.copy(this.largeFile.getFile().toPath(), sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

    assertTrue(sourceFile.exists() && sourceFile.isFile());
    assertEquals(50_000_000, sourceFile.length());
  }

  @SneakyThrows
  void setupKeystore() {
    Path baseTemp = Paths.get(this.tempDir);
    Encryption.addBouncyCastleSecurityProvider();
    keyStorePath = baseTemp.resolve("test.ks").toFile().getCanonicalPath();
    log.info("BASE TEMP IS AT [{}]", baseTemp.toFile().getCanonicalPath());
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
    Deposit deposit = new ObjectMapper().readValue(depositMessage, Deposit.class);
    log.info("depositMessage {}", depositMessage);
    sendNormalMessage(depositMessage);
    waitUntil(this::foundComplete);

    DepositEvents depositEvents = new DepositEvents(deposit, this.events);

    checkDepositWorkedOkay(depositMessage, depositEvents);

    File hiddenFile = new File(this.retrieveDir, DATA_VAULT_HIDDEN_FILE_NAME);
    assertThat(hiddenFile).doesNotExist();
    buildAndSendRetrieveMessage(depositEvents);
    checkRetrieve();
    assertThat(hiddenFile).exists().isFile().isReadable();
  }

  void waitUntil(Callable<Boolean> test) {
    Awaitility.await().atMost(1, TimeUnit.MINUTES)
        .pollInterval(Duration.ofSeconds(15))
        .until(test);
  }

  @SneakyThrows
  private void checkRetrieve() {
    log.info("FIN {}", retrieveDir.getCanonicalPath());
    waitUntil(this::foundRetrieveComplete);
    log.info("FIN {}", retrieveDir.getCanonicalPath());
    File retrieved = new File(this.retrieveDir + "/src-path-1/src-file-1");

    String digestOriginal = Verify.getDigest(this.largeFile.getFile());
    String digestRetrieved = Verify.getDigest(retrieved);

    assertEquals(digestOriginal, digestRetrieved);
  }

  boolean foundRetrieveComplete() {
    return events.stream()
        .anyMatch(e -> e.getClass().equals(RetrieveComplete.class));
  }

  boolean foundComplete() {
    return events.stream()
        .anyMatch(e -> e.getClass().equals(Complete.class));
  }

  abstract Optional<Integer> getExpectedNumberChunks();

  @SneakyThrows
  private void checkDepositWorkedOkay(String depositMessage, DepositEvents depositEvents) {
    Deposit deposit = mapper.readValue(depositMessage, Deposit.class);
    String bagId = deposit.getProperties().get("bagId");

    log.info("BROKER MSG COUNT {}", events.size());
    File[] destFiles = destDir.listFiles();

    int expectedNumFiles = getExpectedNumberChunks().isPresent() ? getExpectedNumberChunks().get() : 1;
    assertEquals(expectedNumFiles, destFiles.length);

    Arrays.sort(destFiles, Comparator.comparing(File::getName));

    final Map<Integer,File> chunkNumToEncChunk = new HashMap<>();

    final ComputedEncryption computedEncryption = depositEvents.getComputedEncryption();
    AESMode aesMode = AESMode.valueOf(computedEncryption.getAesMode());
    assertEquals(AESMode.GCM, aesMode);
    final ComputedDigest computedDigest = depositEvents.getComputedDigest();

    final File decryptedTarFile;

    if (getExpectedNumberChunks().isPresent()) {

      int expectedNumberChunks = getExpectedNumberChunks().get();

      for (int chunkNum = 1; chunkNum <= expectedNumberChunks; chunkNum++) {
        File expectedEncChunk = destDir.toPath().resolve(bagId + ".tar." + chunkNum).toFile();
        assertEquals(expectedEncChunk, destFiles[chunkNum - 1]);
        chunkNumToEncChunk.put(chunkNum, expectedEncChunk);
      }

      for (int chunkNum = 1; chunkNum <= expectedNumberChunks; chunkNum++) {
        File expectedEncChunk = chunkNumToEncChunk.get(chunkNum);
        String encHash = computedEncryption.getEncChunkDigests().get(chunkNum);
        assertEquals(encHash, getSha1Hash(expectedEncChunk));
      }

      Map<Integer, File> chunkNumToDecryptedChunk = new HashMap<>();
      for (int chunkNum = 1; chunkNum <= expectedNumberChunks; chunkNum++) {
        File expectedEncChunk = chunkNumToEncChunk.get(chunkNum);
        byte[] iv = computedEncryption.getChunkIVs().get(chunkNum);
        File decryptedChunkFile = Files.createTempFile("decryptedChunk", ".plain").toFile();
        chunkNumToDecryptedChunk.put(chunkNum, decryptedChunkFile);
        FileUtils.copyFile(expectedEncChunk, decryptedChunkFile);
        Encryption.decryptFile(aesMode, decryptedChunkFile, iv);
        assertTrue(decryptedChunkFile.length() > 0);
        assertTrue(decryptedChunkFile.length() != expectedEncChunk.length());
      }

      decryptedTarFile = Files.createTempFile("decryptedTar", ".plain").toFile();

      try (FileOutputStream fos = new FileOutputStream(decryptedTarFile)) {
        for (int chunkNum = 1; chunkNum <= expectedNumberChunks; chunkNum++) {
          File decryptedChunkFile = chunkNumToDecryptedChunk.get(chunkNum);
          Files.copy(decryptedChunkFile.toPath(), fos);
        }
      }

    } else {
      File expectedEncTar = destDir.toPath().resolve(bagId + ".tar").toFile();
      assertEquals(expectedEncTar, destFiles[0]);

      String encTarHash = computedEncryption.getEncTarDigest();
      assertEquals(encTarHash, getSha1Hash(expectedEncTar));

      decryptedTarFile = Files.createTempFile("decryptedTar", ".plain").toFile();

      byte[] iv = computedEncryption.getTarIV();
      FileUtils.copyFile(destFiles[0], decryptedTarFile);
      Encryption.decryptFile(aesMode, decryptedTarFile, iv);
      assertTrue(decryptedTarFile.length() > 0);
      assertTrue(decryptedTarFile.length() != expectedEncTar.length());
    }

    Set<Path> tarEntryPaths = getPathsWithinTarFile(decryptedTarFile);

    Path base = Paths.get(bagId);
    assertThat(tarEntryPaths).containsExactlyInAnyOrder(
        base.resolve("bagit.txt"),
        base.resolve("manifest-md5.txt"),
        base.resolve("tagmanifest-md5.txt"),

        base.resolve("data/src-path-1/src-file-1"),

        base.resolve("metadata/filetype.json"),
        base.resolve("metadata/vault.json"),
        base.resolve("metadata/external.txt"),
        base.resolve("metadata/deposit.json"));

    assertEquals(computedDigest.getDigest(), getSha1Hash(decryptedTarFile));
  }

  @SneakyThrows
  private void buildAndSendRetrieveMessage(DepositEvents depositEvents) {
    String retrieveMessage2 = depositEvents.generateRetrieveMessage(this.retrieveBaseDir, this.retrieveDir.getName());
    sendNormalMessage(retrieveMessage2);
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

  @SneakyThrows
  private String getSha1Hash(File file) {
    return Verify.getDigest(file);
  }
}