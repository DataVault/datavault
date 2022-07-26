package org.datavaultplatform.common.crypto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.DigestUtils;

/**
 * Tests parts of the Encryption class that uses Java Key Store File.
 */
@Slf4j
public class EncryptionUsingJavaKeyStoreFileIT {

  private static final String KEY_STORE_TYPE_JCEKS = "JCEKS";
  private static final String KEY_STORE_TYPE_PKCS12 = "PKCS12";

  private static final String KEY_STORE_PASSWORD = "thePassword";

  //The Encryption class uses same password for keystore and key!
  private static final String KEY_PASSWORD = KEY_STORE_PASSWORD;
  private static final String KEY_NAME = "thekeyname";

  @TempDir
  static File temp;

  String keyStorePath;

  @BeforeAll
  static void init() {
    Encryption.addBouncyCastleSecurityProvider();
    assertTrue(Encryption.isInitialised());
  }

  @BeforeEach
  void setup() {
    keyStorePath = temp.toPath().resolve("test.ks").toString();
    log.info("TEMP KEY IS AT [{}]",keyStorePath);
  }

  @Test
  @SneakyThrows
  void testActualJCEKeyStore() {
    Encryption enc = new Encryption();
    enc.setVaultEnable(false);
    enc.setVaultPrivateKeyEncryptionKeyName(KEY_NAME);

    enc.setKeystoreEnable(true);
    enc.setKeystorePath(keyStorePath);
    enc.setKeystorePassword(KEY_STORE_PASSWORD);

    SecretKey keyForKeyStore = Encryption.generateSecretKey();

    assertFalse(new File(this.keyStorePath).exists());

    // Encryption class uses 'vaultPrivateKeyEncryptionKeyName' property as the default key name for JavaKeyStore'
    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultPrivateKeyEncryptionKeyName(), keyForKeyStore);
    assertTrue(new File(this.keyStorePath).exists());

    byte[] iv = Encryption.generateIV();

    String plainText = UUID.randomUUID().toString();

    byte[] encrypted1 = Encryption.encryptSecret(plainText, keyForKeyStore, iv);

    //This method call (with null secret key param) is used in 'org.datavaultplatform.broker.controllers.FileStoreController.addFileStoreSFTP' to encrypt SSH private keys
    byte[] encrypted2 = Encryption.encryptSecret(plainText, null, iv);

    // proves that the encryption key used is the one saved with name from 'vaultPrivateKeyEncryptionKeyName'
    assertArrayEquals(encrypted1, encrypted2);

    byte[] decrypted = Encryption.decryptSecret(encrypted1, iv);
    assertEquals(plainText, new String(decrypted, StandardCharsets.UTF_8));

    KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE_JCEKS);
    ks.load(new FileInputStream(keyStorePath), KEY_STORE_PASSWORD.toCharArray());
    Enumeration<String> aliases = ks.aliases();
    List<String> al = new ArrayList<>();
    while(aliases.hasMoreElements()) {
      al.add(aliases.nextElement());
    }
    // ooh - alias names are stored in JKS as lowercase!
    assertIterableEquals(Collections.singleton(KEY_NAME), al);
    SecretKey directFromKS = (SecretKey) ks.getKey(KEY_NAME, KEY_PASSWORD.toCharArray());
    assertEquals(keyForKeyStore, directFromKS);

    readKeyFromJCEKeyStore(keyStorePath, KEY_STORE_PASSWORD, KEY_NAME, KEY_PASSWORD);
   }

  @SneakyThrows
  public static void readKeyFromPKCS12KeyStore(final String keyStorePath, final String keyStorePassword, final String keyNameRaw, final String keyPassword) {
    readKeyFromKeyStore(KEY_STORE_TYPE_PKCS12, keyStorePath, keyStorePassword, keyNameRaw, keyPassword);
  }

   @SneakyThrows
   public static void readKeyFromJCEKeyStore(final String keyStorePath, final String keyStorePassword, final String keyNameRaw, final String keyPassword) {
     readKeyFromKeyStore(KEY_STORE_TYPE_JCEKS, keyStorePath, keyStorePassword, keyNameRaw, keyPassword);
   }

   @SneakyThrows
   public static void readKeyFromKeyStore(final String instanceType, final String keyStorePath, final String keyStorePassword, final String keyNameRaw, final String keyPassword) {
     final String keyName = keyNameRaw.toLowerCase();
     KeyStore ks = KeyStore.getInstance(instanceType);
     byte[] digest = DigestUtils.md5Digest(new FileInputStream(keyStorePath));
     String md5 = DatatypeConverter.printHexBinary(digest).toLowerCase();

     ks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
     Enumeration<String> aliases = ks.aliases();
     List<String> al = new ArrayList<>();
     while (aliases.hasMoreElements()) {
       al.add(aliases.nextElement());
     }
     // ooh - alias names are stored in JKS as lowercase!
     assertTrue(al.contains(keyName));
     SecretKey directFromKS = (SecretKey) ks.getKey(keyName, keyPassword.toCharArray());
     Instant created = toInstant(ks.getCreationDate(keyName));
     byte[] encoded = directFromKS.getEncoded();
     String hex = toHex(encoded);
     log.info("Keystore Path [{}]", keyStorePath);
     log.info("Keystore MD5 [{}]", md5);
     log.info("Keystore Provider [{}]", ks.getProvider());
     log.info("Keystore Type [{}]", ks.getType());
     log.info("Secret Key Name [{}] ", keyName);
     log.info("\tKey Created [{}]", formatInstant(created));
     log.info("\tKey Format [{}]", directFromKS.getFormat());
     log.info("\tKey Size [{}]bits", encoded.length * 8);
     log.info("\tKey Algorithm [{}]", directFromKS.getAlgorithm());
     log.info("\tKey Encoded [{}]", hex);
   }

   public static String formatInstant(Instant timestamp) {
     String newYorkDateTimePattern = "dd/MMM/yyyy HH:mm:ss z";
     DateTimeFormatter formatter = DateTimeFormatter.ofPattern(newYorkDateTimePattern).withLocale(
         Locale.getDefault())
         .withZone(ZoneId.systemDefault());
     return formatter.format(timestamp);
   }

   public static String toHex(byte[] encoded){
     StringBuilder encodedHex = new StringBuilder("0x");
     for(int i=0;i<encoded.length;i++){
       encodedHex.append(byteToHex(encoded[i]));
     }
     return encodedHex.toString();
   }

  public static String byteToHex(byte num) {
    char[] hexDigits = new char[2];
    hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
    hexDigits[1] = Character.forDigit((num & 0xF), 16);
    return new String(hexDigits).toUpperCase();
  }

  public static Instant toInstant(Date dateToConvert) {
    return Instant.ofEpochMilli(dateToConvert.getTime());
  }
}
