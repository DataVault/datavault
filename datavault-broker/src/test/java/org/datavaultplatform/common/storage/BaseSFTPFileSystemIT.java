package org.datavaultplatform.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.io.ProgressEvent;
import org.datavaultplatform.common.io.ProgressEventListener;
import org.datavaultplatform.common.io.ProgressEventType;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;
import org.datavaultplatform.common.storage.impl.ssh.UtilitySSHD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;

public abstract class BaseSFTPFileSystemIT {

  static final int SFTP_SERVER_PORT = 2222;

  static final String ENV_USER_NAME = "USER_NAME";
  static final String TEST_USER = "testuser";

  static final String SFTP_ROOT_DIR = "/config";
  static final Clock TEST_CLOCK = Clock.fixed(Instant.parse("2022-03-26T09:44:33.22Z"),
      ZoneId.of("Europe/London"));
  static final String TEMP_PREFIX = "dvSftpTempDir";
  static final long EXPECTED_SPACE_AVAILABLE_ON_SFTP_SERVER = 100_000;
  private static final int FREE_SPACE_FACTOR = 10;

  static final Path tempLocalPath;

  static {
    try {
      tempLocalPath = Files.createTempDirectory("sftpTestFilesDir");
      for (int i = 0; i < 1000; i++) {
        Path tempFile = tempLocalPath.resolve(String.format("temp-%s.txt", i));
        try (PrintWriter pw = new PrintWriter(new FileWriter(tempFile.toFile()))) {
          pw.printf("test file number - [%s]%n", i);

        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public abstract GenericContainer<?> getContainer();

  public abstract SFTPFileSystemDriver getSftpDriver();

  public abstract void addAuthenticationProps(Map<String,String> props);

  public final int getSftpServerPort() {
    return getContainer().getMappedPort(SFTP_SERVER_PORT);
  }

  public final String getSftpServerHost() {
    return getContainer().getHost();
  }

  private void logContainerId(String label) {
    getLog().info("for [{}] containerId[{}]", label, getContainer().getContainerId());
  }

  @SneakyThrows
  protected Map<String, String> getStoreProperties() {
    HashMap<String, String> props = new HashMap<>();

    //standard sftp properties
    props.put(PropNames.USERNAME, TEST_USER);
    props.put(PropNames.ROOT_PATH, SFTP_ROOT_DIR); //this is the directory ON THE SFTP SERVER - for ALL OpenSSH containers, it's config
    props.put(PropNames.HOST, getSftpServerHost());
    props.put(PropNames.PORT, String.valueOf(getSftpServerPort()));

    addAuthenticationProps(props);

    return props;
  }

  /* TESTS BELOW HERE */

  @Test
  @SneakyThrows
  void testFileSize() {
    long size = getSftpDriver().getSize("sshd.pid");
    assertEquals(4, size);
  }

  @Test
  @SneakyThrows
  void testFileExists() {
    assertTrue(getSftpDriver().exists("sshd.pid"));
    assertFalse(getSftpDriver().exists("sshd.pid.nope"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/",".","./","/.","/./","","//"})
  @SneakyThrows
  void testDirExists(String path) {
    assertTrue(getSftpDriver().exists(path));
  }

  @Test
  @SneakyThrows
  void testValid() {
    assertTrue(getSftpDriver().valid("sshd.pid"));
    assertTrue(getSftpDriver().valid("sshd.pid.nope"));
  }

  @Test
  @SneakyThrows
  void testIsDir() {
    assertFalse(getSftpDriver().isDirectory("sshd.pid"));
    assertTrue(getSftpDriver().valid("."));
    assertTrue(getSftpDriver().valid(".."));
  }

  @Test
  @SneakyThrows
  void testGetName() {
    assertEquals("sshd.pid", getSftpDriver().getName("sshd.pid"));
    assertEquals(".", getSftpDriver().getName("."));
    assertEquals("..", getSftpDriver().getName(".."));
  }

  @Test
  @SneakyThrows
  void testUsableSpace() {
    assertThat(getSftpDriver().getUsableSpace()).isGreaterThan(1_000_000_000L);
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

    getLog().info("sftpDriver {}", getSftpDriver());

    Progress p1 = new Progress();
    String pathOnRemote = getSftpDriver().store(".", fromDvFile, p1, "dv_20220326094433");

    if(getSftpDriver().isMonitoring()) {
      assertEquals(fromDvFile.length(), p1.getByteCount());
      assertEquals(TEST_CLOCK.millis(), p1.getTimestamp());

      assertEquals(0, p1.getDirCount());

      // TODO - seems like the  p1.fileCount should be 1.
      assertEquals(0, p1.getFileCount());

      // TODO - seems like the p1.startTime has not been set
      assertEquals(0, p1.getStartTime());
    }

    getLog().info("pathOnRemote[{}]", pathOnRemote);
    assertEquals("/config/dv_20220326094433", pathOnRemote);

    Path tsPath = Paths.get(SFTP_ROOT_DIR).relativize(Paths.get(pathOnRemote));
    String retrievePath = tsPath.resolve(fromDvFile.toPath().getFileName()).toString();
    getLog().info("retrievePath[{}]", retrievePath);
    getSftpDriver().retrieve(retrievePath, toDvFile, new Progress());
    String contents = readFile(toDvFile);

    assertEquals(contents, TEST_FILE_CONTENTS);

    List<FileInfo> result1 = getSftpDriver().list(tsPath.toString());
    result1.forEach(System.out::println);
    assertEquals(1, result1.size());
    FileInfo singleFile = result1.get(0);
    assertEquals("", singleFile.getAbsolutePath());
    assertEquals(false, singleFile.getIsDirectory());
    assertEquals(FROM_DV_FILE_NAME, singleFile.getName());
    assertEquals(tsPath.resolve(FROM_DV_FILE_NAME).toString(), singleFile.getKey());

    long fileSize = getSftpDriver().getSize(singleFile.getKey());
    assertEquals(fromDvFile.length(), fileSize);

    assertTrue(getSftpDriver().exists(singleFile.getKey()));
    assertFalse(getSftpDriver().exists(singleFile.getKey() + ".txt"));

    assertTrue(getSftpDriver().isDirectory(tsPath.toString()));
    assertFalse(getSftpDriver().isDirectory(singleFile.getKey()));

    long actualSpaceAvailable = getSftpDriver().getUsableSpace();
    assertTrue(actualSpaceAvailable > EXPECTED_SPACE_AVAILABLE_ON_SFTP_SERVER);

    assertEquals(FROM_DV_FILE_NAME, getSftpDriver().getName(singleFile.getKey()));
    assertEquals(FROM_DV_FILE_NAME, getSftpDriver().getName(FROM_DV_FILE_NAME));

    /* isValid always returns true :-( */
    assertTrue(getSftpDriver().valid(singleFile.getKey()));
    assertTrue(getSftpDriver().valid(singleFile.getKey() + ".txt"));

    assertThrows(NullPointerException.class, () -> getSftpDriver().getName(null));
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

    getLog().info("sftpDriver {}", getSftpDriver());

    Progress p1 = new Progress();
    String pathOnRemote = getSftpDriver().store(".", fromDvFile, p1, "dv_20220326094433");

    if(getSftpDriver().isMonitoring()) {
      assertEquals(fromDvFile.length(), p1.getByteCount());
      assertEquals(TEST_CLOCK.millis(), p1.getTimestamp());

      assertEquals(0, p1.getDirCount());

      // TODO - seems like the  p1.fileCount should be 1.
      assertEquals(0, p1.getFileCount());

      // TODO - seems like the p1.startTime has not been set
      assertEquals(0, p1.getStartTime());
    }

    getLog().info("pathOnRemote[{}]", pathOnRemote);
    assertEquals("/config/dv_20220326094433", pathOnRemote);

    Path tsPath = Paths.get(SFTP_ROOT_DIR).relativize(Paths.get(pathOnRemote));
    String retrievePath = tsPath.resolve(fromDvFile.toPath().getFileName()).toString();
    getLog().info("retrievePath[{}]", retrievePath);
    getSftpDriver().retrieve(retrievePath, toDirectory, new Progress());
    List<File> files = Files.list(toDirectory.toPath())
        .map(Path::toFile)
        .toList();

    File expected = new File(toDirectory, FROM_DV_FILE_NAME);
    assertEquals(expected, files.get(0));
    String contents = readFile(expected);

    assertEquals(contents, fileContents);

    List<FileInfo> result1 = getSftpDriver().list(tsPath.toString());
    result1.forEach(System.out::println);
    assertEquals(1, result1.size());
    FileInfo singleFile = result1.get(0);
    assertEquals("", singleFile.getAbsolutePath());
    assertEquals(false, singleFile.getIsDirectory());
    assertEquals(FROM_DV_FILE_NAME, singleFile.getName());
    assertEquals(tsPath.resolve(FROM_DV_FILE_NAME).toString(), singleFile.getKey());

    long fileSize = getSftpDriver().getSize(singleFile.getKey());
    assertEquals(fromDvFile.length(), fileSize);

    assertTrue(getSftpDriver().exists(singleFile.getKey()));
    assertFalse(getSftpDriver().exists(singleFile.getKey() + ".txt"));

    assertTrue(getSftpDriver().isDirectory(tsPath.toString()));
    assertFalse(getSftpDriver().isDirectory(singleFile.getKey()));

    long actualSpaceAvailable = getSftpDriver().getUsableSpace();
    assertTrue(actualSpaceAvailable > EXPECTED_SPACE_AVAILABLE_ON_SFTP_SERVER);

    assertEquals(FROM_DV_FILE_NAME, getSftpDriver().getName(singleFile.getKey()));
    assertEquals(FROM_DV_FILE_NAME, getSftpDriver().getName(FROM_DV_FILE_NAME));

    /* isValid always returns true :-( */
    assertTrue(getSftpDriver().valid(singleFile.getKey()));
    assertTrue(getSftpDriver().valid(singleFile.getKey() + ".txt"));

    assertThrows(NullPointerException.class, () -> getSftpDriver().getName(null));

    //retrieve a Single File to a Directory that does not exist
    File doesNotExistDir = new File(tempFileDir, "doesNotExist");
    assertFalse(doesNotExistDir.exists());
    getSftpDriver().retrieve(tsPath.toString(), doesNotExistDir, new Progress());
    assertTrue(doesNotExistDir.exists());
    assertTrue(doesNotExistDir.isDirectory());

    assertEquals(fileContents.length(), getSftpDriver().getSize(tsPath.toString()));
  }

  @ParameterizedTest
  @CsvSource({
      "10K , 10_000",
      "5MB , 5_000_000"
  })
  @SneakyThrows
  void testTransferLargeFiles(String label, long fileSize) {
    assertTrue(fileSize > 0, "This test assumes non empty files");
    File tempFileDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
    tempFileDir.deleteOnExit();
    long freeSpace = tempFileDir.getFreeSpace();
    if (freeSpace < fileSize * FREE_SPACE_FACTOR) {
      getLog().warn("Skipping test of [{}/{}], we don't have [{}]bytes free, just [{}]bytes free", label,
          fileSize, fileSize * 10, freeSpace);
      return;
    }
    List<ProgressEvent> sendEvents = new ArrayList<>();
    ProgressEventListener sendListener = sendEvents::add;
    Progress pSend = new Progress(sendListener);
    File largeFileSend = createLargeFile(tempFileDir, fileSize);
    String pathOnRemote = time(label, "store", () -> getSftpDriver().store(".", largeFileSend, pSend,
            "dv_20220326094433"));
    Path tsPath = Paths.get(SFTP_ROOT_DIR).relativize(Paths.get(pathOnRemote));

    String largeFileRemotePath = tsPath.resolve(largeFileSend.getName()).toString();
    File largeFileRecv = new File(tempFileDir, "recvFile");
    if (largeFileRecv.exists()) {
      largeFileRecv.delete();
    }
    List<ProgressEvent> recvEvents = new ArrayList<>();
    ProgressEventListener recvListener = recvEvents::add;
    Progress pRecv = new Progress(recvListener);

    time(label, "retrieve", () -> {
      getSftpDriver().retrieve(largeFileRemotePath, largeFileRecv, pRecv);
      return null;
    });

    if(getSftpDriver().isMonitoring()) {
      assertEquals(largeFileSend.length(), largeFileRecv.length());
      assertEquals(getMD5(largeFileSend), getMD5(largeFileRecv));

      long sendByteIncEvents = getNumberOfEvents(sendEvents, ProgressEventType.BYTE_COUNT_INC);
      long recvByteIncEvents = getNumberOfEvents(recvEvents, ProgressEventType.BYTE_COUNT_INC);

      assertTrue(sendByteIncEvents > 0);
      assertTrue(recvByteIncEvents > 0);

      if (getSftpDriver() instanceof SFTPFileSystemSSHD) {
        assertEquals(sendByteIncEvents, recvByteIncEvents);
        long numParts = (long) Math.ceil((double) fileSize / (double) UtilitySSHD.BUFFER_SIZE);
        assertTrue(sendByteIncEvents >= numParts);
        assertTrue(recvByteIncEvents >= numParts);
      }
    }
  }

  @SneakyThrows
  private <T> T time(String label, String action, Callable<T> callable) {
    long start = System.currentTimeMillis();
    try {
      return callable.call();
    } finally {
      getLog().info("time for [{}/{}] took [{}]ms", action, label, System.currentTimeMillis() - start);
    }
  }

  private long getNumberOfEvents(List<ProgressEvent> events, ProgressEventType type) {
    return events.stream().filter(e -> e.getType() == type).count();
  }

  @SneakyThrows
  private String getMD5(File file) {
    try (InputStream is = Files.newInputStream(file.toPath())) {
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

    getLog().info("sftpDriver {}", getSftpDriver());

    Progress p1 = new Progress();
    String pathOnRemote = getSftpDriver().store(".", fromDvDir, p1, "dv_20220326094433");
    if(getSftpDriver().isMonitoring()) {
      assertEquals(fromDvDirFileA.length() + fromDvDirFileB.length() + fromDvDirFileC.length(),
          p1.getByteCount());
      assertEquals(TEST_CLOCK.millis(), p1.getTimestamp());

      //TODO - seems like the  p1.dirCount should be 1.
      assertEquals(0, p1.getDirCount());

      //TODO - seems like the  p1.fileCount should be 3.
      assertEquals(0, p1.getFileCount());

      //TODO - seems like the p1.startTime has not been set
      assertEquals(0, p1.getStartTime());
    }

    Path tsPath = Paths.get(SFTP_ROOT_DIR).relativize(Paths.get(pathOnRemote));
    Path retrievePath = tsPath.resolve(fromDvDir.toPath().getFileName());
    getLog().info("retrievePath[{}]", retrievePath);
    String retrievePathAsString = retrievePath.toString();

    // check files are on SFTP server
    Map<String, FileInfo> fileMap = getSftpDriver().list(retrievePathAsString).stream().collect(
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
    getSftpDriver().retrieve(retrievePathAsString, toDvDir, new Progress());

    // We can check we have got 3 files back from SFTP Server
    assertEquals(3, Files.list(toDvDir.toPath()).count());

    assertEquals(TEST_FILE_A_CONTENTS, readFile(toDvDirFileA));
    assertEquals(TEST_FILE_B_CONTENTS, readFile(toDvDirFileB));
    assertEquals(TEST_FILE_C_CONTENTS, readFile(toDvDirFileC));

    long fileSizeA = getSftpDriver().getSize(fileInfoA.getKey());
    assertEquals(fromDvDirFileA.length(), fileSizeA);
    assertEquals(FROM_DV_DIR_FILE_A, getSftpDriver().getName(fileInfoA.getKey()));

    long fileSizeB = getSftpDriver().getSize(fileInfoB.getKey());
    assertEquals(fromDvDirFileB.length(), fileSizeB);
    assertEquals(FROM_DV_DIR_FILE_B, getSftpDriver().getName(fileInfoB.getKey()));

    long fileSizeC = getSftpDriver().getSize(fileInfoC.getKey());
    assertEquals(fromDvDirFileC.length(), fileSizeC);
    assertEquals(FROM_DV_DIR_FILE_C, getSftpDriver().getName(fileInfoC.getKey()));

    //Check that we cannot retrieve a directory back to a file
    File singleFile = Files.createFile(tempFileDir.toPath().resolve("file.txt")).toFile();
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
      getSftpDriver().retrieve(retrievePathAsString, singleFile, new Progress());
    });
    assertEquals(String.format("You cannot retrieve remote directory [/config/%s] back to a non-directory[%s]",retrievePathAsString,
        singleFile.getAbsolutePath()), ex.getMessage());

    //retrieve a Single Dir to a Single Dir that does not exist
    File doesNotExistDir = new File(tempFileDir, "doesNotExist");
    assertFalse(doesNotExistDir.exists());
    getSftpDriver().retrieve(retrievePathAsString, doesNotExistDir, new Progress());
    List<FileInfo> items = getSftpDriver().list(retrievePathAsString);
    assertTrue(doesNotExistDir.exists());
    assertTrue(doesNotExistDir.isDirectory());

    assertEquals((TEST_FILE_A_CONTENTS + TEST_FILE_B_CONTENTS + TEST_FILE_C_CONTENTS).length(), getSftpDriver().getSize(retrievePathAsString));
  }

  @Test
  @SneakyThrows
  public void testSftpDriverNestedDirectoryStoreAndRetrieve() {
    final String FROM_DV_DIR_NAME = "fromDir";
    final String DIR_A = "DIR_AAA";
    final String DIR_B = "DIR_BBB";
    final String DIR_C = "DIR_CCC";
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

    File dirA = new File(fromDvDir, DIR_A);
    dirA.mkdir();

    File dirB = new File(dirA, DIR_B);
    dirB.mkdir();

    File dirC = new File(dirB, DIR_C);
    dirC.mkdir();

    File fromDvDirFileA = new File(dirA, FROM_DV_DIR_FILE_A);
    File fromDvDirFileB = new File(dirB, FROM_DV_DIR_FILE_B);
    File fromDvDirFileC = new File(dirC, FROM_DV_DIR_FILE_C);

    writeToFile(fromDvDirFileA, TEST_FILE_A_CONTENTS);
    writeToFile(fromDvDirFileB, TEST_FILE_B_CONTENTS);
    writeToFile(fromDvDirFileC, TEST_FILE_C_CONTENTS);

    getLog().info("sftpDriver {}", getSftpDriver());

    Progress p1 = new Progress();
    String pathOnRemote = getSftpDriver().store(".", fromDvDir, p1, "dv_20220326094433");

    Path tsPath = Paths.get(SFTP_ROOT_DIR).relativize(Paths.get(pathOnRemote));
    Path retrievePath = tsPath.resolve(fromDvDir.toPath().getFileName());
    getLog().info("retrievePath[{}]", retrievePath);
    String retrievePathAsString = retrievePath.toString();

    assertEquals(0, Files.list(toDvDir.toPath()).count());
    getSftpDriver().retrieve(retrievePathAsString, toDvDir, new Progress());

    File toDvDirFileA = Paths.get(toDvDir.getAbsolutePath(), DIR_A, FROM_DV_DIR_FILE_A).toFile();
    File toDvDirFileB = Paths.get(toDvDir.getAbsolutePath(), DIR_A, DIR_B, FROM_DV_DIR_FILE_B).toFile();
    File toDvDirFileC = Paths.get(toDvDir.getAbsolutePath(), DIR_A, DIR_B, DIR_C, FROM_DV_DIR_FILE_C).toFile();

    // We can check we have got 3 files back from SFTP Server
    Collection<File> retrievedFilesAndDirs = FileUtils.listFiles(toDvDir,
        TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

    assertEquals(3, retrievedFilesAndDirs.size());
    assertTrue(retrievedFilesAndDirs.contains(toDvDirFileA));
    assertTrue(retrievedFilesAndDirs.contains(toDvDirFileB));
    assertTrue(retrievedFilesAndDirs.contains(toDvDirFileC));

    assertEquals(TEST_FILE_A_CONTENTS, readFile(toDvDirFileA));
    assertEquals(TEST_FILE_B_CONTENTS, readFile(toDvDirFileB));
    assertEquals(TEST_FILE_C_CONTENTS, readFile(toDvDirFileC));

    assertEquals((TEST_FILE_A_CONTENTS + TEST_FILE_B_CONTENTS + TEST_FILE_C_CONTENTS).length(), getSftpDriver().getSize(retrievePathAsString));
  }


  @SneakyThrows
  private String readFile(File file) {
    return Files.readString(file.toPath(), StandardCharsets.UTF_8);
  }

  @SneakyThrows
  private void writeToFile(File file, String contents) {
    Files.writeString(file.toPath(), contents, StandardCharsets.UTF_8);
  }

  @AfterEach
  @SneakyThrows
  void cleanup() {
    executeCommand("pwd");
    executeCommand("ls -l /config");
    executeCommand("/bin/bash","-c","if [ -d config/dv_20220326094433 ]; then rm -rf config/dv_20220326094433; fi");
  }

  @SneakyThrows
  private void executeCommand(String command) {
    String[] args = command.split("\\s");
    executeCommand(args);
  }

  @SneakyThrows
  private void executeCommand(String... commands){
    getLog().info("COMMANDS {}", Arrays.toString(commands));
    String command = String.join(" ", commands);
    getLog().info("COMMAND {}", command);
    getLog().info("container4[{}}]", getContainer().getEnvMap().get("TC_NAME"));
    ExecResult result = getContainer().execInContainer(commands);
    getLog().info("exitcode[{}][{}]", command,result.getExitCode());
    getLog().info("stderr[{}][{}]", command,result.getStderr());
    getLog().info("stdout[{}][{}]", command,result.getStdout());
  }

  abstract Logger getLog();

  @Test
  public void testListOneThousandFiles() {

    long startMS = System.currentTimeMillis();
    List<FileInfo> files = getSftpDriver().list(".");
    long diffMS = System.currentTimeMillis() - startMS;
    for (int i = 0; i < files.size(); i++) {
      FileInfo info = files.get(i);
      System.out.printf("%04d - [%s]%n", i, info);
    }
    getLog().info("Listing {} files took [{}]ms", files.size(), diffMS);
    assertThat(files).hasSizeGreaterThan(1_000);
    assertThat(Duration.ofMillis(diffMS)).isLessThan(Duration.ofSeconds(5));
  }
}
