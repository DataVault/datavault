package org.datavaultplatform.common.storage;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.util.HashMap;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.datavaultplatform.broker.services.UserKeyPairServiceJSchImpl;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.docker.DockerImage;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
 Tests SFTPFileSystem class against SFTP Server using private/public keypair authentication.
 */
@Slf4j
@Testcontainers(disabledWithoutDocker = true)
public class SFTPFileSystemWithPrivatePublicKeyPairIT extends BaseSFTPFileSystemIT {
  static final String TEST_PASSPHRASE = "tenet";
  static final String ENV_PUBLIC_KEY = "PUBLIC_KEY";
  static final String KEY_STORE_PASSWORD = "keyStorePassword";
  static final String SSH_KEY_NAME = "sshKeyName";

  @TempDir
  File keyStoreTempDir;

  @Override
  GenericContainer<?> getSftpTestContainer() {
    return new GenericContainer<>(DockerImage.OPEN_SSH_8pt6_IMAGE_NAME)
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PUBLIC_KEY, this.keyPairInfo.getPublicKey()) //this causes the public key to be added to /config/.ssh/authorized_keys
        .withExposedPorts(2222)
        .waitingFor(Wait.forListeningPort());
  }

  @Override
  void addAuthenticationProps(HashMap<String, String> props) throws Exception {

    byte[] iv = Encryption.generateIV();
    byte[] encrypted = Encryption.encryptSecret(keyPairInfo.getPrivateKey(), null, iv);

    props.put("passphrase", TEST_PASSPHRASE);
    props.put("iv", Base64.toBase64String(iv));
    props.put("privateKey", Base64.toBase64String(encrypted));
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
