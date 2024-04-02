package org.datavaultplatform.broker.authentication;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
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
import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.StorageConstants;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;
import org.datavaultplatform.common.util.Constants;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import org.testcontainers.utility.MountableFile;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=true",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false",
    "sftp.driver.use.apache.sshd=true"
})
@AutoConfigureMockMvc
public class FileStoreControllerIT extends BaseDatabaseTest {

  private static final String TEST_USER = "admin1";
  private static final String ENV_USER_NAME = "USER_NAME";
  private static final String ENV_PUBLIC_KEY = "PUBLIC_KEY";
  private static final String KEY_STORE_PASSWORD = "thePassword";
  private static final String KEY_NAME = "thekeyname";

  private static final String BASE_SFTP_DIR = "/tmp/sftp/root";
  public static final int SFTP_SERVER_PORT = 2222;
  public static final String TEMP_SFTP_SERVER_PORT = "9999";
  public static final String LOCALHOST = "localhost";

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
  @Autowired
  MockMvc mvc;
  @Autowired
  FileStoreDAO fileStoreDAO;
  String keyStorePath;
  private Path tempCopyToSftpFolder;
  @TempDir
  File tempRetrievedFromSftp;
  @TempDir
  File tempStoreToSftp;

  private GenericContainer<?> sftpServer;

  private final StorageClassNameResolver resolver = new StorageClassNameResolver(true);


  @Autowired
  SftpFileStoreEndpoint sftpFileStoreEndpoint;

  /*
    Creates and configures a Java Key Store specifically for this test run.
   */
  @BeforeEach
  @SneakyThrows
  void setup() {
    keyStorePath = tempRetrievedFromSftp.toPath().resolve("test.ks").toString();
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
    props.put(PropNames.HOST, LOCALHOST);
    props.put(PropNames.PORT, TEMP_SFTP_SERVER_PORT); //we will replace TEMP_SFTP_PORT/9999 later
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
        PropNames.PORT, PropNames.HOST, PropNames.ROOT_PATH,
        PropNames.PUBLIC_KEY, PropNames.IV, PropNames.USERNAME);
    assertEquals(fileStoreId, fsFromDb.getID());

    assertEquals(TEST_USER, fsFromDb.getUser().getID());

    assertEquals(StorageConstants.SFTP_FILE_SYSTEM,
        fsFromDb.getStorageClass());
    assertEquals("label-one", fsFromDb.getLabel());
    HashMap<String, String> storedProps = fsFromDb.getProperties();
    assertEquals(new HashSet<>(
        List.of(PropNames.USERNAME, PropNames.PASSWORD, PropNames.PUBLIC_KEY,
            PropNames.PRIVATE_KEY, PropNames.IV, PropNames.PASSPHRASE, PropNames.HOST,
            PropNames.PORT, PropNames.ROOT_PATH)), storedProps.keySet());
    assertEquals(TEST_USER, storedProps.get(PropNames.USERNAME));
    assertEquals("", storedProps.get(PropNames.PASSWORD));
    assertEquals(LOCALHOST, storedProps.get(PropNames.HOST));
    assertEquals(TEMP_SFTP_SERVER_PORT, storedProps.get(PropNames.PORT));
    assertEquals(passphrase, storedProps.get(PropNames.PASSPHRASE));
    assertTrue(storedProps.get(PropNames.PUBLIC_KEY).startsWith("ssh-rsa"));
    assertTrue(isBase64(storedProps.get(PropNames.PRIVATE_KEY)));
    assertTrue(isBase64(storedProps.get(PropNames.IV)));
    log.info("properties {}", storedProps);

    String publicKey = fsFromDb.getProperties().get(PropNames.PUBLIC_KEY);
    //by saving a new FileStore - we generate the private and public key - we can start sftp server
    this.sftpServer = setupAndStartSFTP(publicKey);

    // now we have running sftp server with correct authenticationsetup, we can update FileStore with host and port
    int sftpServerPort = this.sftpServer.getMappedPort(SFTP_SERVER_PORT);
    String sftpServerHost = this.sftpServer.getHost();

    //now we have generated the public/private key
    fsFromDb.getProperties().put(PropNames.PORT, String.valueOf(sftpServerPort));
    fsFromDb.getProperties().put(PropNames.HOST, sftpServerHost);
    this.fileStoreDAO.update(fsFromDb);

    UserStore us = UserStore.fromFileStore(fsFromDb, resolver);
    assertInstanceOf(SFTPFileSystemSSHD.class, us);
    SFTPFileSystemSSHD sftp = (SFTPFileSystemSSHD) us;

