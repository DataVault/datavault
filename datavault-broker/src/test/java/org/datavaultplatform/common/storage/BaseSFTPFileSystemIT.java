package org.datavaultplatform.common.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.broker.services.UserKeyPairService;
import org.datavaultplatform.broker.services.UserKeyPairService.KeyPairInfo;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.io.ProgressEvent;
import org.datavaultplatform.common.io.ProgressEventListener;
import org.datavaultplatform.common.io.ProgressEventType;
import org.datavaultplatform.common.model.FileInfo;

import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;
import org.datavaultplatform.common.storage.impl.ssh.UtilitySSHD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;

/*
 A Base test class for testing classes that implement SFTPFileSystemDriver
 SFTP Server Authentication configuration left to the subclasses.
 */
@Slf4j
@TestPropertySource(properties = "logging.level.org.apache.sshd=INFO")
public abstract class BaseSFTPFileSystemIT {

  static final String ENV_USER_NAME = "USER_NAME";
  static final String TEST_USER = "testuser";

  static final String SFTP_ROOT_DIR = "/config";
  static final Clock TEST_CLOCK = Clock.fixed(Instant.parse("2022-03-26T09:44:33.22Z"),
      ZoneId.of("Europe/London"));
  static final String TEMP_PREFIX = "dvSftpTempDir";
  static final long EXPECTED_SPACE_AVAILABLE_ON_SFTP_SERVER = 100_000;
  private static final int FREE_SPACE_FACTOR = 10;
  GenericContainer<?> sftpServerContainer;
  UserKeyPairService userKeyPairService;
  SFTPFileSystemDriver sftpDriver;
  KeyPairInfo keyPairInfo;

  @BeforeEach
  @SneakyThrows
  void setup() {
    authenticationSetup();
    this.sftpServerContainer = getSftpTestContainer();
    this.sftpServerContainer.start();
    sftpDriver = getSftpFileSystemDriver();
  }

