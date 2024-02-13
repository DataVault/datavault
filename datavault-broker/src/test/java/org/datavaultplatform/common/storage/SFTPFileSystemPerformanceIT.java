package org.datavaultplatform.common.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.storage.impl.SFTPConnection;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemJSch;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * This test checks the relative performance of
 *  1) JSch,
 *  2) SSHD without monitoring
 *  3) SSHD with monitoring
 * <p>
 * The test will fail if either of the SSHD tests takes more than 20% longer than JSch
 * </p>
 */
@Slf4j
@Testcontainers(disabledWithoutDocker = true)
public class SFTPFileSystemPerformanceIT {

  static final String TEST_PASSWORD = "testPassword";
  static final String ENV_PASSWORD = "USER_PASSWORD";
  static final String ENV_PASSWORD_ACCESS = "PASSWORD_ACCESS";

  static final String ENV_USER_NAME = "USER_NAME";
  static final String TEST_USER = "testuser";
  static final String TEST_SFTP_SERVER_DEFAULT_DIR = "/config";
  static final int TEST_SFTP_SERVER_PORT = 2222;
  static final int TEST_ITERATIONS = 5;

  static final double PERFORMANCE_THRESHOLD = 1.3;
  public static final int SIZE_50MB = 50_000_000;

  @Container
  GenericContainer<?> sftpServerContainer = getSftpTestContainer();
  File bigFile;
  private SFTPFileSystemSSHD sftpSSHD;
  private SFTPFileSystemJSch sftpJSch;

