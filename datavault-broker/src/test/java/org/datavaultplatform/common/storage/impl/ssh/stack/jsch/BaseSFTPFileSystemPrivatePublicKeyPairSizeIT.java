package org.datavaultplatform.common.storage.impl.ssh.stack.jsch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.datavaultplatform.broker.services.UserKeyPairService;
import org.datavaultplatform.broker.services.UserKeyPairService.KeyPairInfo;
import org.datavaultplatform.broker.services.UserKeyPairServiceJSchImpl;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.crypto.SshRsaKeyUtils;
import org.datavaultplatform.common.docker.DockerImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@Slf4j
public abstract class BaseSFTPFileSystemPrivatePublicKeyPairSizeIT extends BaseSFTPFileSystemSizeIT {

  static final String TEST_PASSPHRASE = "tenet";
  static final String ENV_PUBLIC_KEY = "PUBLIC_KEY";
  static final String KEY_STORE_PASSWORD = "keyStorePassword";
  static final String SSH_KEY_NAME = "sshKeyName";

  static File keyStoreTempDir;
  static KeyPairInfo keyPairInfo;



  static GenericContainer<?> initialiseContainer(String tcName) {

    try {
      keyStoreTempDir = Files.createTempDirectory("tmpKeyStoreDir").toFile();
    }catch(IOException ex){
      throw new RuntimeException(ex);
    }

    keyPairInfo = generateKeyPair();

    return new GenericContainer<>(DockerImage.OPEN_SSH_8pt6_IMAGE_NAME)
        .withEnv("TC_NAME", tcName)
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PUBLIC_KEY,
            keyPairInfo.getPublicKey()) //this causes the public key to be added to /config/.ssh/authorized_keys
        .withExposedPorts(SFTP_SERVER_PORT)
        //.withFileSystemBind("/Users/davidhay/SPARSE_FILES/files","/tmp/files", BindMode.READ_ONLY)
        .waitingFor(Wait.forListeningPort());
  }


  @SneakyThrows
  @Override
  public void addAuthenticationProps(Map<String, String> props) {
    byte[] iv = Encryption.generateIV();
    byte[] encrypted = Encryption.encryptSecret(keyPairInfo.getPrivateKey(), null, iv);

    props.put(PropNames.PASSPHRASE, TEST_PASSPHRASE);
    props.put(PropNames.IV, Base64.toBase64String(iv));
    props.put(PropNames.PRIVATE_KEY, Base64.toBase64String(encrypted));

    RSAPublicKey publicKey = SshRsaKeyUtils.readPublicKey(
        keyPairInfo.getPublicKey());
    if(getLog().isTraceEnabled()) {
      getLog().trace("ORIG PUBLIC KEY MODULUS [{}]", publicKey.getModulus().toString(16));
    }
  }

  @SneakyThrows
  private static KeyPairInfo generateKeyPair() {

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
    UserKeyPairService userKeyPairService = new UserKeyPairServiceJSchImpl(TEST_PASSPHRASE);
    return userKeyPairService.generateNewKeyPair();
  }
}
