package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.config.BaseQueueConfig;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context.AESMode;
import org.datavaultplatform.common.util.TestUtils;
import org.datavaultplatform.worker.rabbit.BaseRabbitIT;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.junit.jupiter.api.BeforeEach;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public abstract class BaseDepositIT extends BaseRabbitIT {

    static final String SRC_PATH_1 = "src-path-a";
    static final String SRC_PATH_2 = "src-path-b";
    static final String SRC_PATH_DEFAULT = SRC_PATH_1;
    static final String BAG_ID_1 = "d87ca007-9cee-4c49-8169-f74c2b90b773";
    static final String BAG_ID_2 = "bf73a7f5-42d1-4c3f-864a-a171af8373d4";

    static final String BAG_ID_DEFAULT = BAG_ID_1;
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
    boolean chunkingEnabled;

    @Value("${chunking.size:0}")
    String chunkingByteSize;

    @SneakyThrows
    static Set<Path> getPathsWithinTarFile(File tarFile) {
        Set<Path> paths = new HashSet<>();
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(Files.newInputStream(tarFile.toPath()))) {
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

        Files.createDirectories(tempDir.toPath());
        Files.createDirectories(metaDir.toPath());

        registry.add("tempDir", () -> tempDirValue);
        registry.add("metaDir", () -> metaDirValue);
    }

    @SuppressWarnings("UnusedReturnValue")
    final String sendNormalMessage(String msgBody) {
        MessageProperties props = new MessageProperties();
        props.setMessageId(UUID.randomUUID().toString());
        props.setPriority(NORMAL_PRIORITY);
        Message msg = new Message(msgBody.getBytes(StandardCharsets.UTF_8), props);
        template.send(workerQueue.getActualName(), msg);
        return props.getMessageId();
    }

    @BeforeEach
    @SneakyThrows
    final void setup() {
        checkChunkingProps(this.chunkingEnabled, this.chunkingByteSize);
        purgeQueues();
        setupKeystore();
        setupDirectoriesAndFiles();
        setupSourceDirectories();
        taskSpecificSetup();
    }

    final void setupSourceDirectories() throws Exception {
        for(String sourcePath : getSourcePaths()){
            setupSourceDirectory(sourcePath);
        }
    }

    List<String> getSourcePaths() {
        return Collections.singletonList(SRC_PATH_DEFAULT);
    }

    abstract void taskSpecificSetup() throws IOException;

    abstract void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize);

    @SneakyThrows
    final void setupDirectoriesAndFiles() {
        Path baseTemp = Paths.get(this.tempDir);
        assertThat(baseTemp).exists();
        assertThat(baseTemp).isDirectory();

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

    }

    final void setupSourceDirectory(String srcPath) throws Exception{
        File sourceFileDir = new File(sourceDir, srcPath);
        assertTrue(sourceFileDir.mkdir());
        assertTrue(sourceFileDir.exists() && sourceFileDir.isDirectory());

        File sourceFile = new File(sourceFileDir, "src-file-1");
        Files.copy(this.largeFile.getFile().toPath(), sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        assertTrue(sourceFile.exists() && sourceFile.isFile());
        assertEquals(50_000_000, sourceFile.length());
    }

    @SneakyThrows
    final void setupKeystore() {
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

    final void purgeQueues() {
        rabbitAdmin.purgeQueue(workerQueue.getActualName(), false);
        rabbitAdmin.purgeQueue(workerQueue.getActualName(), false);
        assertEquals(0, rabbitAdmin.getQueueInfo(workerQueue.getActualName()).getMessageCount());
        assertEquals(0, rabbitAdmin.getQueueInfo(workerQueue.getActualName()).getMessageCount());
    }


    final void waitUntil(Callable<Boolean> test) {
        TestUtils.waitUntil(
                Duration.ofMinutes(5),
                Duration.ofSeconds(15),
                test);
    }

    abstract Optional<Integer> getExpectedNumberChunksPerDeposit();

    final void checkDepositWorkedOkay(String depositMessage, DepositEvents depositEvents){
        checkDepositWorkedOkay(SRC_PATH_DEFAULT, depositMessage, depositEvents);
    }

    @SneakyThrows
    final void checkDepositWorkedOkay(String srcPath, String depositMessage, DepositEvents depositEvents) {
        Deposit deposit = mapper.readValue(depositMessage, Deposit.class);
        String bagId = deposit.getProperties().get("bagId");

        log.info("BROKER MSG COUNT {}", events.size());
        String[] depositFileNames = destDir.list((dir, name) -> dir.equals(destDir) && name.startsWith(bagId));
        File[] destFiles = Arrays.stream(depositFileNames).map(fn -> new File(destDir, fn)).sorted().toArray(File[]::new);

        int expectedNumDepositFiles = getExpectedNumberChunksPerDeposit().isPresent() ? getExpectedNumberChunksPerDeposit().get() : 1;
        assertEquals(expectedNumDepositFiles, depositFileNames.length);

        Arrays.sort(depositFileNames);

        final Map<Integer,File> chunkNumToEncChunk = new HashMap<>();

        final ComputedEncryption computedEncryption = depositEvents.getComputedEncryption();
        AESMode aesMode = AESMode.valueOf(computedEncryption.getAesMode());
        assertEquals(AESMode.GCM, aesMode);
        final ComputedDigest computedDigest = depositEvents.getComputedDigest();

        final File decryptedTarFile;

        if (getExpectedNumberChunksPerDeposit().isPresent()) {

            int expectedNumberChunks = getExpectedNumberChunksPerDeposit().get();

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


                base.resolve("data/"+srcPath+"/src-file-1"),

                base.resolve("metadata/filetype.json"),
                base.resolve("metadata/vault.json"),
                base.resolve("metadata/external.txt"),
                base.resolve("metadata/deposit.json"));

        assertEquals(computedDigest.getDigest(), getSha1Hash(decryptedTarFile));
    }

    final String getSampleDepositMessage() {
        return getSampleDepositMessage(SRC_PATH_DEFAULT, BAG_ID_DEFAULT);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @SneakyThrows
    final String getSampleDepositMessage(String srcPath, String bagId) {
        String temp1 = FileUtils.readFileToString(this.depositMessage.getFile(),
                StandardCharsets.UTF_8);
        String temp2 = temp1.replaceAll("/tmp/dv/src", sourceDir.getCanonicalPath());
        String temp3 =  temp2.replaceAll("/tmp/dv/dest", destDir.getCanonicalPath());
        String temp4 = temp3.replaceAll("src-path-1", srcPath);
        String temp5 = temp4.replaceAll("bf73a7f5-42d1-4c3f-864a-a171af8373d4", bagId);
        return temp5;
    }

    @RabbitListener(queues = BaseQueueConfig.BROKER_QUEUE_NAME)
    @SneakyThrows
    final void receiveBrokerMessage(Message message, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        channel.basicAck(deliveryTag, false);

        String msgBody = new String(message.getBody(), StandardCharsets.UTF_8);
        synchronized (events) {
            events.add(extractEvent(msgBody));

            log.info("Received message for broker [{}]", events.size());
        }
    }

    @SneakyThrows
    final Event extractEvent(String message) {
        Event event = mapper.readValue(message, Event.class);
        String eventClassName = event.getEventClass();
        Class<? extends Event> eventClass = Class.forName(eventClassName).asSubclass(Event.class);
        return mapper.readValue(message, eventClass);
    }

    @SneakyThrows
    final String getSha1Hash(File file) {
        return Verify.getDigest(file);
    }
    boolean foundComplete() {
        synchronized (events) {
            return events.stream()
                    .anyMatch(e -> e.getClass().equals(Complete.class));
        }
    }

}