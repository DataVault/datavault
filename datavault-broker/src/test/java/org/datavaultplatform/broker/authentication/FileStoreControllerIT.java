package org.datavaultplatform.broker.authentication;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.broker.actuator.SftpFileStoreEndpoint;
import org.datavaultplatform.broker.actuator.SftpFileStoreInfo;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.dao.FileStoreDAO;
import org.datavaultplatform.common.storage.StorageConstants;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.storage.impl.SFTPFileSystem;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemJSch;
import org.datavaultplatform.common.util.Constants;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=true",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false"
})
@AutoConfigureMockMvc
public class FileStoreControllerIT extends BaseDatabaseTest {

  private static final String TEST_USER = "admin1";
  private static final String ENV_USER_NAME = "USER_NAME";
  private static final String ENV_PUBLIC_KEY = "PUBLIC_KEY";
  private static final String KEY_STORE_PASSWORD = "thePassword";
  private static final String KEY_NAME = "thekeyname";

  private static final String BASE_SFTP_DIR = "/tmp/sftp/root";

  final ObjectMapper mapper = new ObjectMapper();
  final String CONTENTS_FILE_1 = UUID.randomUUID().toString();
  final String CONTENTS_FILE_2 = UUID.randomUUID().toString();
  final String CONTENTS_FILE_3 = UUID.randomUUID().toString();
  final String CONTENTS_FILE_4 = UUID.randomUUID().toString();
  final String PATH_DIR_1 = "FILES/AAA";
  final String PATH_FILE_1 = PATH_DIR_1 + "/file1.txt";
  final String PATH_FILE_2 = PATH_DIR_1 + "/file2.txt";
  final String PATH_DIR_2 = "FILES/BBB";
  final String PATH_FILE_3 = PATH_DIR_2 + "/file3.txt";
  final String PATH_FILE_4 = PATH_DIR_2 + "/file4.txt";
  @Value("${sftp.passphrase}")
  String passphrase;
  @MockBean
  Sender sender;
  @TempDir
  File tempFromSftp;
  @Autowired
  MockMvc mvc;
  @Autowired
  FileStoreDAO fileStoreDAO;
  String keyStorePath;
  private Path tempSftpFolder;
  private GenericContainer<?> sftpServer;

  private final StorageClassNameResolver resolver = new StorageClassNameResolver(true);

  @MockBean
  Function<String,String> mPortAdjuster;

  @Autowired
  SftpFileStoreEndpoint sftpFileStoreEndpoint;

