package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.datavaultplatform.common.config.BaseQueueConfig;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context.AESMode;
import org.datavaultplatform.worker.rabbit.BaseRabbitTCTest;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
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

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public abstract class BaseMultiPerformDepositThenRetrieveIT extends BaseRabbitTCTest {

  static final String KEY_NAME_FOR_SSH = "key-name-for-ssh";
  static final String KEY_NAME_FOR_DATA = "key-name-for-data";
  static final String KEY_STORE_PASSWORD = "testPassword";

  final Resource depositMessage = new ClassPathResource("sampleMessages/sampleMultiDepositMessage.json");

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
  File destDir1;
  File destDir2;
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
      while ((entry = tarIn.getNextEntry()) != null) {
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
    destDir1 = baseTemp.resolve("dest/1").toFile();
    assertTrue(destDir1.mkdirs());
    destDir2 = baseTemp.resolve("dest/2").toFile();
    assertTrue(destDir2.mkdirs());

    retrieveBaseDir = baseTemp.resolve("retrieve").toFile();
    assertTrue(retrieveBaseDir.mkdir());
    retrieveDir = retrieveBaseDir.toPath().resolve("ret-folder").toFile();
    assertTrue(retrieveDir.mkdir());
    log.info("meta.dir   [{}]", metaDir);
    log.info("temp.dir   [{}]", tempDir);
    log.info("source dir [{}]", sourceDir);
    log.info("dest1   dir [{}]", destDir1);
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
  void testDepositThenRetrieveUsingSecondLocation() {
    assertEquals(0, destDir1.listFiles().length);
    assertEquals(0, destDir2.listFiles().length);
    String depositMessage = getSampleDepositMessage();
    Deposit deposit = new ObjectMapper().readValue(depositMessage, Deposit.class);
    log.info("depositMessage {}", depositMessage);
    sendNormalMessage(depositMessage);
    waitUntil(this::foundComplete);

    DepositEvents depositEvents = new DepositEvents(deposit, this.events);

    checkDepositWorkedOkay(depositMessage, depositEvents, destDir1);
    checkDepositWorkedOkay(depositMessage, depositEvents, destDir2);

    // delete files from the first location, the retrieve should work using the second location !
    Arrays.stream(destDir1.listFiles()).forEach(File::delete);

    checkDepositEvents();

    buildAndSendRetrieveMessage(depositEvents);
    checkRetrieve();

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
  private void checkDepositWorkedOkay(String depositMessage, DepositEvents depositEvents, File destinationDir) {
    Deposit deposit = mapper.readValue(depositMessage, Deposit.class);
    String bagId = deposit.getProperties().get("bagId");

    log.info("BROKER MSG COUNT {}", events.size());
    File[] destFiles = destinationDir.listFiles();

    int expectedNumFiles = getExpectedNumberChunks().isPresent() ? getExpectedNumberChunks().get() : 1;
    assertEquals(expectedNumFiles, destFiles.length);

    Arrays.sort(destFiles, Comparator.comparing(File::getName));

    final Map<Integer, File> chunkNumToEncChunk = new HashMap<>();

    final ComputedEncryption computedEncryption = depositEvents.getComputedEncryption();
    AESMode aesMode = AESMode.valueOf(computedEncryption.getAesMode());
    assertEquals(AESMode.GCM, aesMode);
    final ComputedDigest computedDigest = depositEvents.getComputedDigest();

    final File decryptedTarFile;

    if (getExpectedNumberChunks().isPresent()) {

      int expectedNumberChunks = getExpectedNumberChunks().get();

      for (int chunkNum = 1; chunkNum <= expectedNumberChunks; chunkNum++) {
        File expectedEncChunk = destinationDir.toPath().resolve(bagId + ".tar." + chunkNum).toFile();
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
      File expectedEncTar = destinationDir.toPath().resolve(bagId + ".tar").toFile();
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
    String temp3 = temp2.replaceAll("/tmp/dv/dest/1", destDir1.getCanonicalPath());
    String temp4 = temp3.replaceAll("/tmp/dv/dest/2", destDir2.getCanonicalPath());
    return temp4;
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
    Class<? extends Event> eventClass = Class.forName(eventClassName).asSubclass(Event.class);
    return mapper.readValue(message, eventClass);
  }

  @SneakyThrows
  private String getSha1Hash(File file) {
    return Verify.getDigest(file);
  }

  public List<CompleteCopyUpload> getCopyUploadCompleteEvents(){
    return events.stream()
            .filter(e -> e.getClass().equals(CompleteCopyUpload.class))
            .map(CompleteCopyUpload.class::cast)
            .toList();
  }

  protected void checkDepositEvents() {
  }
}