    log.info("public key [{}]", publicKey);

    checkSFTP(sftp);

    checkSftpFileStoreEndpoint(fileStoreId, sftpServerHost, sftpServerPort);
  }

  @SneakyThrows
  private void checkSftpFileStoreEndpoint(String fileStoreId, String expectedHost, int expectedPort) {

    List<SftpFileStoreInfo> items = this.sftpFileStoreEndpoint.getSftpFileStoresInfo();
    SftpFileStoreInfo info = items.stream().filter(i -> i.getId().equals(fileStoreId)).findFirst()
        .get();
    assertThat(info.getHost()).isEqualTo(expectedHost);
    assertThat(info.getPort()).isEqualTo(String.valueOf(expectedPort));
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
    assertEquals(fsResponse.getProperties().keySet(), Set.of(props));
  }

  @SneakyThrows
  private GenericContainer<?> setupAndStartSFTP(String publicKey) {

    this.tempCopyToSftpFolder = Files.createTempDirectory("SFTP_TEST");
    copyToSFTPServer(tempCopyToSftpFolder.resolve(PATH_FILE_1), CONTENTS_FILE_1);
    copyToSFTPServer(tempCopyToSftpFolder.resolve(PATH_FILE_2), CONTENTS_FILE_2);
    copyToSFTPServer(tempCopyToSftpFolder.resolve(PATH_FILE_3), CONTENTS_FILE_3);
    copyToSFTPServer(tempCopyToSftpFolder.resolve(PATH_FILE_4), CONTENTS_FILE_4);

    var uid = String.valueOf(new com.sun.security.auth.module.UnixSystem().getUid());
    var gid = String.valueOf(new com.sun.security.auth.module.UnixSystem().getGid());

    GenericContainer<?> sftpServer = new GenericContainer<>(DockerImageName.parse(DockerImage.OPEN_SSH_8pt6_IMAGE_NAME))
        .withEnv("PUID", uid)
        .withEnv("PGID", gid)
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PUBLIC_KEY, publicKey) //this causes the public key to be added to /config/.ssh/authorized_keys
        .withExposedPorts(SFTP_SERVER_PORT)
        .withCopyToContainer(MountableFile.forHostPath(tempCopyToSftpFolder), BASE_SFTP_DIR)
        .waitingFor(Wait.forListeningPort());

    sftpServer.start();

    return sftpServer;
  }

  void checkSFTP(SFTPFileSystemDriver sftp) throws Exception {

    List<FileInfo> items = sftp.list("FILES").stream().sorted(Comparator.comparing(FileInfo::getName))
        .toList();

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
    sftp.retrieve("FILES/AAA/file1.txt", tempRetrievedFromSftp, new Progress());
    sftp.retrieve("FILES/AAA/file2.txt", tempRetrievedFromSftp, new Progress());
    sftp.retrieve("FILES/BBB/file3.txt", tempRetrievedFromSftp, new Progress());
    sftp.retrieve("FILES/BBB/file4.txt", tempRetrievedFromSftp, new Progress());

    checkFromSFTP(tempRetrievedFromSftp, "file1.txt", CONTENTS_FILE_1);
    checkFromSFTP(tempRetrievedFromSftp, "file2.txt", CONTENTS_FILE_2);
    checkFromSFTP(tempRetrievedFromSftp, "file3.txt", CONTENTS_FILE_3);
    checkFromSFTP(tempRetrievedFromSftp, "file4.txt", CONTENTS_FILE_4);

    //TEST WRITING FILE TO SFTP
    File tempFile = new File(this.tempStoreToSftp, "writeTest.txt");
    String randomContents = UUID.randomUUID().toString();
    writeToFile(tempFile, randomContents);
    String storedPath = sftp.store("FILES/AAA", tempFile, new Progress());
    //this file should now be stored on sftp file system

    File retrievedFromSftp = new File(this.tempRetrievedFromSftp, "writeTest.txt");
    String tsDirName = Paths.get(storedPath).getFileName().toString();
    Path retrievePath = Path.of("FILES","AAA",tsDirName, "writeTest.txt");
    sftp.retrieve(retrievePath.toString(), retrievedFromSftp, new Progress());

    assertTrue(retrievedFromSftp.exists());
    assertTrue(retrievedFromSftp.canRead());
    assertEquals(randomContents, readFromFile(retrievedFromSftp));
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
