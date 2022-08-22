package org.datavaultplatform.broker.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.services.UserKeyPairService.KeyPairInfo;
import org.datavaultplatform.broker.test.EmbeddedSftpServer;
import org.datavaultplatform.broker.test.SftpServerUtils;
import org.datavaultplatform.common.storage.impl.JSchLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * This test generates a key pair and checks that the keypair is valid by ...
 * using the private and public key with EmbeddedSftpServer to establish ssh connection via JSch
 */
@Slf4j
public class UserKeyPairService3IT extends BaseUserKeyPairServiceTest {

  public static final String TEST_PASSPHRASE = "tenet";
  private static final String TEST_USER = "testuser";

  private int sftpServerPort;
  private EmbeddedSftpServer sftpServer;

  /**
   * Tests that the key pair is valid by
   * using keypair to perform scp between testcontainers
   */

  @Test
  @Override
  @SneakyThrows
  void testKeyPair() {
    UserKeyPairService service = new UserKeyPairServiceJSchImpl(TEST_PASSPHRASE);
    KeyPairInfo info = service.generateNewKeyPair();
    validateKeyPair(info.getPublicKey(), info.getPrivateKey().getBytes(StandardCharsets.UTF_8));
  }

  @Test
  @SneakyThrows
  void testKeyPairIsInValid() {
    UserKeyPairService service = new UserKeyPairServiceJSchImpl(TEST_PASSPHRASE);
    KeyPairInfo info = service.generateNewKeyPair();
    byte[] badBytes = info.getPrivateKey().getBytes(StandardCharsets.UTF_8);
    //just by changing 1 byte of private key - we should get an error
    badBytes[0] = (byte)166;
    JSchException ex = assertThrows(JSchException.class, () -> validateKeyPair(info.getPublicKey(), badBytes ));
    assertTrue(ex.getMessage().startsWith("invalid privatekey"));
  }


  @SneakyThrows
  private void validateKeyPair(String publicKey, byte[] privateKeyBytes) {

    initSftpServer(publicKey);
    JSch.setLogger(JSchLogger.getInstance());
    JSch jSch = new JSch();
    Session session = jSch.getSession(TEST_USER, "localhost", this.sftpServerPort);
    jSch.addIdentity(TEST_USER, privateKeyBytes, null, TEST_PASSPHRASE.getBytes());
    java.util.Properties properties = new java.util.Properties();
    properties.put(STRICT_HOST_KEY_CHECKING, NO);
    session.setConfig(properties);
    try {
      session.connect();
    } catch(JSchException ex) {
      fail("ssh error",ex);
    }
    log.info("Connected!");
  }

  @SneakyThrows
  void initSftpServer(String publicKey) {
    Path tempSftpFolder = Files.createTempDirectory("SFTP_TEST");
    this.sftpServer = SftpServerUtils.getSftpServer(publicKey, tempSftpFolder);
    this.sftpServerPort= sftpServer.getServer().getPort();
  }

  @AfterEach
  void tearDown() {
    if(this.sftpServer != null){
      this.sftpServer.stop();
    }
  }
}
