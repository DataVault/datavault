package org.datavaultplatform.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.datavaultplatform.broker.services.UserKeyPairServiceJSchImpl;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.crypto.SshRsaKeyUtils;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava.Range;
import org.springframework.boot.system.JavaVersion;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@ConditionalOnJava(value=JavaVersion.NINE, range=Range.EQUAL_OR_NEWER)
@Testcontainers(disabledWithoutDocker = true)
public class SFTPFileSystemSSHDPrivatePublicKeyPairIT extends BaseSFTPFileSystemIT {

  static final String TEST_PASSPHRASE = "tenet";
  static final String ENV_PUBLIC_KEY = "PUBLIC_KEY";
  static final String KEY_STORE_PASSWORD = "keyStorePassword";
  static final String SSH_KEY_NAME = "sshKeyName";

  @TempDir
  File keyStoreTempDir;

  @Override
  public SFTPFileSystemDriver getSftpFileSystemDriver() {
    Map<String, String> props = getStoreProperties();
    return new SFTPFileSystemSSHD("mina-sshd", props, TEST_CLOCK);
  }

  @Override
  GenericContainer<?> getSftpTestContainer() {
    return new GenericContainer<>(DockerImage.OPEN_SSH_8pt6_IMAGE_NAME)
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PUBLIC_KEY, this.keyPairInfo.getPublicKey()) //this causes the public key to be added to /config/.ssh/authorized_keys
        .withExposedPorts(2222)
        .waitingFor(Wait.forListeningPort());
  }

  @Override
  @SneakyThrows
  void addAuthenticationProps(HashMap<String, String> props) {
    byte[] iv = Encryption.generateIV();
    byte[] encrypted = Encryption.encryptSecret(keyPairInfo.getPrivateKey(), null, iv);

    props.put("passphrase", TEST_PASSPHRASE);
    props.put("iv", Base64.toBase64String(iv));
    props.put("privateKey", Base64.toBase64String(encrypted));

    RSAPublicKey publicKey = SshRsaKeyUtils.readPublicKey(
        keyPairInfo.getPublicKey());
    log.info("ORIG PUBLIC KEY MODULUS [{}]", publicKey.getModulus().toString(16));
  }


  @Test
  void testList() {
    List<FileInfo> items = this.sftpDriver.list(".");
    items.forEach(System.out::println);
  }

  @Test
  @SneakyThrows
  void testFileSize() {
    long size = this.sftpDriver.getSize("sshd.pid");
    assertEquals(4, size);
  }

  @Test
  @SneakyThrows
  void testExists() {
    assertTrue(this.sftpDriver.exists("sshd.pid"));
    assertFalse(this.sftpDriver.exists("sshd.pid.nope"));
  }

  @Test
  @SneakyThrows
  void testValid() {
    assertTrue(this.sftpDriver.valid("sshd.pid"));
    assertTrue(this.sftpDriver.valid("sshd.pid.nope"));
  }

  @Test
  @SneakyThrows
  void testIsDir() {
    assertFalse(this.sftpDriver.isDirectory("sshd.pid"));
    assertTrue(this.sftpDriver.valid("."));
    assertTrue(this.sftpDriver.valid(".."));
  }

  @Test
  @SneakyThrows
  void testGetName() {
    assertEquals("sshd.pid", this.sftpDriver.getName("sshd.pid"));
    assertEquals(".", this.sftpDriver.getName("."));
    assertEquals("..", this.sftpDriver.getName(".."));
  }

  @Test
  @SneakyThrows
  void testUsableSpace() {
    assertThat(this.sftpDriver.getUsableSpace()).isGreaterThan(58_000_000_000L);
  }

  @Override
  void authenticationSetup()  throws Exception {

    Encryption.addBouncyCastleSecurityProvider();
    String keyStorePath = keyStoreTempDir.toPath().resolve("test.ks").toString();
    log.info("TEMP KEY IS AT [{}]", keyStorePath);

    Encryption enc = new Encryption();
    enc.setVaultEnable(false);
    enc.setVaultPrivateKeyEncryptionKeyName(SSH_KEY_NAME);

    enc.setKeystoreEnable(true);
    enc.setKeystorePath(keyStorePath);
    enc.setKeystorePassword(KEY_STORE_PASSWORD);

    SecretKey keyForKeyStore = Encryption.generateSecretKey();

    assertFalse(new File(keyStorePath).exists());

    // Encryption class uses 'vaultPrivateKeyEncryptionKeyName' property as the default key name for JavaKeyStore
    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultPrivateKeyEncryptionKeyName(),
        keyForKeyStore);

    assertTrue(new File(keyStorePath).exists());
    userKeyPairService = new UserKeyPairServiceJSchImpl(TEST_PASSPHRASE);
    keyPairInfo = userKeyPairService.generateNewKeyPair();
  }

}
