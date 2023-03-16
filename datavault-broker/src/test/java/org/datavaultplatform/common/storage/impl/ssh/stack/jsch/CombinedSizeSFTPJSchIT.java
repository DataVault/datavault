package org.datavaultplatform.common.storage.impl.ssh.stack.jsch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemJSch;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers(disabledWithoutDocker = true)
@Slf4j
public class CombinedSizeSFTPJSchIT extends BaseSFTPFileSystemPrivatePublicKeyPairSizeIT {

  @Container
  static final GenericContainer<?> container = initialiseContainer("SftpPrivatePublicJSchDIT");
  private static final int BYTES_PER_FILE = 4;

  @BeforeAll
  @SneakyThrows
  static void setupContainer() {
    log.info("Copying script createFileTree.sh to /tmp in SFTP Container");
    container.copyFileToContainer(MountableFile.forClasspathResource("sftpsize/createFileTree.sh"),
        "/tmp/createFileTree.sh");
    log.info("Creating directory /config/files in SFTP Container");
    container.execInContainer("mkdir", "-p", "/config/files");
  }

  @BeforeEach
  @SneakyThrows
  void cleanupAnyTestFilesInContainer() {
    container.execInContainer("/bin/bash", "-c", "cd /config/files; rm -rf *");
  }

  static Stream<Arguments> getTestCaseParams() {
    return Stream.of(
        TestCaseParam.SMALL
        //, TestCaseParam.SMALLISH
        //, TestCaseParam.MEDIUM
        //, TestCaseParam.LARGE
    ).map(param -> Arguments.of(param, String.format("%s files[%s] depth[%s]",param,param.fileCount, param.depth)));
  }

  @ParameterizedTest(name="{index}-{1}")
  @MethodSource("getTestCaseParams")
  @SneakyThrows
  void testTotalSizeOfFilesViaSftp(TestCaseParam testCaseParam, String testDescription) {

    log.info("Creating files beneath /config/files in SFTP Container");
    container.execInContainer("/tmp/createFileTree.sh", "/config/files", ""+ testCaseParam.depth);
    log.info("Created files beneath /config/files in SFTP Container");

    // count the number of files in the beneath /config/files in the SFTP Container
    ExecResult result = container.execInContainer(
        "/bin/bash","-c","cd /config/files; find . -type f | wc -l");

    log.info("output message [{}]", result.getStdout());
    log.info("error message [{}]", result.getStderr());
    log.info("exit code [{}]", result.getExitCode());

    assertEquals(0, result.getExitCode());
    assertEquals(testCaseParam.fileCount, Integer.parseInt(result.getStdout().trim()));

    // check that the getSize method of SFTPDriver returns the expected size
    log.info("BEFORE GET SIZE");

    // get the total size files beneath /config/files
    long size = getSftpDriver().getSize("files");
    log.info("AFTER GET SIZE");
    assertEquals(testCaseParam.fileCount * BYTES_PER_FILE, size);
  }

  /**
   * @See DirectrorySizeTest
   */
  enum TestCaseParam {

    SMALL(4, 62),
    SMALLISH(10, 4094),
    MEDIUM(13, 32766),
    LARGE(17, 524286);

    public final int depth;
    public final long fileCount;

    TestCaseParam(int depth, long fileCount) {
      this.depth = depth;
      this.fileCount = fileCount;
    }


  }

  @Override
  public SFTPFileSystemDriver getSftpDriver() {
    Map<String, String> props = getStoreProperties();
    return new SFTPFileSystemJSch("sftp-jsch", props, TEST_CLOCK);
  }

  @Override
  public GenericContainer<?> getContainer() {
    return container;
  }

  @Override
  Logger getLog() {
    return log;
  }
}