  @Test
  @SneakyThrows
  public void testSftpDriverSingleFileStoreAndRetrieveToFile() {
    final String TEST_FILE_CONTENTS = "hello Test File!";
    final String FROM_DV_FILE_NAME = "fromDV.txt";
    final String TO_DV_FILE_NAME = "toDV.txt";

    File tempFileDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
    tempFileDir.deleteOnExit();
    File fromDvFile = new File(tempFileDir, FROM_DV_FILE_NAME);
    File toDvFile = new File(tempFileDir, TO_DV_FILE_NAME);
    writeToFile(fromDvFile, TEST_FILE_CONTENTS);

    log.info("sftpDriver {}", sftpDriver);

    Progress p1 = new Progress();
    String pathOnRemote = sftpDriver.store(".", fromDvFile, p1);

    assertEquals(fromDvFile.length(), p1.getByteCount());
    assertEquals(TEST_CLOCK.millis(), p1.getTimestamp());

    assertEquals(0, p1.getDirCount());

    // TODO - seems like the  p1.fileCount should be 1.
    assertEquals(0, p1.getFileCount());

    // TODO - seems like the p1.startTime has not been set
    assertEquals(0, p1.getStartTime());

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
    assertEquals(fromDvFile.length(), fileSize);

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

  @ParameterizedTest
  @ValueSource(strings = {"", "a single line", "two\nlines"})
  @SneakyThrows
  public void testSftpDriverSingleFileStoreAndRetrieveToDirectory(String fileContents) {
    final String FROM_DV_FILE_NAME = "fromDV.txt";

    File tempFileDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
    tempFileDir.deleteOnExit();

    File fromDvFile = new File(tempFileDir, FROM_DV_FILE_NAME);
    writeToFile(fromDvFile, fileContents);

    File toDirectory = new File(tempFileDir, "toDirectory");
    assertTrue(toDirectory.mkdir());

    log.info("sftpDriver {}", sftpDriver);

    Progress p1 = new Progress();
    String pathOnRemote = sftpDriver.store(".", fromDvFile, p1);

    assertEquals(fromDvFile.length(), p1.getByteCount());
    assertEquals(TEST_CLOCK.millis(), p1.getTimestamp());

    assertEquals(0, p1.getDirCount());

    // TODO - seems like the  p1.fileCount should be 1.
    assertEquals(0, p1.getFileCount());

    // TODO - seems like the p1.startTime has not been set
    assertEquals(0, p1.getStartTime());

    log.info("pathOnRemote[{}]", pathOnRemote);
    assertEquals("/config/dv_20220326094433", pathOnRemote);

    Path tsPath = Paths.get(SFTP_ROOT_DIR).relativize(Paths.get(pathOnRemote));
    String retrievePath = tsPath.resolve(fromDvFile.toPath().getFileName()).toString();
    log.info("retrievePath[{}]", retrievePath);
    sftpDriver.retrieve(retrievePath, toDirectory, new Progress());
    List<File> files = Files.list(toDirectory.toPath())
        .map(Path::toFile)
        .collect(Collectors.toList());

    File expected = new File(toDirectory, FROM_DV_FILE_NAME);
    assertEquals(expected, files.get(0));
    String contents = readFile(expected);

    assertEquals(contents, fileContents);

    List<FileInfo> result1 = sftpDriver.list(tsPath.toString());
    result1.forEach(System.out::println);
    assertEquals(1, result1.size());
    FileInfo singleFile = result1.get(0);
    assertEquals("", singleFile.getAbsolutePath());
    assertEquals(false, singleFile.getIsDirectory());
    assertEquals(FROM_DV_FILE_NAME, singleFile.getName());
    assertEquals(tsPath.resolve(FROM_DV_FILE_NAME).toString(), singleFile.getKey());

    long fileSize = sftpDriver.getSize(singleFile.getKey());
    assertEquals(fromDvFile.length(), fileSize);

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

  @ParameterizedTest
  @CsvSource({
      "10K , 10_000",
      "5MB , 5_000_000",
  })
  @SneakyThrows
  void testTransferLargeFiles(String label, long fileSize) {
    assertTrue(fileSize > 0, "This test assumes non empty files");
    File tempFileDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
    tempFileDir.deleteOnExit();
    long freeSpace = tempFileDir.getFreeSpace();
    if (freeSpace < fileSize * FREE_SPACE_FACTOR) {
      log.warn("Skipping test of [{}/{}], we don't have [{}]bytes free, just [{}]bytes free", label,
          fileSize, fileSize * 10, freeSpace);
      return;
    }
    List<ProgressEvent> sendEvents = new ArrayList<>();
    ProgressEventListener sendListener = sendEvents::add;
    Progress pSend = new Progress(sendListener);
    File largeFileSend = createLargeFile(tempFileDir, fileSize);
    String pathOnRemote = time(label, "store", () -> sftpDriver.store(".", largeFileSend, pSend));
    Path tsPath = Paths.get(SFTP_ROOT_DIR).relativize(Paths.get(pathOnRemote));

    String largeFileRemotePath = tsPath.resolve(largeFileSend.getName()).toString();
    File largeFileRecv = new File(tempFileDir, "recvFile");
    List<ProgressEvent> recvEvents = new ArrayList<>();
    ProgressEventListener recvListener = recvEvents::add;
    Progress pRecv = new Progress(recvListener);

    time(label, "retrieve", () -> {
      sftpDriver.retrieve(largeFileRemotePath, largeFileRecv, pRecv);
      return null;
    });

    assertEquals(largeFileSend.length(), largeFileRecv.length());
    assertEquals(getMD5(largeFileSend), getMD5(largeFileRecv));

    long sendByteIncEvents = getNumberOfEvents(sendEvents, ProgressEventType.BYTE_COUNT_INC);
    long recvByteIncEvents = getNumberOfEvents(recvEvents, ProgressEventType.BYTE_COUNT_INC);

    assertTrue(sendByteIncEvents > 0);
    assertTrue(recvByteIncEvents > 0);

    if (this.sftpDriver instanceof SFTPFileSystemSSHD) {
      assertEquals(sendByteIncEvents, recvByteIncEvents);
      long numParts = (long) Math.ceil((double) fileSize / (double) UtilitySSHD.BUFFER_SIZE);
      assertEquals(numParts, sendByteIncEvents);
      assertEquals(numParts, recvByteIncEvents);
    }
  }

  @SneakyThrows
  private <T> T time(String label, String action, Callable<T> callable) {
    long start = System.currentTimeMillis();
    try {
      return callable.call();
    } finally {
      log.info("time for [{}/{}] took [{}]ms", action, label, System.currentTimeMillis() - start);
    }
  }

  private long getNumberOfEvents(List<ProgressEvent> events, ProgressEventType type) {
    return events.stream().filter(e -> e.getType() == type).count();
  }

  @SneakyThrows
  private String getMD5(File file) {
    try (InputStream is = new FileInputStream(file)) {
      return DigestUtils.md5Hex(is);
    }
  }

  @SneakyThrows
  File createLargeFile(File tempFileDir, long fileSize) {
    String filename = FileUtils.byteCountToDisplaySize(fileSize).replaceAll("\\s", "");
    File result = new File(tempFileDir, filename);
    Files.createFile(result.toPath());
    try (RandomAccessFile raf = new RandomAccessFile(result, "rw")) {
      raf.setLength(fileSize);
    }
    assertEquals(fileSize, result.length());
    return result;
  }

  @Test
  @SneakyThrows
  public void testSftpDriverSingleDirectoryStoreAndRetrieve() {
    final String FROM_DV_DIR_NAME = "fromDir";
    final String FROM_DV_DIR_FILE_A = "fromDVfileA.txt";
    final String FROM_DV_DIR_FILE_B = "fromDVfileB.txt";
    final String FROM_DV_DIR_FILE_C = "fromDVfileC.txt";
    final String TO_DV_DIR_NAME = "toDir";
    final String TEST_FILE_A_CONTENTS = "aaa-XXXX-AA";
    final String TEST_FILE_B_CONTENTS = "bbb-XXXX-BBBB";
    final String TEST_FILE_C_CONTENTS = "ccc-XXXX-CCCCCC";

    File tempFileDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
    tempFileDir.deleteOnExit();
    File fromDvDir = new File(tempFileDir, FROM_DV_DIR_NAME);
    assertTrue(fromDvDir.mkdir());

    File toDvDir = new File(tempFileDir, TO_DV_DIR_NAME);
    assertTrue(toDvDir.mkdirs());

    File fromDvDirFileA = new File(fromDvDir, FROM_DV_DIR_FILE_A);
    File fromDvDirFileB = new File(fromDvDir, FROM_DV_DIR_FILE_B);
    File fromDvDirFileC = new File(fromDvDir, FROM_DV_DIR_FILE_C);

    File toDvDirFileA = new File(toDvDir, FROM_DV_DIR_FILE_A);
    File toDvDirFileB = new File(toDvDir, FROM_DV_DIR_FILE_B);
    File toDvDirFileC = new File(toDvDir, FROM_DV_DIR_FILE_C);

    writeToFile(fromDvDirFileA, TEST_FILE_A_CONTENTS);
    writeToFile(fromDvDirFileB, TEST_FILE_B_CONTENTS);
    writeToFile(fromDvDirFileC, TEST_FILE_C_CONTENTS);

    log.info("sftpDriver {}", sftpDriver);

    Progress p1 = new Progress();
    String pathOnRemote = sftpDriver.store(".", fromDvDir, p1);
    assertEquals(fromDvDirFileA.length() + fromDvDirFileB.length() + fromDvDirFileC.length(),
        p1.getByteCount());
    assertEquals(TEST_CLOCK.millis(), p1.getTimestamp());

    //TODO - seems like the  p1.dirCount should be 1.
    assertEquals(0, p1.getDirCount());

    //TODO - seems like the  p1.fileCount should be 3.
    assertEquals(0, p1.getFileCount());

    //TODO - seems like the p1.startTime has not been set
    assertEquals(0, p1.getStartTime());

    Path tsPath = Paths.get(SFTP_ROOT_DIR).relativize(Paths.get(pathOnRemote));
    Path retrievePath = tsPath.resolve(fromDvDir.toPath().getFileName());
    log.info("retrievePath[{}]", retrievePath);
    String retrievePathAsString = retrievePath.toString();

    // check files are on SFTP server
    Map<String, FileInfo> fileMap = sftpDriver.list(retrievePathAsString).stream().collect(
        Collectors.toMap(FileInfo::getName, Function.identity()));

    assertEquals(3, fileMap.size());

    FileInfo fileInfoA = fileMap.get(FROM_DV_DIR_FILE_A);
    assertEquals(FROM_DV_DIR_FILE_A, fileInfoA.getName());
    assertEquals(retrievePath.resolve(FROM_DV_DIR_FILE_A).toString(),
        fileInfoA.getKey());
    assertEquals(false, fileInfoA.getIsDirectory());
    assertEquals("", fileInfoA.getAbsolutePath());

    FileInfo fileInfoB = fileMap.get(FROM_DV_DIR_FILE_B);
    assertEquals(FROM_DV_DIR_FILE_B, fileInfoB.getName());
    assertEquals(retrievePath.resolve(FROM_DV_DIR_FILE_B).toString(),
        fileInfoB.getKey());
    assertEquals(false, fileInfoB.getIsDirectory());
    assertEquals("", fileInfoB.getAbsolutePath());

    FileInfo fileInfoC = fileMap.get(FROM_DV_DIR_FILE_C);
    assertEquals(FROM_DV_DIR_FILE_C, fileInfoC.getName());
    assertEquals(retrievePath.resolve(FROM_DV_DIR_FILE_C).toString(),
        fileInfoC.getKey());
    assertEquals(false, fileInfoC.getIsDirectory());
    assertEquals("", fileInfoC.getAbsolutePath());

    assertEquals(0, Files.list(toDvDir.toPath()).count());
    sftpDriver.retrieve(retrievePathAsString, toDvDir, new Progress());

    // We can check we have got 3 files back from SFTP Server
    assertEquals(3, Files.list(toDvDir.toPath()).count());

    assertEquals(TEST_FILE_A_CONTENTS, readFile(toDvDirFileA));
    assertEquals(TEST_FILE_B_CONTENTS, readFile(toDvDirFileB));
    assertEquals(TEST_FILE_C_CONTENTS, readFile(toDvDirFileC));

    long fileSizeA = sftpDriver.getSize(fileInfoA.getKey());
    assertEquals(fromDvDirFileA.length(), fileSizeA);
    assertEquals(FROM_DV_DIR_FILE_A, sftpDriver.getName(fileInfoA.getKey()));

    long fileSizeB = sftpDriver.getSize(fileInfoB.getKey());
    assertEquals(fromDvDirFileB.length(), fileSizeB);
    assertEquals(FROM_DV_DIR_FILE_B, sftpDriver.getName(fileInfoB.getKey()));

    long fileSizeC = sftpDriver.getSize(fileInfoC.getKey());
    assertEquals(fromDvDirFileC.length(), fileSizeC);
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
  }

  public abstract SFTPFileSystemDriver getSftpFileSystemDriver();

  @SneakyThrows
  protected Map<String, String> getStoreProperties() {
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

  void authenticationSetup() throws Exception {
  }
}
