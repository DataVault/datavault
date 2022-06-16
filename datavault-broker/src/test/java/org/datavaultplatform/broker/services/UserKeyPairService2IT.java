package org.datavaultplatform.broker.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.services.UserKeyPairService.KeyPairInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;

/**
 * This test generates a key pair and checks that the keypair is valid by ...
 * using the private and public key with 'scp' to copy a text file between 2 testcontainers.
 * There is a LOT of setup involved to perform this test.
 */
@Slf4j
public class UserKeyPairService2IT extends BaseUserKeyPairServiceTest {

  public static final String PLACEHOLDER = "XXXX";
  public static final String TEST_PASSPHRASE = "tenet";
  public static final String PATH_FROM_SCP = "/tmp/scp.sh";
  public static final String PATH_FROM_EXPECT = "/tmp/scp.exp";
  public static final String PATH_FROM_PRIVATE_KEY = "/tmp/test_rsa";
  public static final String FROM_PATH_RANDOM_FILE = "/tmp/randFrom.txt";
  private static final String PATH_TO_RANDOM_FILE = "/tmp/randTo.txt";
  private static final String TEST_USER = "testuser";
  private static final String ENV_USER_NAME = "USER_NAME";
  private static final String ENV_PUBLIC_KEY = "PUBLIC_KEY";
  private static final String CONTAINER_NAME_FROM = "testfrom";
  private static final String CONTAINER_NAME_TO = "testto";

  Resource runScpUsingExpect = new ClassPathResource("runScpUsingExpect.exp");
  Resource runScp = new ClassPathResource("runScp.sh");
  GenericContainer<?> fromContainer;
  GenericContainer<?> toContainer;

  /**
   * Tests that the key pair is valid by
   * using keypair to perform scp between testcontainers
   */
  @Test
  @Override
  @SneakyThrows
  void testKeyPairIsValid() {
    UserKeyPairService service = new UserKeyPairService(TEST_PASSPHRASE);
    KeyPairInfo info = service.generateNewKeyPair();
    validateKeyPair(info.getPublicKey(), info.getPrivateKey());
  }

  private void validateKeyPair(String publicKey, String privateKey) {

    initContainers(publicKey);

    String random = UUID.randomUUID().toString();

    setupFromContainer(random, privateKey);

    execInContainer(fromContainer, "scp using privateKey", PATH_FROM_EXPECT);

    //if the secure copy(scp) 'from container'->'to contaner' worked
    // - we should have a file to read off 'from container'
    String copiedContents = readFileFromContainer(toContainer, PATH_TO_RANDOM_FILE);

    // if the file contents from 'to container' match 'random'
    // - the scp worked which means
    // - the private key/public key pair is valid
    assertEquals(random, copiedContents);
  }

  void initContainers(String publicKey) {
    ImageFromDockerfile image = new ImageFromDockerfile()
        .withDockerfileFromBuilder(builder -> builder
            .from(IMAGE_NAME_OPENSSH)
            .run("apk add expect") //we have to add 'expect' to openssh image
            .build());

    Network network = Network.newNetwork();

    //we put the publicKey into the TO container at startup - so ssh daemon will trust the private key later on
    toContainer = new GenericContainer(IMAGE_NAME_OPENSSH)
        .withNetwork(network)
        .withNetworkAliases(CONTAINER_NAME_TO)
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PUBLIC_KEY, publicKey) //this causes the public key to be added to /config/.ssh/authorized_keys
        .withExposedPorts(2222)
        .waitingFor(Wait.forListeningPort());

    fromContainer = new GenericContainer(image)
        .withNetwork(network)
        .withNetworkAliases(CONTAINER_NAME_FROM)
        .dependsOn(toContainer);

    toContainer.start();
    fromContainer.start();
  }


  private void setupFromContainer(String random, String privateKey) {
    fromContainer.copyFileToContainer(Transferable.of(random), FROM_PATH_RANDOM_FILE);

    copyPrivateKeyToFromContainer(privateKey);

    copyScpScriptToFromContainer();

    copyExpectScriptToFromContainer();
  }

  @SneakyThrows
  private void copyExpectScriptToFromContainer() {
    String expectScriptTemplate = getStringFromResource(this.runScpUsingExpect);
    String expectScript = expectScriptTemplate.replaceAll(PLACEHOLDER, TEST_PASSPHRASE);
    copyScriptToContainer(fromContainer, expectScript, PATH_FROM_EXPECT);
  }

  @SneakyThrows
  private void copyScpScriptToFromContainer() {
    String scpScript = getStringFromResource(this.runScp);
    copyScriptToContainer(fromContainer, scpScript, PATH_FROM_SCP);
  }

  @SneakyThrows
  private void copyPrivateKeyToFromContainer(String privateKeyContents) {
    fromContainer.copyFileToContainer(Transferable.of(privateKeyContents), PATH_FROM_PRIVATE_KEY);
    execInContainer(fromContainer, "chmod", "chmod 400 " + PATH_FROM_PRIVATE_KEY);
  }

  @AfterEach
  void tearDown() {
    stop(fromContainer);
    stop(toContainer);
  }

  void stop(GenericContainer<?> container) {
    if (container != null) {
      container.stop();
    }
  }

}
