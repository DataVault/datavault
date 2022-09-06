package org.datavaultplatform.common.storage;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.datavaultplatform.broker.services.UserKeyPairServiceJSchImpl;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.docker.DockerImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
 A test class for the SFTPFileSystem interface (implements SFTPFileSystemDriver)
 uses Public/Private KeyPair to authenticate with SFTPFileSystem
 */
@Slf4j
@Testcontainers(disabledWithoutDocker = true)
public class SFTPFileSystemWithPrivatePublicKeyPairIT extends BaseSFTPFileSystemIT {

  private static final String TEST_USER = "testuser";
  private static final String TEST_PASSPHRASE = "tenet";
  private static final String ENV_PUBLIC_KEY = "PUBLIC_KEY";

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
    GenericContainer<?> sftpServerContainer = new GenericContainer<>(DockerImage.OPEN_SSH_8pt6_IMAGE_NAME)
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PUBLIC_KEY, this.keyPairInfo.getPublicKey()) //this causes the public key to be added to /config/.ssh/authorized_keys
        .withExposedPorts(2222)
        .waitingFor(Wait.forListeningPort());
    return sftpServerContainer;
  }

  public Map<String,String> getStoreProperties()
      throws Exception {
    HashMap<String, String> props = new HashMap<>();

    byte[] iv = Encryption.generateIV();
    byte[] encrypted = Encryption.encryptSecret(keyPairInfo.getPrivateKey(), null, iv);

    //standard sftp properties
    props.put("username", TEST_USER);
    props.put("rootPath", "/config"); //this is the directory ON THE SFTP SERVER - for OpenSSH containers, it's config
    props.put("host", sftpServerContainer.getHost());
    props.put("port", String.valueOf(sftpServerContainer.getMappedPort(2222)));

    //extra properties for the sftp private key authentication
    props.put("privateKey", Base64.toBase64String(encrypted));
    props.put("passphrase", TEST_PASSPHRASE);
    props.put("iv", Base64.toBase64String(iv));

    return props;
  }


}
