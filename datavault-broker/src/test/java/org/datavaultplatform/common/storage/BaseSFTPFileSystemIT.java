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
import java.util.function.Function;
import java.util.stream.Collectors;
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

  static final String ENV_USER_NAME = "USER_NAME";
  static final String TEST_USER = "testuser";

  static final String FROM_DV_FILE_NAME = "fromDV.txt";
  static final String TO_DV_FILE_NAME = "toDV.txt";
  static final String DV_SFTP_TEMP_DIR_PREFIX = "dvSftpTempDir";
  static final String FROM_DV_DIR_NAME = "fromDir";
  static final String FROM_DV_DIR_FILE_A = "fromDVfileA.txt";
  static final String FROM_DV_DIR_FILE_B = "fromDVfileB.txt";
  static final String FROM_DV_DIR_FILE_C = "fromDVfileC.txt";
  static final String TO_DV_DIR_NAME = "toDir";
  static final String TEST_FILE_CONTENTS = "hello Test File!";
  static final String TEST_FILE_A_CONTENTS = "aaa-XXXX-AA";
  static final String TEST_FILE_B_CONTENTS = "bbb-XXXX-BBBB";
  static final String TEST_FILE_C_CONTENTS = "ccc-XXXX-CCCCCC";

  static final Clock TEST_CLOCK = Clock.fixed(Instant.parse("2022-03-26T09:44:33.22Z"),
      ZoneId.of("Europe/London"));
  static final long EXPECTED_SPACE_AVAILABLE_ON_SFTP_SERVER = 100_000;
  static final String SFTP_ROOT_DIR = "/config";
  GenericContainer<?> sftpServerContainer;
  UserKeyPairService userKeyPairService;
  File tempFileDir;
  File fromDvFile;
  File toDvFile;

  SFTPFileSystemDriver sftpDriver;
  KeyPairInfo keyPairInfo;
  File fromDvDir;
  File fromDvDirFileA;
  File fromDvDirFileB;
  File fromDvDirFileC;
  File toDvDirFileA;
  File toDvDirFileB;
  File toDvDirFileC;
  File toDvDir;

  @BeforeEach
  @SneakyThrows
  void setup() {

    tempFileDir = Files.createTempDirectory(DV_SFTP_TEMP_DIR_PREFIX).toFile();
    fromDvFile = new File(tempFileDir, FROM_DV_FILE_NAME);
    toDvFile = new File(tempFileDir, TO_DV_FILE_NAME);

    fromDvDir = new File(tempFileDir, FROM_DV_DIR_NAME);
    fromDvDir.mkdirs();

    toDvDir = new File(tempFileDir, TO_DV_DIR_NAME);
    toDvDir.mkdirs();

    fromDvDirFileA = new File(fromDvDir, FROM_DV_DIR_FILE_A);
    fromDvDirFileB = new File(fromDvDir, FROM_DV_DIR_FILE_B);
    fromDvDirFileC = new File(fromDvDir, FROM_DV_DIR_FILE_C);

    toDvDirFileA = new File(toDvDir, FROM_DV_DIR_FILE_A);
    toDvDirFileB = new File(toDvDir, FROM_DV_DIR_FILE_B);
    toDvDirFileC = new File(toDvDir, FROM_DV_DIR_FILE_C);

    writeToFile(fromDvFile, TEST_FILE_CONTENTS);

    writeToFile(fromDvDirFileA, TEST_FILE_A_CONTENTS);
    writeToFile(fromDvDirFileB, TEST_FILE_B_CONTENTS);
    writeToFile(fromDvDirFileC, TEST_FILE_C_CONTENTS);

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
    //assertEquals(0, p1.dirCount);

    //assertEquals(fromDvFile.length(), p1.byteCount);
    //assertEquals(TEST_CLOCK.millis(), p1.timestamp);

    //assertEquals(0, p1.dirCount);

    //TODO - seems like the  p1.fileCount should be 1.
    //assertEquals(0, p1.fileCount);

    //TODO - seems like the p1.startTime has not been set
    //assertEquals(0, p1.startTime);

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

  @Test
  @SneakyThrows
  public void testSftpDriverSingleDirectoryStoreAndRetrieve() {
    log.info("sftpDriver {}", sftpDriver);

    String pathOnRemote = sftpDriver.store(".", fromDvDir, new Progress());
    //assertEquals(fromDvDirFileA.length() + fromDvDirFileB.length() + fromDvDirFileC.length(), p1.byteCount);
    //assertEquals(TEST_CLOCK.millis(), p1.timestamp);

    //TODO - seems like the  p1.dirCount should be 1.
    //assertEquals(0, p1.dirCount);

    //TODO - seems like the  p1.fileCount should be 3.
    //assertEquals(0, p1.fileCount);

    //TODO - seems like the p1.startTime has not been set
    //assertEquals(0, p1.startTime);


    Path tsPath = Paths.get(SFTP_ROOT_DIR).relativize(Paths.get(pathOnRemote));
    String retrievePath = tsPath.resolve(fromDvDir.toPath().getFileName()).toString();
    log.info("retrievePath[{}]", retrievePath);

    // check files are on SFTP server
    Map<String, FileInfo> fileMap = sftpDriver.list(retrievePath).stream().collect(
        Collectors.toMap(FileInfo::getName, Function.identity()));

    assertEquals(3, fileMap.size());

    FileInfo fileInfoA = fileMap.get(FROM_DV_DIR_FILE_A);
    assertEquals(FROM_DV_DIR_FILE_A, fileInfoA.getName());
    assertEquals(Paths.get(retrievePath).resolve(FROM_DV_DIR_FILE_A).toString(),
        fileInfoA.getKey());
    assertEquals(false, fileInfoA.getIsDirectory());
    assertEquals("", fileInfoA.getAbsolutePath());

    FileInfo fileInfoB = fileMap.get(FROM_DV_DIR_FILE_B);
    assertEquals(FROM_DV_DIR_FILE_B, fileInfoB.getName());
    assertEquals(Paths.get(retrievePath).resolve(FROM_DV_DIR_FILE_B).toString(),
        fileInfoB.getKey());
    assertEquals(false, fileInfoB.getIsDirectory());
    assertEquals("", fileInfoB.getAbsolutePath());

    FileInfo fileInfoC = fileMap.get(FROM_DV_DIR_FILE_C);
    assertEquals(FROM_DV_DIR_FILE_C, fileInfoC.getName());
    assertEquals(Paths.get(retrievePath).resolve(FROM_DV_DIR_FILE_C).toString(),
        fileInfoC.getKey());
    assertEquals(false, fileInfoC.getIsDirectory());
    assertEquals("", fileInfoC.getAbsolutePath());

    assertEquals(0, Files.list(toDvDir.toPath()).count());
    sftpDriver.retrieve(retrievePath, toDvDir, new Progress());

    // We can check we have got 3 files back from SFTP Server
    assertEquals(3, Files.list(toDvDir.toPath()).count());

    assertEquals(TEST_FILE_A_CONTENTS, readFile(toDvDirFileA));
    assertEquals(TEST_FILE_B_CONTENTS, readFile(toDvDirFileB));
    assertEquals(TEST_FILE_C_CONTENTS, readFile(toDvDirFileC));

    long fileSizeA = sftpDriver.getSize(fileInfoA.getKey());
    assertEquals(this.fromDvDirFileA.length(), fileSizeA);
    assertEquals(FROM_DV_DIR_FILE_A, sftpDriver.getName(fileInfoA.getKey()));

    long fileSizeB = sftpDriver.getSize(fileInfoB.getKey());
    assertEquals(this.fromDvDirFileB.length(), fileSizeB);
    assertEquals(FROM_DV_DIR_FILE_B, sftpDriver.getName(fileInfoB.getKey()));

    long fileSizeC = sftpDriver.getSize(fileInfoC.getKey());
    assertEquals(this.fromDvDirFileC.length(), fileSizeC);
    assertEquals(FROM_DV_DIR_FILE_C, sftpDriver.getName(fileInfoC.getKey()));
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

  protected SFTPFileSystemDriver getSftpFileSystem() {
    Map<String, String> props = getStoreProperties();
    return new SFTPFileSystem("sftp-jsch", props, TEST_CLOCK);
  }

  @SneakyThrows
  protected Map<String,String> getStoreProperties() {
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

  abstract GenericContainer<?> getSftpTestContainer();

  abstract void addAuthenticationProps(HashMap<String, String> props) throws Exception;

  void authenticationSetup() throws Exception {}
}