  GenericContainer<?> getSftpTestContainer() {
    return new GenericContainer<>(DockerImage.OPEN_SSH_8pt6_IMAGE_NAME)
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PASSWORD, TEST_PASSWORD)
        .withEnv(ENV_PASSWORD_ACCESS, "true")
        .withExposedPorts(2222)
        .waitingFor(Wait.forListeningPort());
  }

  @BeforeEach
  @SneakyThrows
  public void setup() {

    //set the SFTPConnection logger level to DEBUG - just for this test
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    Logger logger = context.getLogger(SFTPConnection.class.getName());
    if (logger != null) {
      logger.setLevel(Level.DEBUG);
    }

    bigFile = Files.createTempFile("tempPerf", ".bin").toFile();
    try(RandomAccessFile raf = new RandomAccessFile(bigFile, "rw")) {
      raf.setLength(SIZE_50MB);
    }
    assertEquals(SIZE_50MB, bigFile.length());
    Map<String, String> props = getStoreProperties();
    this.sftpSSHD = new SFTPFileSystemSSHD("sshd", props);
    this.sftpJSch = new SFTPFileSystemJSch("jsch", props);
  }

  @Test
  @SneakyThrows
  void comparePerformanceOfSftpDrivers() {

    List<ProgressInfo> infoStoreSSHD = new ArrayList<>();
    List<ProgressInfo> infoStoreSSHDMonitor = new ArrayList<>();
    List<ProgressInfo> infoStoreJSch = new ArrayList<>();

    List<ProgressInfo> infoRetrieveSSHD = new ArrayList<>();
    List<ProgressInfo> infoRetrieveSSHDMonitor = new ArrayList<>();
    List<ProgressInfo> infoRetrieveJSch = new ArrayList<>();

    for (int i = 0; i < TEST_ITERATIONS; i++) {

      sftpSSHD.setMonitoring(false);
      Pair<ProgressInfo, ProgressInfo> sshd = timedStoreRetrieve("SSHD", sftpSSHD);

      sftpSSHD.setMonitoring(true);
      Pair<ProgressInfo, ProgressInfo> sshdMon = timedStoreRetrieve("SSHDMon", sftpSSHD);

      Pair<ProgressInfo, ProgressInfo> jsch = timedStoreRetrieve("JSch", sftpJSch);

      infoStoreSSHD.add(sshd.getFirst());
      infoStoreSSHDMonitor.add(sshdMon.getFirst());
      infoStoreJSch.add(jsch.getFirst());

      infoRetrieveSSHD.add(sshd.getSecond());
      infoRetrieveSSHDMonitor.add(sshdMon.getSecond());
      infoRetrieveJSch.add(jsch.getSecond());
    }
    summarize("Store", infoStoreJSch, infoStoreSSHD, infoStoreSSHDMonitor);
    summarize("Retrieve", infoRetrieveJSch, infoRetrieveSSHD, infoRetrieveSSHDMonitor);
  }

  private void summarize(String label, List<ProgressInfo> infoJSch, List<ProgressInfo> infoSSHD, List<ProgressInfo> infoSSHDMonitor){
    LongSummaryStatistics summarySSHD = infoSSHD.stream().mapToLong(st -> st.time)
        .summaryStatistics();
    LongSummaryStatistics summarySSHDMonitor = infoSSHDMonitor.stream().mapToLong(st -> st.time)
        .summaryStatistics();
    LongSummaryStatistics summaryJSch = infoJSch.stream().mapToLong(st -> st.time)
        .summaryStatistics();

    log.info("Stats [{}] JSch    [{}]", label, summaryJSch);
    log.info("Stats [{}] SSHD    [{}]", label, summarySSHD);
    log.info("Stats [{}] SSHDMon [{}]", label, summarySSHDMonitor);

    double base = summaryJSch.getAverage();
    double threshold = base * PERFORMANCE_THRESHOLD;

    assertTrue(summarySSHD.getAverage() <= threshold, "for " + label + ", SFTP with SSHD without monitoring takes > " + PERFORMANCE_THRESHOLD + " longer than JSch");
    assertTrue(summarySSHD.getAverage() <= threshold, "for " + label + ", SFTP with SSHD with monitoring takes > " + PERFORMANCE_THRESHOLD + " longer than JSch");
  }

  @SneakyThrows
  private Pair<ProgressInfo,ProgressInfo> timedStoreRetrieve(String label, SFTPFileSystemDriver sftpFileSystemDriver) {

    Progress progressStore = new Progress();
    long start1 = System.currentTimeMillis();
    String storedPath = sftpFileSystemDriver.store(".", bigFile, progressStore);
    long diff1 = System.currentTimeMillis() - start1;
    ProgressInfo pInfoStore = new ProgressInfo("store", diff1, progressStore);


    File retrieveDir = Files.createTempDirectory("tempRetrieve").toFile();
    String relativeStoredPath = Paths.get(TEST_SFTP_SERVER_DEFAULT_DIR).relativize(Paths.get(storedPath)).toString();
    Progress progressRetrieve = new Progress();
    long start2 = System.currentTimeMillis();
    sftpFileSystemDriver.retrieve(relativeStoredPath, retrieveDir, progressRetrieve);
    long diff2 = System.currentTimeMillis() - start2;
    ProgressInfo pInfoRetrieve = new ProgressInfo("retrieve",diff2, progressRetrieve);

    return Pair.of(pInfoStore, pInfoRetrieve);
  }

  protected Map<String, String> getStoreProperties() {
    HashMap<String, String> props = new HashMap<>();

    //standard sftp properties
    props.put(PropNames.USERNAME, TEST_USER);
    props.put(PropNames.ROOT_PATH,
        TEST_SFTP_SERVER_DEFAULT_DIR); //this is the directory ON THE SFTP SERVER - for OpenSSH containers, it's config
    props.put(PropNames.HOST, sftpServerContainer.getHost());
    props.put(PropNames.PORT,
        String.valueOf(sftpServerContainer.getMappedPort(TEST_SFTP_SERVER_PORT)));
    props.put(PropNames.PASSWORD, TEST_PASSWORD);

    return props;
  }

  static class ProgressInfo {

    final String label;
    final long time;

    final Progress progress;

    ProgressInfo(String label, long time, Progress progress) {
      this.label = label;
      this.time = time;
      this.progress = progress;
      log.info("byteCount[{}][{}]", label, progress.getByteCount());
    }
  }
}
