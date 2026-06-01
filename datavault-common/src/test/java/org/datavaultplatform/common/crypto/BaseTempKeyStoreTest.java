package org.datavaultplatform.common.crypto;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

@Slf4j
public abstract class BaseTempKeyStoreTest {

  static final String KEY_NAME_FOR_SSH = "key-name-for-ssh";
  static final String KEY_NAME_FOR_DATA = "key-name-for-data";
  static final String KEY_STORE_PASSWORD = "testPassword";

  @TempDir
  File temp;

  String keyStorePath;

  @BeforeEach
  @SneakyThrows
  void setupKeyStore() {

    Encryption.addBouncyCastleSecurityProvider();
    keyStorePath = temp.toPath().resolve("test.ks").toString();
    log.info("TEMP KEY IS AT [{}]", keyStorePath);
    Encryption enc = new Encryption();
    enc.setVaultEnable(false);
    enc.setVaultPrivateKeyEncryptionKeyName(KEY_NAME_FOR_SSH);
    enc.setVaultDataEncryptionKeyName(KEY_NAME_FOR_DATA);

    enc.setKeystoreEnable(true);
    enc.setKeystorePath(keyStorePath);
    enc.setKeystorePassword(KEY_STORE_PASSWORD);

    SecretKey keyForSSH = Encryption.generateSecretKey();
    SecretKey keyForData = Encryption.generateSecretKey();

    assertFalse(new File(keyStorePath).exists());

    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultPrivateKeyEncryptionKeyName(),
        keyForSSH);
    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultDataEncryptionKeyName(),
        keyForData);

    assertTrue(new File(keyStorePath).exists());
  }
  
  @AfterEach
  void tearDown() {
    // JUnit @TempDir takes care of deleting the 'temp' directory
    // but clearing the Encryption settings is good practice since they seem to be static
    Encryption enc = new Encryption();
    enc.setKeystoreEnable(false);
    enc.setKeystorePath(null);
    enc.setKeystorePassword(null);
    enc.setVaultPrivateKeyEncryptionKeyName(null);
    enc.setVaultDataEncryptionKeyName(null);
  }

}
