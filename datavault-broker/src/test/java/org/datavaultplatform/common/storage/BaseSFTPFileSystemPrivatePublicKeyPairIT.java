package org.datavaultplatform.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
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
import org.datavaultplatform.common.model.FileInfo;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

@Slf4j
public abstract class BaseSFTPFileSystemPrivatePublicKeyPairIT extends BaseSFTPFileSystemIT {

  static final String TEST_PASSPHRASE = "tenet";
  static final String ENV_PUBLIC_KEY = "PUBLIC_KEY";
  static final String KEY_STORE_PASSWORD = "keyStorePassword";
  static final String SSH_KEY_NAME = "sshKeyName";

  static File keyStoreTempDir;
  static KeyPairInfo keyPairInfo;

  static final Path tempLocalPath;
  
  static {
    try {
      tempLocalPath = Files.createTempDirectory("sftpTestFilesDir");
      for (int i = 0; i < 1000; i++) {
        Path tempFile = tempLocalPath.resolve(String.format("temp-%s.txt", i));
        try (PrintWriter pw = new PrintWriter(new FileWriter(tempFile.toFile()))) {
          pw.printf("test file number - [%s]%n", i);

        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

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
        .withCopyFileToContainer(MountableFile.forHostPath(tempLocalPath),"/config")
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

  @Test
  public void testListOneThousandFiles() {

    long startMS = System.currentTimeMillis();
    List<FileInfo> files = getSftpDriver().list(".");
    long diffMS = System.currentTimeMillis() - startMS;
    for (int i = 0; i < files.size(); i++) {
      FileInfo info = files.get(i);
      System.out.printf("%04d - [%s]%n", i, info);
    }
    getLog().info("Listing {} files took [{}]ms", files.size(), diffMS);
    assertThat(files).hasSizeGreaterThan(1000);
  }

}
