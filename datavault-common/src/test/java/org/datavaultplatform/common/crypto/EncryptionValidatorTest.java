package org.datavaultplatform.common.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
  void testSameKeyNames() {
    String keyName = "Same-Key-Name";
    Encryption enc = new Encryption();
    enc.setKeystoreEnable(true);
    enc.setVaultPrivateKeyEncryptionKeyName(keyName);
    enc.setVaultDataEncryptionKeyName(keyName);
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> validator.validate(true, true));

    assertEquals("The properties [vault.dataEncryptionKeyName] and [vault.privateKeyEncryptionKeyName] cannot have the same value [Same-Key-Name]", ex.getMessage());
  }


  @Test
  @SneakyThrows
  void testWithActualKeyStore() {

    assertTrue(new File(keyStorePath).exists());
    validator.validate(true,true);
  }
}
