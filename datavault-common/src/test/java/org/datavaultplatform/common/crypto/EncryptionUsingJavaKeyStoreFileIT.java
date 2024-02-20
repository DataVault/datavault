package org.datavaultplatform.common.crypto;

import jakarta.xml.bind.DatatypeConverter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.DigestUtils;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
  private static final String KEY_NAME_PRIVATE_KEYS = "keynamepks";
  private static final String KEY_NAME_DATA = "keynamedata";

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
    enc.setVaultPrivateKeyEncryptionKeyName(KEY_NAME_PRIVATE_KEYS);
    enc.setVaultDataEncryptionKeyName(KEY_NAME_DATA);
    enc.setKeystoreEnable(true);
    enc.setKeystorePath(keyStorePath);
    enc.setKeystorePassword(KEY_STORE_PASSWORD);
    Encryption.checkKeyNamesAreNotSame();


    SecretKey keyForPrivateKeys = Encryption.generateSecretKey();
    SecretKey keyForData = Encryption.generateSecretKey();


    assertFalse(new File(this.keyStorePath).exists());

    // Encryption class uses 'vaultPrivateKeyEncryptionKeyName' property as the default key name for JavaKeyStore'
    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultPrivateKeyEncryptionKeyName(), keyForPrivateKeys);
    Encryption.saveSecretKeyToKeyStore(Encryption.getVaultDataEncryptionKeyName(), keyForData);
    assertTrue(new File(this.keyStorePath).exists());

    checkEncryptDecrypt(Encryption.generateSecretKey());
    checkEncryptDecrypt(keyForPrivateKeys);
    checkEncryptDecrypt(keyForData);

    KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE_JCEKS);
    ks.load(new FileInputStream(keyStorePath), KEY_STORE_PASSWORD.toCharArray());
    Enumeration<String> aliases = ks.aliases();
    List<String> al = new ArrayList<>();
    while(aliases.hasMoreElements()) {
      al.add(aliases.nextElement());
    }

    // Alias names are stored in JKS as lowercase!
    assertIterableEquals(Arrays.asList(KEY_NAME_PRIVATE_KEYS, KEY_NAME_DATA), al);
    SecretKey directFromKSPrivateKeys = (SecretKey) ks.getKey(KEY_NAME_PRIVATE_KEYS, KEY_PASSWORD.toCharArray());
    checkAES(directFromKSPrivateKeys);
    SecretKey directFromKSData = (SecretKey) ks.getKey(KEY_NAME_DATA, KEY_PASSWORD.toCharArray());
    checkAES(directFromKSData);
    assertEquals(keyForPrivateKeys, directFromKSPrivateKeys);
    assertEquals(keyForData, directFromKSData);

    readKeyFromJCEKeyStore(keyStorePath, KEY_STORE_PASSWORD, KEY_PASSWORD);
   }

  private void checkAES(SecretKey secretKey) {
    assertEquals("AES", secretKey.getAlgorithm());
  }

  @SneakyThrows
  private void checkEncryptDecrypt(SecretKey... secretKeys) {
    String plainText = UUID.randomUUID().toString();
    byte[] iv = Encryption.generateIV();
    Set<String> allEncrypted = new HashSet<>();
    for(SecretKey secretKey : secretKeys) {
      byte[] encrypted = Encryption.encryptSecret(plainText, secretKey, iv);
      byte[] decrypted = Encryption.decryptSecret(encrypted, iv, secretKey);
      assertEquals(plainText, toString(decrypted));
      allEncrypted.add(toString(encrypted));
    }
    assertEquals(1, allEncrypted.size());
  }

  private String toString(byte[] data){
    return new String(data, StandardCharsets.UTF_8);
  }

  @SneakyThrows
  public static void readKeyFromPKCS12KeyStore(final String keyStorePath, final String keyStorePassword, final String keyPassword) {
    readKeyFromKeyStore(KEY_STORE_TYPE_PKCS12, keyStorePath, keyStorePassword, keyPassword);
  }

   @SneakyThrows
   public static void readKeyFromJCEKeyStore(final String keyStorePath, final String keyStorePassword, final String keyPassword) {
     readKeyFromKeyStore(KEY_STORE_TYPE_JCEKS, keyStorePath, keyStorePassword, keyPassword);
   }

   @SneakyThrows
   public static void readKeyFromKeyStore(final String instanceType, final String keyStorePath, final String keyStorePassword, final String keyPassword) {
     KeyStore ks = KeyStore.getInstance(instanceType);
     byte[] digest = DigestUtils.md5Digest(new FileInputStream(keyStorePath));
     String md5 = DatatypeConverter.printHexBinary(digest).toLowerCase();

     ks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
     Enumeration<String> aliases = ks.aliases();
     List<String> keyNames = new ArrayList<>();
     while (aliases.hasMoreElements()) {
       keyNames.add(aliases.nextElement());
     }
     for(String keyName : keyNames) {
       boolean isKey = ks.isKeyEntry(keyName);
       if(!isKey){
         continue;
       }
       // ooh - alias names are stored in JKS as lowercase!
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
   }

   public static String formatInstant(Instant timestamp) {
     String newYorkDateTimePattern = "dd/MMM/yyyy HH:mm:ss z";
     DateTimeFormatter formatter = DateTimeFormatter.ofPattern(newYorkDateTimePattern).withLocale(
         Locale.getDefault())
         .withZone(ZoneId.systemDefault());
     return formatter.format(timestamp);
   }

  public static Instant toInstant(Date dateToConvert) {
    return Instant.ofEpochMilli(dateToConvert.getTime());
  }

  @Disabled
  @Test
  void testDebugExistingKeyStore() {
    readKeyFromJCEKeyStore("/path/to/existing.ks","thePassword","thePassword");
  }


  public static String toHex(byte[] encoded){
    return "0x" + DatatypeConverter.printHexBinary(encoded).toUpperCase();
  }

  @Test
  void testToHex() {
    byte[] orig = {-27, 56, -121, -4, -21, 2, -84, -90, -52, -12, 117, 30, 49, 41, -81, -69, -31,
        111, -124, 42, -20, -69, 117, -118, 17, 123, -55, -84, 11, -126, -28, 99, 33, 3, 27, 67,
        -91, 49, -71, 16, 117, 36, -6, -80, 76, 118, 39, -101, -58, 53, 112, -4, 42, 121, 29, 48,
        -42, -24, 11, 110, -117, -18, -70, -13, -12, -28, 22, -56, 98, 72, -58, -58, -49, 118, 35,
        83, 19, 16, 56, 19, 12, 53, 123, -103, -115, 7, -36, 58, -110, -74, -75, 120, 1, 91, -77,
        -59};
    String hex = toHex(orig);
    assertEquals(
        "0xE53887FCEB02ACA6CCF4751E3129AFBBE16F842AECBB758A117BC9AC0B82E46321031B43A531B9107524FAB04C76279BC63570FC2A791D30D6E80B6E8BEEBAF3F4E416C86248C6C6CF762353131038130C357B998D07DC3A92B6B578015BB3C5",
        hex);
  }
}
