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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.broker.actuator.SftpFileStoreEndpoint;
import org.datavaultplatform.broker.actuator.SftpFileStoreInfo;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.broker.test.EmbeddedSftpServer;
import org.datavaultplatform.broker.test.SftpServerUtils;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.dao.FileStoreDAO;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.storage.impl.SFTPFileSystem;
import org.datavaultplatform.common.util.Constants;
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
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false"
})
@AutoConfigureMockMvc
public class FileStoreControllerIT extends BaseDatabaseTest {

  private static final String KEY_STORE_PASSWORD = "thePassword";
  private static final String KEY_NAME = "thekeyname";
  final ObjectMapper mapper = new ObjectMapper();
  final String CONTENTS_FILE_1 = UUID.randomUUID().toString();
  final String CONTENTS_FILE_2 = UUID.randomUUID().toString();
  final String CONTENTS_FILE_3 = UUID.randomUUID().toString();
  final String CONTENTS_FILE_4 = UUID.randomUUID().toString();
  final String ROOT_PATH = "tmp/sftp/root";
  final String PATH_DIR_1 = ROOT_PATH + "/AAA";
  final String PATH_FILE_1 = PATH_DIR_1 + "/file1.txt";
  final String PATH_FILE_2 = PATH_DIR_1 + "/file2.txt";
  final String PATH_DIR_2 = ROOT_PATH + "/BBB";
  final String PATH_FILE_3 = PATH_DIR_2 + "/file3.txt";
  final String PATH_FILE_4 = PATH_DIR_2 + "/file4.txt";
  @Value("${sftp.passphrase}")
  String passphrase;
  @MockBean
  Sender sender;
  @TempDir
  File temp;
  @Autowired
  MockMvc mvc;
  @Autowired
  FileStoreDAO fileStoreDAO;
  String keyStorePath;
  private Path tempSftpFolder;
  private EmbeddedSftpServer sftpServer;

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
    keyStorePath = temp.toPath().resolve("test.ks").toString();
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
    filestore.setStorageClass("org.datavaultplatform.common.storage.impl.SFTPFileSystem");
    HashMap<String, String> props = new HashMap<>();
    props.put("host", "localhost");
    props.put("port", "9999"); //we will replace 9999 later
    props.put("rootPath", "/tmp/sftp/root");
    filestore.setProperties(props);

    log.info("{}",mapper.writeValueAsString(filestore));

