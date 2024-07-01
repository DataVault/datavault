package org.datavaultplatform.common.crypto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.Verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
public class EncryptionValidatorTest extends BaseTempKeyStoreTest{

  private final EncryptionValidator validator = new EncryptionValidator();


  @ParameterizedTest
  @NullSource
  @EmptySource
  @ValueSource(strings = {" ", "\n", "\t"})
  void testBlankDataKeyName(String keyNameForDataEncryption) {
    Encryption enc = new Encryption();
    enc.setKeystoreEnable(true);
    enc.setVaultDataEncryptionKeyName(keyNameForDataEncryption);

    log.info("keyNameForDataEncryption is [{}]", keyNameForDataEncryption);
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> validator.validate(true, false, null));

    assertEquals("property [vault.dataEncryptionKeyName] is not set", ex.getMessage());
  }

  @ParameterizedTest
  @NullSource
  @EmptySource
  @ValueSource(strings = {" ", "\n", "\t"})
  void testBlankPrivateKeyName(String keyNameForPrivateKeyEncryption) {
    Encryption enc = new Encryption();
    enc.setKeystoreEnable(true);
    enc.setVaultPrivateKeyEncryptionKeyName(keyNameForPrivateKeyEncryption);
    log.info("keyNameForPrivateKeyEncryption is [{}]", keyNameForPrivateKeyEncryption);
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> validator.validate(false, true, null));

    assertEquals("property [vault.privateKeyEncryptionKeyName] is not set", ex.getMessage());
  }

  @Test
  void testValidatePrivateKeyNameButDateKeyNameIsNull() {
    Encryption enc = new Encryption();
    enc.setVaultPrivateKeyEncryptionKeyName("privateKeyName");
    enc.setVaultDataEncryptionKeyName(null);
   validator.validateKeyNames(false, true);
  }

  @ParameterizedTest
  @CsvSource(nullValues = "null", value = {
      "null,      true,  true,  true",
      "null,      true,  false, true",
      "null,      false, true,  true",
      "null,      false, false, false",

      "' ',      true,  true,  true",
      "' ',      true,  false, true",
      "' ',      false, true,  true",
      "' ',      false, false, false",

      "key-name, true,  true,  true",
      "key-name, true,  false, false",
      "key-name, false, true,  false",
      "key-name, false, false, false",
  })
  void testSameKeyNames(String keyName, boolean validateDataKey, boolean validatePrivateKeysKey, boolean errorExpected) {
    Encryption enc = new Encryption();

    enc.setVaultPrivateKeyEncryptionKeyName(keyName);
    enc.setVaultDataEncryptionKeyName(keyName);
    try {
      validator.validateKeyNames(validateDataKey, validatePrivateKeysKey);
      assertFalse(errorExpected);
    } catch (IllegalArgumentException ex) {
      assertTrue(errorExpected);
    }
  }


  @Test
  @SneakyThrows
  void testWithActualKeyStore() {

    File ksFile = new File(keyStorePath);
    String sha1 = Verify.getDigest(ksFile);
    assertTrue(ksFile.exists());
    validator.validate(true,true, sha1);
  }
  @Test
  @SneakyThrows
  void testWithActualKeyStoreButInvalidSha1() {

    File ksFile = new File(keyStorePath);
    assertTrue(ksFile.exists());
    String actualSha1 = Verify.getDigest(ksFile);
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(true,true, "invalid-sha1"));
    assertEquals(String.format("KeyStore ["+ksFile+"]: actual SHA1["+actualSha1+"] does match expected SHA1[invalid-sha1]"), ex.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "    ", "\t", "\n"})
  @NullSource
  @SneakyThrows
  void testWithActualKeyStoreButBlankSha1(String blank) {
    File ksFile = new File(keyStorePath);
    assertTrue(ksFile.exists());
    validator.validate(true,true, blank);
  }

  @Test
  @SneakyThrows
  void testWithActualButUnreadableKeyStore() {

    File ksFile = new File(keyStorePath);
    assertTrue(ksFile.exists());

    ksFile.setReadable(false);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(true,true, "invalid-sha1"));
    assertEquals("KeyStore file is not readable: " + ksFile, ex.getMessage());
  }

  @Test
  @SneakyThrows
  void testWithNonExistentKeyStore() {

    Encryption enc = new Encryption();
    enc.setKeystorePath("/tmp/does-not-exist.ks");
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(true,true, "invalid-sha1"));
    assertEquals("KeyStore file does not exist: /tmp/does-not-exist.ks", ex.getMessage());
  }

  @Test
  @SneakyThrows
  void testWithActualKeyStoreWithMissingDataKey() {
    assertTrue(new File(keyStorePath).exists());

    Encryption enc = new Encryption();
    enc.setVaultDataEncryptionKeyName("missingDataKey");

    assertEquals("missingDataKey", Encryption.getVaultDataEncryptionKeyName());
    IllegalStateException ex = assertThrows(IllegalStateException.class,
        EncryptionValidator::encryptThenDecryptTempFile);
    Throwable cause = ex.getCause();
    assertEquals(IllegalArgumentException.class, cause.getClass());
    assertEquals(
        String.format("No key found in keystore[%s] for KeyName[%s]", keyStorePath, "missingDataKey"),
        cause.getMessage());
  }
}
