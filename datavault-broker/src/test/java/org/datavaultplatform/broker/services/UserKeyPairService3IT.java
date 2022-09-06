package org.datavaultplatform.broker.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.services.UserKeyPairService.KeyPairInfo;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.storage.impl.JSchLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * This test generates a key pair and checks that the keypair is valid by ...
 * using the private and public key with OpenSSH to establish ssh connection via JSch
 */
@Slf4j
public class UserKeyPairService3IT extends BaseUserKeyPairServiceTest {

  private static final String ENV_USER_NAME = "USER_NAME";
  private static final String ENV_PUBLIC_KEY = "PUBLIC_KEY";

  public static final String TEST_PASSPHRASE = "tenet";
  private static final String TEST_USER = "testuser";

  private int sftpServerPort;
  private GenericContainer<?> sftpServer;

  /**
   * Tests that the key pair is valid by
   * using keypair to perform scp between testcontainers
   */

  @ParameterizedTest
  @MethodSource("provideUserKeyPairService")
  @Override
  @SneakyThrows
  void testKeyPair(UserKeyPairService service) {
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
    sftpServer = new GenericContainer<>(DockerImageName.parse(DockerImage.OPEN_SSH_8pt6_IMAGE_NAME))
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PUBLIC_KEY, publicKey) //this causes the public key to be added to /config/.ssh/authorized_keys
        .withExposedPorts(2222)
        .waitingFor(Wait.forListeningPort());

    sftpServer.start();
    sftpServerPort = sftpServer.getMappedPort(2222);
  }

  @AfterEach
  void tearDown() {
    if(sftpServer != null){
      sftpServer.stop();
    }
  }
}