  /*
    Creates and configures a Java Key Store specifically for this test run.
   */
  @BeforeEach
  @SneakyThrows
  void setup() {
    keyStorePath = tempFromSftp.toPath().resolve("test.ks").toString();
    log.info("TEMP KEY IS AT [{}]", keyStorePath);
    Encryption enc = new Encryption();
    enc.setVaultEnable(false);
    enc.setVaultPrivateKeyEncryptionKeyName(KEY_NAME);

    enc.setKeystoreEnable(true);
    enc.setKeystorePath(keyStorePath);
    enc.setKeystorePassword(KEY_STORE_PASSWORD);

    SecretKey keyForKeyStore = Encryption.generateSecretKey();

    assertFalse(new File(this.keyStorePath).exists());

    // Encryption class uses 'vaultPrivateKeyEncryptionKeyName' property as the default key name for JavaKeyStore
    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultPrivateKeyEncryptionKeyName(),
        keyForKeyStore);
    assertTrue(new File(this.keyStorePath).exists());

  }

  @Test
  void testPostAddSftpFileStore() throws Exception {

    assertEquals(0, fileStoreDAO.count());

    assertTrue(Encryption.isInitialised());
    FileStore filestore = new FileStore();
    filestore.setLabel("label-one");
    filestore.setStorageClass(StorageConstants.SFTP_FILE_SYSTEM);
    HashMap<String, String> props = new HashMap<>();
    props.put(PropNames.HOST, "localhost");
    props.put(PropNames.PORT, "9999"); //we will replace 9999 later
    props.put(PropNames.ROOT_PATH, BASE_SFTP_DIR);
    filestore.setProperties(props);

    log.info("{}",mapper.writeValueAsString(filestore));

    //posts to org.datavaultplatform.broker.controllers.FileStoreController.getFileStoresSFTP

    MvcResult result = mvc.perform(post("/filestores/sftp")
            .with(req -> {
              req.setRemoteAddr("127.0.0.1");
              return req;
            })
            .header(Constants.HEADER_CLIENT_KEY, "datavault-webapp")
            .header(Constants.HEADER_USER_ID,   TEST_USER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(filestore)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andReturn();

    String jsonResponse = result.getResponse().getContentAsString();
    ObjectMapper mapper = new ObjectMapper();
    FileStore fsResponse = mapper.readValue(jsonResponse, FileStore.class);
    String fileStoreId = fsResponse.getID();
    assertEquals(1, fileStoreDAO.count());

    FileStore fsFromDb = fileStoreDAO.findById(fileStoreId).get();
    checkFileStores(fsResponse, fsFromDb,
        "port", "host", "rootPath", "publicKey", "iv", "username");
    assertEquals(fileStoreId, fsFromDb.getID());

    assertEquals(TEST_USER, fsFromDb.getUser().getID());

    assertEquals(StorageConstants.SFTP_FILE_SYSTEM,
        fsFromDb.getStorageClass());
    assertEquals("label-one", fsFromDb.getLabel());
    HashMap<String, String> storedProps = fsFromDb.getProperties();
    assertEquals(new HashSet<>(
        Arrays.asList("username", "password", "publicKey", "privateKey", "iv", "passphrase", "host",
            "port", "rootPath")), storedProps.keySet());
    assertEquals(TEST_USER, storedProps.get("username"));
    assertEquals("", storedProps.get("password"));
    assertEquals("localhost", storedProps.get("host"));
    assertEquals("9999", storedProps.get("port"));
    assertEquals(passphrase, storedProps.get("passphrase"));
    assertTrue(storedProps.get("publicKey").startsWith("ssh-rsa"));
    assertTrue(isBase64(storedProps.get("privateKey")));
    assertTrue(isBase64(storedProps.get("iv")));
    log.info("properties {}", storedProps);

    UserStore us = UserStore.fromFileStore(fsFromDb, resolver);
    assertTrue(us instanceof SFTPFileSystem);
    SFTPFileSystem sftp = (SFTPFileSystem) us;
    String publicKey = storedProps.get("publicKey");
    log.info("public key [{}]", publicKey);

    this.sftpServer = setupAndStartSFTP(publicKey);
    int sftpServerPort = this.sftpServer.getMappedPort(2222);

    changeSFTPFileSystemPort(sftp, sftpServerPort);

    checkSFTP(sftp);

    checkSftpFileStoreEndpoint(fileStoreId, sftpServerPort);
  }

  @SneakyThrows
  private void checkSftpFileStoreEndpoint(String fileStoreId, int actualPort) {

    Mockito.when(mPortAdjuster.apply("9999")).thenReturn(String.valueOf(actualPort));

    List<SftpFileStoreInfo> items = this.sftpFileStoreEndpoint.getSftpFileStoresInfo();
    SftpFileStoreInfo info = items.stream().filter(i -> i.getId().equals(fileStoreId)).findFirst()
        .get();
    assertTrue(info.isCanConnect());
  }

  void checkFileStores(FileStore fsResponse, FileStore fsFromDb, String... props) {
    assertEquals(fsResponse.getID(), fsFromDb.getID());
    assertEquals(fsResponse.getLabel(), fsFromDb.getLabel());
    assertEquals(fsResponse.getStorageClass(), fsFromDb.getStorageClass());
    Map<String, String> resProps = fsResponse.getProperties();
    Map<String, String> dbProps = fsFromDb.getProperties();
    for (String prop : props) {
      assertEquals(resProps.get(prop), dbProps.get(prop));
    }
    assertEquals(fsResponse.getProperties().keySet(), new HashSet<>(Arrays.asList(props)));
  }

  @SneakyThrows
  private void changeSFTPFileSystemPort(SFTPFileSystemJSch sftp, int sftpServerPort) {
    // Tweak the SFTPFileSystem to change the port to point to embedded sftp server
    Field fPort = SFTPFileSystemJSch.class.getDeclaredField("port");
    fPort.setAccessible(true);
    log.info("sftpServerPort {}", sftpServerPort);
    fPort.set(sftp, sftpServerPort);
  }

  @SneakyThrows
  private GenericContainer<?> setupAndStartSFTP(String publicKey) {

    this.tempSftpFolder = Files.createTempDirectory("SFTP_TEST");
    copyToSFTPServer(tempSftpFolder.resolve(PATH_FILE_1), CONTENTS_FILE_1);
    copyToSFTPServer(tempSftpFolder.resolve(PATH_FILE_2), CONTENTS_FILE_2);
    copyToSFTPServer(tempSftpFolder.resolve(PATH_FILE_3), CONTENTS_FILE_3);
    copyToSFTPServer(tempSftpFolder.resolve(PATH_FILE_4), CONTENTS_FILE_4);

    GenericContainer<?> sftpServer = new GenericContainer<>(DockerImageName.parse(DockerImage.OPEN_SSH_8pt6_IMAGE_NAME))
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PUBLIC_KEY, publicKey) //this causes the public key to be added to /config/.ssh/authorized_keys
        .withExposedPorts(2222)
        .withFileSystemBind(tempSftpFolder.toString(), BASE_SFTP_DIR)
        .waitingFor(Wait.forListeningPort());

    sftpServer.start();

    return sftpServer;
  }

  void checkSFTP(SFTPFileSystem sftp) throws Exception {

    List<FileInfo> items = sftp.list("FILES").stream().sorted(Comparator.comparing(FileInfo::getName))
        .collect(
            Collectors.toList());

    FileInfo aaa = items.get(0);
    assertEquals("AAA", aaa.getName());
    assertEquals("FILES/AAA", aaa.getKey());
    assertTrue(aaa.getIsDirectory());

    FileInfo bbb = items.get(1);
    assertEquals("BBB", bbb.getName());
    assertEquals("FILES/BBB", bbb.getKey());
    assertTrue(bbb.getIsDirectory());

    //check files exist
    assertTrue(sftp.exists("FILES/AAA/file1.txt"));
    assertTrue(sftp.exists("FILES/AAA/file2.txt"));
    assertTrue(sftp.exists("FILES/BBB/file3.txt"));
    assertTrue(sftp.exists("FILES/BBB/file4.txt"));

    //check files do NOT exist
    assertFalse(sftp.exists("FILES/BBB/file1.txt"));
    assertFalse(sftp.exists("FILES/BBB/file2.txt"));
    assertFalse(sftp.exists("FILES/AAA/file3.txt"));
    assertFalse(sftp.exists("FILES/AAA/file4.txt"));

    //TEST READING FILES FROM SFTP
    sftp.retrieve("FILES/AAA/file1.txt", tempFromSftp, new Progress());
    sftp.retrieve("FILES/AAA/file2.txt", tempFromSftp, new Progress());
    sftp.retrieve("FILES/BBB/file3.txt", tempFromSftp, new Progress());
    sftp.retrieve("FILES/BBB/file4.txt", tempFromSftp, new Progress());

    checkFromSFTP(tempFromSftp, "file1.txt", CONTENTS_FILE_1);
    checkFromSFTP(tempFromSftp, "file2.txt", CONTENTS_FILE_2);
    checkFromSFTP(tempFromSftp, "file3.txt", CONTENTS_FILE_3);
    checkFromSFTP(tempFromSftp, "file4.txt", CONTENTS_FILE_4);

    //TEST WRITING FILE TO SFTP
    File tempFile = new File(this.tempFromSftp, "writeTest.txt");
    String randomContents = UUID.randomUUID().toString();
    writeToFile(tempFile, randomContents);
    String storedPath = sftp.store("FILES/AAA", tempFile, new Progress());
    //this file should now be stored on sftp file system

    Path relStoredPath = Paths.get(BASE_SFTP_DIR).relativize(Paths.get(storedPath));
    File onSftp = this.tempSftpFolder.resolve(relStoredPath).resolve("writeTest.txt").toFile();

    assertTrue(onSftp.exists());
    assertTrue(onSftp.canRead());
    assertEquals(randomContents, readFromFile(onSftp));
  }

  @SneakyThrows
  private void checkFromSFTP(File temp, String filename, String expectedContents) {
    String actualContents = readFromFile(new File(temp, filename));
    assertEquals(expectedContents, actualContents);
  }

  @SneakyThrows
  private String readFromFile(File file) {
    return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
  }

  @SneakyThrows
  private void writeToFile(File file, String contents) {
    try (FileWriter fw = new FileWriter(file)) {
      fw.write(contents);
    }
  }

  @SneakyThrows
  private void copyToSFTPServer(Path filePath, String contents) {
    Path parent = filePath.getParent();
    Files.createDirectories(parent);
    File parentDir = parent.toFile();
    log.info("parent [{}]", parent);
    log.info("parentDir [{}]", parentDir);
    assertTrue(parentDir.exists() && parentDir.isDirectory());

    try (FileWriter fw = new FileWriter(filePath.toFile())) {
      fw.write(contents);
    }
  }

  public boolean isBase64(String path) {
    try {
      Base64.getDecoder().decode(path);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @AfterEach
  void tearDown() {
    if(this.sftpServer != null){
      this.sftpServer.stop();
    }
  }
}
