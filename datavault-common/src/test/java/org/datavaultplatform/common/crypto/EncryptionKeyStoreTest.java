package org.datavaultplatform.common.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EncryptionKeyStoreTest {

  @SneakyThrows
  void checkKeyStore(String path, String password, String keyname) {
      Encryption enc = new Encryption();
      Encryption.addBouncyCastleSecurityProvider();
      enc.setVaultEnable(false);
      enc.setKeystoreEnable(true);
      enc.setKeystorePath(path);
      enc.setKeystorePassword(password);
      enc.setVaultPrivateKeyEncryptionKeyName(keyname);
      byte[] iv = Encryption.generateIV();
      String randomSecret = UUID.randomUUID().toString();
      SecretKey secret = Encryption.getSecretKeyFromKeyStore(keyname);
      assertEquals("AES", secret.getAlgorithm());
      assertEquals("RAW", secret.getFormat());
      byte[] encrypted = Encryption.encryptSecret(randomSecret, null, iv);
      byte[] decrpyted = Encryption.decryptSecret(encrypted, iv, null);
      String decrypedSecret = new String(decrpyted, StandardCharsets.UTF_8);
      assertEquals(randomSecret, decrypedSecret);
  }

  @Test
  @Disabled
  void testKeyStoreFile() {
    checkKeyStore("/path/to/keystore.ks", "<password>", "<keyname>");
  }
}