    MvcResult result = mvc.perform(post("/filestores/sftp")
            .with(req -> {
              req.setRemoteAddr("127.0.0.1");
              return req;
            })
            .header(Constants.HEADER_CLIENT_KEY, "datavault-webapp")
            .header(Constants.HEADER_USER_ID, "admin1")
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

    assertEquals("admin1", fsFromDb.getUser().getID());

    assertEquals("org.datavaultplatform.common.storage.impl.SFTPFileSystem",
        fsFromDb.getStorageClass());
    assertEquals("label-one", fsFromDb.getLabel());
    HashMap<String, String> storedProps = fsFromDb.getProperties();
    assertEquals(new HashSet<>(
        Arrays.asList("username", "password", "publicKey", "privateKey", "iv", "passphrase", "host",
            "port", "rootPath")), storedProps.keySet());
    assertEquals("admin1", storedProps.get("username"));
    assertEquals("", storedProps.get("password"));
    assertEquals("localhost", storedProps.get("host"));
    assertEquals("9999", storedProps.get("port"));
    assertEquals(passphrase, storedProps.get("passphrase"));
    assertTrue(storedProps.get("publicKey").startsWith("ssh-rsa"));
    assertTrue(isBase64(storedProps.get("privateKey")));
    assertTrue(isBase64(storedProps.get("iv")));
    log.info("properties {}", storedProps);

    UserStore us = UserStore.fromFileStore(fsFromDb);
    assertTrue(us instanceof SFTPFileSystem);
    SFTPFileSystem sftp = (SFTPFileSystem) us;
    String publicKey = storedProps.get("publicKey");

    sftpServer = setupSFTP(publicKey);
    int sftpServerPort = sftpServer.getServer().getPort();

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
  private void changeSFTPFileSystemPort(SFTPFileSystem sftp, int sftpServerPort) {
    // Tweak the SFTPFileSystem to change the port to point to embedded sftp server
    Field fPort = SFTPFileSystem.class.getDeclaredField("port");
    fPort.setAccessible(true);
    log.info("sftpServerPort {}", sftpServerPort);
    fPort.set(sftp, sftpServerPort);
  }

  @SneakyThrows
  private EmbeddedSftpServer setupSFTP(String publicKey) {
    this.tempSftpFolder = Files.createTempDirectory("SFTP_TEST");

    EmbeddedSftpServer sftpServer = SftpServerUtils.getSftpServer(publicKey, tempSftpFolder);

    copyToSFTPServer(tempSftpFolder.resolve(PATH_FILE_1), CONTENTS_FILE_1);
    copyToSFTPServer(tempSftpFolder.resolve(PATH_FILE_2), CONTENTS_FILE_2);
    copyToSFTPServer(tempSftpFolder.resolve(PATH_FILE_3), CONTENTS_FILE_3);
    copyToSFTPServer(tempSftpFolder.resolve(PATH_FILE_4), CONTENTS_FILE_4);

    return sftpServer;
  }

  void checkSFTP(SFTPFileSystem sftp) throws Exception {

    List<FileInfo> items = sftp.list(".").stream().sorted(Comparator.comparing(FileInfo::getName))
        .collect(
            Collectors.toList());

    FileInfo aaa = items.get(0);
    assertEquals("AAA", aaa.getName());
    assertEquals("./AAA", aaa.getKey());
    assertTrue(aaa.getIsDirectory());

    FileInfo bbb = items.get(1);
    assertEquals("BBB", bbb.getName());
    assertEquals("./BBB", bbb.getKey());
    assertTrue(bbb.getIsDirectory());

    //check files exist
    assertTrue(sftp.exists("AAA/file1.txt"));
    assertTrue(sftp.exists("AAA/file2.txt"));
    assertTrue(sftp.exists("BBB/file3.txt"));
    assertTrue(sftp.exists("BBB/file4.txt"));

    //check files do NOT exist
    assertFalse(sftp.exists("BBB/file1.txt"));
    assertFalse(sftp.exists("BBB/file2.txt"));
    assertFalse(sftp.exists("AAA/file3.txt"));
    assertFalse(sftp.exists("AAA/file4.txt"));

    //TEST READING FILES FROM SFTP
    sftp.retrieve("AAA/file1.txt", temp, new Progress());
    sftp.retrieve("AAA/file2.txt", temp, new Progress());
    sftp.retrieve("BBB/file3.txt", temp, new Progress());
    sftp.retrieve("BBB/file4.txt", temp, new Progress());
    checkFromSFTP(temp, "file1.txt", CONTENTS_FILE_1);
    checkFromSFTP(temp, "file2.txt", CONTENTS_FILE_2);
    checkFromSFTP(temp, "file3.txt", CONTENTS_FILE_3);
    checkFromSFTP(temp, "file4.txt", CONTENTS_FILE_4);

    //TEST WRITING FILE TO SFTP
    File tempFile = new File(this.temp, "writeTest.txt");
    String randomContents = UUID.randomUUID().toString();
    writeToFile(tempFile, randomContents);
    String storedPath = sftp.store("AAA", tempFile, new Progress());
    //this file should now be stored on sftp file system
    File onSftp = this.tempSftpFolder.resolve(storedPath.substring(1)).resolve("writeTest.txt")
        .toFile();
    String contentsfromSftp = readFromFile(onSftp);
    assertEquals(randomContents, contentsfromSftp);
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
    if (this.sftpServer != null) {
      sftpServer.stop();
    }
  }
}
