package org.datavaultplatform.common.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.utils.NullableConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
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
        () -> validator.validate(true, false));

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
        () -> validator.validate(false, true));

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
  @CsvSource({
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
  void testSameKeyNames(@ConvertWith(NullableConverter.class) String keyName, boolean validateDataKey, boolean validatePrivateKeysKey, boolean errorExpected) {
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

    assertTrue(new File(keyStorePath).exists());
    validator.validate(true,true);
  }
}
