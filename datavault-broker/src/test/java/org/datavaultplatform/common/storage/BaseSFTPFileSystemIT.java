package org.datavaultplatform.common.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.services.UserKeyPairService;
import org.datavaultplatform.broker.services.UserKeyPairService.KeyPairInfo;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.storage.impl.SFTPFileSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

/*
 A Base test class for testing SFTPFileSystem (implements SFTPFileSystemDriver)
 SFTP Server Authentication configuration left to the subclasses.
 */
@Slf4j
public abstract class BaseSFTPFileSystemIT {

  protected static final String ENV_USER_NAME = "USER_NAME";
  protected static final String TEST_USER = "testuser";

  public static final String FROM_DV_FILE_NAME = "fromDV.txt";
  public static final String TO_DV_FILE_NAME = "toDV.txt";
  public static final String DV_SFTP_TEMP_DIR_PREFIX = "dvSftpTempDir";

  protected GenericContainer<?> sftpServerContainer;

  private static final String TEST_FILE_CONTENTS = "hello Test File!";

  public static final Clock TEST_CLOCK = Clock.fixed(Instant.parse("2022-03-26T09:44:33.22Z"),
      ZoneId.of("Europe/London"));

  public static final long EXPECTED_SPACE_AVAILABLE_ON_SFTP_SERVER = 100_000;

  UserKeyPairService userKeyPairService;

  File tempFileDir;
  File fromDvFile;
  File toDvFile;

  public static final String SFTP_ROOT_DIR = "/config";

  protected SFTPFileSystemDriver sftpDriver;
  protected KeyPairInfo keyPairInfo;

  @BeforeEach
  @SneakyThrows
  void setup() {

    tempFileDir = Files.createTempDirectory(DV_SFTP_TEMP_DIR_PREFIX).toFile();
    fromDvFile = new File(tempFileDir, FROM_DV_FILE_NAME);
    toDvFile = new File(tempFileDir, TO_DV_FILE_NAME);

    writeToFile(fromDvFile, TEST_FILE_CONTENTS);

    authenticationSetup();

    this.sftpServerContainer = getSftpTestContainer();
    this.sftpServerContainer.start();
    sftpDriver = getSftpFileSystem();
  }


  @Test
  @SneakyThrows
  public void testSftpDriverSingleFileStoreAndRetrieve() {
    log.info("sftpDriver {}", sftpDriver);

    String pathOnRemote = sftpDriver.store(".", fromDvFile, new Progress());
    log.info("pathOnRemote[{}]", pathOnRemote);
    assertEquals("/config/dv_20220326094433", pathOnRemote);

    Path tsPath = Paths.get(SFTP_ROOT_DIR).relativize(Paths.get(pathOnRemote));
    String retrievePath = tsPath.resolve(fromDvFile.toPath().getFileName()).toString();
    log.info("retrievePath[{}]", retrievePath);
    sftpDriver.retrieve(retrievePath, toDvFile, new Progress());
    String contents = readFile(toDvFile);

    assertEquals(contents, TEST_FILE_CONTENTS);

    List<FileInfo> result1 = sftpDriver.list(tsPath.toString());
    result1.forEach(System.out::println);
    assertEquals(1, result1.size());
    FileInfo singleFile = result1.get(0);
    assertEquals("", singleFile.getAbsolutePath());
    assertEquals(false, singleFile.getIsDirectory());
    assertEquals(FROM_DV_FILE_NAME, singleFile.getName());
    assertEquals(tsPath.resolve(FROM_DV_FILE_NAME).toString(), singleFile.getKey());

    long fileSize = sftpDriver.getSize(singleFile.getKey());
    assertEquals(this.fromDvFile.length(), fileSize);

    assertTrue(sftpDriver.exists(singleFile.getKey()));
    assertFalse(sftpDriver.exists(singleFile.getKey() + ".txt"));

    assertTrue(sftpDriver.isDirectory(tsPath.toString()));
    assertFalse(sftpDriver.isDirectory(singleFile.getKey()));

    long actualSpaceAvailable = sftpDriver.getUsableSpace();
    assertTrue(actualSpaceAvailable > EXPECTED_SPACE_AVAILABLE_ON_SFTP_SERVER);

    assertEquals(FROM_DV_FILE_NAME, sftpDriver.getName(singleFile.getKey()));
    assertEquals(FROM_DV_FILE_NAME, sftpDriver.getName(FROM_DV_FILE_NAME));

    /* isValid always returns true :-( */
    assertTrue(sftpDriver.valid(singleFile.getKey()));
    assertTrue(sftpDriver.valid(singleFile.getKey() + ".txt"));


    assertThrows(NullPointerException.class, () -> sftpDriver.getName(null));
  }

  @SneakyThrows
  private String readFile(File file) {
    return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
  }
  @SneakyThrows
  private void writeToFile(File file, String contents) {
    Files.write(file.toPath(), contents.getBytes(StandardCharsets.UTF_8));
  }

  @AfterEach
  public void tearDown() {
    this.sftpServerContainer.stop();
    this.tempFileDir.deleteOnExit();
  }

  protected final SFTPFileSystem getSftpFileSystem() {
    Map<String, String> props = getStoreProperties();
    return new SFTPFileSystem("sftp-jsch", props, TEST_CLOCK);
  }

  @SneakyThrows
  private Map<String,String> getStoreProperties() {
    HashMap<String, String> props = new HashMap<>();

    //standard sftp properties
    props.put("username", TEST_USER);
    props.put("rootPath",
        "/config"); //this is the directory ON THE SFTP SERVER - for OpenSSH containers, it's config
    props.put("host", sftpServerContainer.getHost());
    props.put("port", String.valueOf(sftpServerContainer.getMappedPort(2222)));

    addAuthenticationProps(props);

    return props;
  }

  protected abstract GenericContainer<?> getSftpTestContainer();

  protected abstract void addAuthenticationProps(HashMap<String, String> props) throws Exception;

  protected void authenticationSetup() throws Exception {}
}
