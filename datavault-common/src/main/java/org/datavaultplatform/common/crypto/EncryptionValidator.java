package org.datavaultplatform.common.crypto;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * Meant to be called from the Worker and Broker on startup to check the Encryption is set up correctly.
 */
@Slf4j
public class EncryptionValidator {

  private static final String LABEL_KEY_FOR_PRIVATE_KEYS = "key for SSHPrivateKeys";
  private static final String LABEL_KEY_FOR_DATA = "key for Data";

  public void validate(boolean validateDataKey, boolean validatePrivateKeysKey) {
    if (Encryption.getVaultEnable() && Encryption.getKeystoreEnable()) {
      log.warn("The Encryption is setup with BOTH HashiCorpVault and Java Keystore");
    }
    if (!(Encryption.getVaultEnable() || Encryption.getKeystoreEnable())) {
      log.warn("The Encryption is NOT setup for either HashiCorpVault OR Java Keystore");
    }
    if (Encryption.getKeystoreEnable()) {
      log.info("Encryption KeyStore [{}]", Encryption.getKeystorePath());
    }
    if (Encryption.getVaultEnable()) {
      log.info("Encryption Vault [{}]", Encryption.getVaultAddress());
    }
    validateKeyNames(validateDataKey, validatePrivateKeysKey);
    if (validateDataKey) {
      validateEncryptionConfig(LABEL_KEY_FOR_DATA, Encryption.getVaultDataEncryptionKeyName());
    }
    if (validatePrivateKeysKey) {
      validateEncryptionConfig(LABEL_KEY_FOR_PRIVATE_KEYS, Encryption.getVaultPrivateKeyEncryptionKeyName());
      validateEncryptionConfig(LABEL_KEY_FOR_PRIVATE_KEYS, null);
    }
  }

  protected void validateKeyNames(boolean validateDataKey, boolean validatePrivateKeysKey) {
    String keyNameData = Encryption.getVaultDataEncryptionKeyName();
    String keyNamePrivateKey = Encryption.getVaultPrivateKeyEncryptionKeyName();
    if (validateDataKey) {
      Assert.isTrue(StringUtils.isNotBlank(keyNameData),
          () -> "property [vault.dataEncryptionKeyName] is not set");
    }
    if (validatePrivateKeysKey) {
      Assert.isTrue(StringUtils.isNotBlank(keyNamePrivateKey),
          () -> "property [vault.privateKeyEncryptionKeyName] is not set");
    }
    if (validateDataKey && validatePrivateKeysKey) {
      Assert.isTrue(!keyNameData.equals(keyNamePrivateKey), () ->
          String.format(
              "The properties [vault.dataEncryptionKeyName] and [vault.privateKeyEncryptionKeyName] cannot have the same value [%s]",
              Encryption.getVaultDataEncryptionKeyName()));
    }
  }

  private void validateEncryptionConfig(String label, String keyName) throws IllegalStateException {
    String randomSecret = UUID.randomUUID().toString();
    String encryptedThenDecrypted = encryptThenDecrypt(randomSecret, label, keyName);
    Assert.isTrue(randomSecret.equals(encryptedThenDecrypted),
        () -> String.format("Problem with the setup of Encryption using label[%s]keyName[%s]", label, keyName));
    log.info("Encryption Config is Valid for label[{}]keyName[{}]", label, keyName);
  }

  @SneakyThrows
  private String encryptThenDecrypt(String plainText, String label, String keyName) {
    try {
      byte[] iv = Encryption.generateIV();
      // null secretKey => the (Vault)PrivateKeyEncryptionKeyName (for SSH KEYS - NOT DATA)
      SecretKey secretKey = keyName == null ? null : Encryption.getSecretKeyFromKeyStore(keyName);
      byte[] encrypted = Encryption.encryptSecret(plainText, secretKey, iv);
      byte[] decrypted = Encryption.decryptSecret(encrypted, iv, secretKey);
      return new String(decrypted, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new IllegalStateException(String.format("Encryption Config is NOT VALID for label[%s]keyName[%s]", label, keyName) , ex);
    }
  }

}
