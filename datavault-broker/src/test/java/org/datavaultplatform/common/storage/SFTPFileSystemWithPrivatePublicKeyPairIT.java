package org.datavaultplatform.common.storage;

import java.util.HashMap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.datavaultplatform.broker.services.UserKeyPairServiceJSchImpl;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.docker.DockerImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
 Tests SFTPFileSystem class against SFTP Server using private/public keypair authentication.
 */
@Slf4j
@Testcontainers(disabledWithoutDocker = true)
public class SFTPFileSystemWithPrivatePublicKeyPairIT extends BaseSFTPFileSystemIT {

  private static final String TEST_PASSPHRASE = "tenet";
  private static final String ENV_PUBLIC_KEY = "PUBLIC_KEY";

  @Override
  protected void authenticationSetup() {
    Encryption.addBouncyCastleSecurityProvider();
    Encryption enc = new Encryption();
    enc.setKeystoreEnable(true);
    enc.setKeystorePath("/Users/davidhay/DEV/DV/FORK/local.ks");
    enc.setVaultPrivateKeyEncryptionKeyName("forprivatekeys");
    enc.setKeystorePassword("thePassword");

    userKeyPairService = new UserKeyPairServiceJSchImpl(TEST_PASSPHRASE);
    keyPairInfo = userKeyPairService.generateNewKeyPair();
  }

  @Override
  protected GenericContainer<?> getSftpTestContainer() {
    return new GenericContainer<>(DockerImage.OPEN_SSH_8pt6_IMAGE_NAME)
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PUBLIC_KEY, this.keyPairInfo.getPublicKey()) //this causes the public key to be added to /config/.ssh/authorized_keys
        .withExposedPorts(2222)
        .waitingFor(Wait.forListeningPort());
  }

  @Override
  @SneakyThrows
  protected void addExtraProps(HashMap<String, String> props) {

    byte[] iv = Encryption.generateIV();
    byte[] encrypted = Encryption.encryptSecret(keyPairInfo.getPrivateKey(), null, iv);

    props.put("passphrase", TEST_PASSPHRASE);
    props.put("iv", Base64.toBase64String(iv));
    props.put("privateKey", Base64.toBase64String(encrypted));
  }


}
