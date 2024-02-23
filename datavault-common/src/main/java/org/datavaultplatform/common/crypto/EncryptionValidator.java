package org.datavaultplatform.common.crypto;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.Arrays;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.Context.AESMode;
import org.springframework.util.Assert;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Meant to be called from the Worker and Broker on startup to check the Encryption is set up correctly.
 */
@Slf4j
public class EncryptionValidator {

  private static final String LABEL_KEY_FOR_PRIVATE_KEYS = "key for SSHPrivateKeys";
  private static final String LABEL_KEY_FOR_DATA = "key for Data";

  public void validate(boolean validateDataKey, boolean validatePrivateKeysKey, String encryptionKeyStoreSHA1) {
    validateKeyStoreSHA1(encryptionKeyStoreSHA1);
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

      encryptThenDecryptTempFile();
    }
    if (validatePrivateKeysKey) {
      validateEncryptionConfig(LABEL_KEY_FOR_PRIVATE_KEYS, Encryption.getVaultPrivateKeyEncryptionKeyName());
      validateEncryptionConfig(LABEL_KEY_FOR_PRIVATE_KEYS, null);
    }
    Encryption.logKeyDigests();
  }

  @SneakyThrows
  private void validateKeyStoreSHA1(String expectedKeyStoreSha1) {
    if (!Encryption.getKeystoreEnable()) {
      log.warn("Encryption KeyStore is not enabled");
      return;
    }
    if (StringUtils.isBlank(expectedKeyStoreSha1)) {
      log.warn("Expected KeyStore SHA1 is blank");
      return;
    }

    String keyStorePath = Encryption.getKeystorePath();
    File keyStoreFile = new File(keyStorePath);

    Assert.isTrue(keyStoreFile.exists(), "KeyStore file does not exist: " + keyStorePath);
    Assert.isTrue(keyStoreFile.canRead(), "KeyStore file is not readable: " + keyStorePath);

    String actualKeyStoreSha1 = Verify.getDigest(keyStoreFile);
    log.info("Encryption KeyStore [{}], actual SHA1[{}]", keyStorePath, actualKeyStoreSha1);
    Assert.isTrue(expectedKeyStoreSha1.equalsIgnoreCase(actualKeyStoreSha1),
          () -> String.format("KeyStore [%s]: actual SHA1[%s] does match expected SHA1[%s]",
            keyStorePath, actualKeyStoreSha1, expectedKeyStoreSha1));
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
              keyNameData));
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

  @SneakyThrows
  public static void encryptThenDecryptTempFile() {
    File temp = Files.createTempFile("dv", ".tmp").toFile();
    try {
      String plainText = UUID.randomUUID().toString();
      byte[] original = plainText.getBytes(StandardCharsets.UTF_8);

      try(FileWriter fw = new FileWriter(temp)){
        fw.write(plainText);
      }

      byte[] iv = Encryption.encryptFile(AESMode.GCM, temp);
      byte[] encrypted = FileUtils.readFileToByteArray(temp);

      Encryption.decryptFile(AESMode.GCM, temp, iv);
      byte[] decrypted = FileUtils.readFileToByteArray(temp);

      Assert.isTrue(!Arrays.areEqual(original, encrypted), () -> "original and encrypted should be un-equal");
      Assert.isTrue( Arrays.areEqual(original, decrypted), () -> "original and decrypted should be equal");

      log.info("SUCCESS : Encrypt/Decrypt of temp file.");
    } catch (Exception ex) {
      throw new IllegalStateException("Problem testing file encryption/decryption", ex);
    } finally {
      temp.delete();
    }
  }

}
